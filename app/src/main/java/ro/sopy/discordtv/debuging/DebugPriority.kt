package ro.sopy.discordtv.debuging

enum class DebugPriority {
    CRITICAL,
    ERROR,
    WARNING,
    INFO,
    DEBUG,
    NO_PRIORITY;

    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.firstOrNull { it as Int == value }
    }
}