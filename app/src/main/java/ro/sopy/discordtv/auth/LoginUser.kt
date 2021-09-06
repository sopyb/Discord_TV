package ro.sopy.discordtv.auth

import ro.sopy.discordtv.debuging.Debug

class LoginUser(decryptedUserPayload: String) {
    val userId: String
    val discriminator: String
    val avatarHash: String
    val username: String
    var token: String = ""

    init {
        if (decryptedUserPayload.matches(payloadRegex)) {
            val splits = decryptedUserPayload.split(":".toRegex(), 0)

            userId = splits[0]
            discriminator = splits[1]
            avatarHash = splits[2]
            username = splits[3]
        } else throw IllegalArgumentException("Received user payload was incorrectly formatted: $decryptedUserPayload")
    }

    override fun toString(): String {
        return """
            User: $username#$discriminator
            Profile picture: https://cdn.discordapp.com/avatars/$userId/$avatarHash.png
            [-] ID: $userId
            [-] Avatar hash: $avatarHash
            
            SECRET TOKEN: ${if(token.isNotEmpty()) token.dropLast(30)+"******************************" else "login not finished"} }
        """.trimIndent()
    }

    companion object {
        private val payloadRegex = "[0-9]+:[0-9]+:([0-9]+[a-zA-Z]+)+:[a-zA-Z]+".toRegex()
        private const val TAG = "LoginUser"
    }
}