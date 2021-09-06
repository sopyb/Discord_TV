package ro.sopy.discordtv.auth

import android.util.Base64
import com.neovisionaries.ws.client.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import ro.sopy.discordtv.debuging.Debug
import ro.sopy.discordtv.debuging.DebugPriority
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class QrAuth(private val listener: QrAuthListener): WebSocketAdapter() {
    private var keys: KeyPair
    private var user: LoginUser? = null
    private var heartBeatInterval: Int = 0
    private var lastHeartBeat: Int = 0

    interface QrAuthListener {
        fun onFingerprintReady(fingerprint: String)
        fun onUserScan(user: LoginUser)
        fun onUserCancel()
        fun onFinishLogin(user: LoginUser)
        fun onError(e: Exception)
    }

    init {
        //generate rsa key for
        val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(2048)
        keys = kpg.generateKeyPair()

        startServer()
    }

    /**
     * Util functions
     */

    private fun startServer() {
        try {
            WebSocketFactory().createSocket("wss://remote-auth-gateway.discord.gg/?v=1", 5000)
                .addHeader("Origin", "https://discord.com")
                .addListener(this)
                .connect()
        } catch (e: Exception) {
            Debug.log("init: ${e.message}", DebugPriority.ERROR, TAG)
            e.printStackTrace()
        }
    }

    private fun decrypt(input: String): ByteArray {
        return try {
            val decodedInput = Base64.decode(input, Base64.DEFAULT)
            val cipher = Cipher.getInstance("RSA/NONE/OAEPwithSHA-256andMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, keys.private, OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT))
            cipher.doFinal(decodedInput)
        } catch(e: Exception) {
            e.printStackTrace()
            Debug.log("decrypt: ${e.message}", DebugPriority.ERROR, TAG)
            ByteArray(0)
        }
    }

    private fun heartbeatSender(ws: WebSocket?) {
        while (true) {
            Thread.sleep(500)

            if (ws == null || !ws.isOpen) return

            val currentTime = System.currentTimeMillis().toInt()
            val passedTime = currentTime - lastHeartBeat

            if(passedTime > this.heartBeatInterval) {
                Debug.log("heartbeatSender: heartbeat sent", DebugPriority.DEBUG, TAG)
                ws.sendText("{\"op\": \"heartbeat\"}")
                this.lastHeartBeat = System.currentTimeMillis().toInt()
            }

        }
    }

    /**
     * WebSocket callback overwrites
     */

    override fun onConnected(
        websocket: WebSocket?,
        headers: MutableMap<String, MutableList<String>>?
    ) {
        Debug.log("onConnected: webSocket initialized.", DebugPriority.INFO, TAG)
        super.onConnected(websocket, headers)
    }

    override fun onTextMessage(websocket: WebSocket, text: String) {
        Debug.log("onTextMessage: got data $text", DebugPriority.DEBUG, TAG)

        val data = JSONObject(text)

        when (data.getString("op")) {
            "hello" -> {
                heartBeatInterval = data.getInt("heartbeat_interval")
                lastHeartBeat = System.currentTimeMillis().toInt()

                GlobalScope.launch {
                    heartbeatSender(websocket)
                }

                websocket.sendText("{\"op\": \"init\", \"encoded_public_key\": \"${
                    Base64.encodeToString(keys.public.encoded, Base64.DEFAULT)
                        .replace("\n", "")
                }\"}")

                Debug.log("onTextMessage: sent init message to auth webSocket", DebugPriority.DEBUG, TAG)
            }
            "nonce_proof" -> {
                val decryptedNonce = decrypt(data.getString("encrypted_nonce"))
                val digest = MessageDigest.getInstance("SHA-256")

                val proof = Base64.encode(digest.digest(decryptedNonce), Base64.URL_SAFE)
                    .decodeToString()
                    .replace("=", "")
                    .trimEnd()

                Debug.log("onTextMessage: proof ($proof)", DebugPriority.DEBUG, TAG)

                websocket.sendText("{\"op\": \"nonce_proof\", \"proof\": \"$proof\"}")
            }
            "pending_remote_init" -> {
                listener.onFingerprintReady(data.getString("fingerprint"))
            }
            "pending_finish" -> {
                val userDataString = String(
                    decrypt(data.getString("encrypted_user_payload")),
                    charset("UTF-8")
                )

                Debug.log(
                    "onTextMessage: User info payload ($userDataString)",
                    DebugPriority.DEBUG, TAG
                )

                try {
                    user = LoginUser(userDataString)
                } catch(e: Exception) {
                    Debug.log("onTextMessage: ${e.message}", DebugPriority.ERROR, TAG)
                }

                Debug.log("""
                    onTextMessage: User data incoming
                        $user
                """.trimIndent(), DebugPriority.DEBUG, TAG)

                listener.onUserScan(user!!)
            }
            "cancel" -> {
                listener.onUserCancel()
                user = null
            }
            "finish" -> {
                user?.token =
                    String(
                        decrypt(data.getString("encrypted_token")),
                        charset("UTF-8")
                    )
                Debug.log("Auth successful <.< $user", DebugPriority.DEBUG, TAG)

                listener.onFinishLogin(user!!)
            }
            else -> {}
        }

        super.onTextMessage(websocket, text)
    }

    override fun onError(websocket: WebSocket?, cause: WebSocketException?) {
        Debug.log("onError: $cause", DebugPriority.ERROR, TAG)
        if(websocket == null || !websocket.isOpen) startServer()
        super.onError(websocket, cause)
    }

    override fun onDisconnected(
        websocket: WebSocket?,
        serverCloseFrame: WebSocketFrame?,
        clientCloseFrame: WebSocketFrame?,
        closedByServer: Boolean
    ) {
        Debug.log(
            "WebSocket disconnected. Cause: ${if (closedByServer) "server" else "network error"}",
            DebugPriority.INFO, TAG
        )
        if(user?.token.isNullOrEmpty()) startServer()
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
    }

    companion object {
        private const val TAG = "QrAuth"
    }
}