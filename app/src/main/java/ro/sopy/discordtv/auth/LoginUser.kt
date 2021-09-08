package ro.sopy.discordtv.auth

class LoginUser(decryptedUserPayload: String) {
    private val userId: String
    val discriminator: String
    private val avatarHash: String
    val username: String
    val avatarUrl: String
    var token: String = ""

    init {
        if (decryptedUserPayload.matches(payloadRegex)) {
            val splits = decryptedUserPayload.split(":".toRegex(), 0)

            userId = splits[0]
            discriminator = splits[1]
            avatarHash = splits[2]
            username = splits[3]

            avatarUrl = if (avatarHash == "0")
                "https://cdn.discordapp.com/embed/avatars/${discriminator.takeLast(1).toInt() % 5}.png"
            else "https://cdn.discordapp.com/avatars/$userId/$avatarHash.png?size=256"
        } else throw IllegalArgumentException("Received user payload was incorrectly formatted: $decryptedUserPayload")
    }

    override fun toString(): String {
        return """
            User: $username#$discriminator
            Profile picture: $avatarUrl
            [-] ID: $userId
            [-] Avatar hash: $avatarHash
            
            SECRET TOKEN: ${if(token.isNotEmpty()) token.dropLast(30)+"******************************" else "login not finished"} }
        """.trimIndent()
    }

    companion object {
        private val payloadRegex = "[0-9]+:[0-9]+:[0-9a-zA-Z]+:[a-zA-Z]+".toRegex()
    }
}