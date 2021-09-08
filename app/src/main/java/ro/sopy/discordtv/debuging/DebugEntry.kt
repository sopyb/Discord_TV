package ro.sopy.discordtv.debuging

data class DebugEntry(private val _message: String, val priority: DebugPriority, val title: String) {
    val message: String = ".$title / $_message"
}