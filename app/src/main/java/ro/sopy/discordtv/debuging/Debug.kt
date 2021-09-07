package ro.sopy.discordtv.debuging

import android.util.Log

class Debug {
    companion object {
        private val entryList = ArrayList<DebugEntry>()

        fun log(message: String,
                priority: DebugPriority = DebugPriority.NO_PRIORITY,
                title: String = "UndefinedClass") {
            entryList.add(DebugEntry(message, priority, title))

            //ADB can't handle titles longer than 23 characters
            val formattedTitle = title.take(23)

            when {
                priority <= DebugPriority.ERROR -> Log.e(formattedTitle, message)
                priority <= DebugPriority.DEBUG -> Log.d(formattedTitle, message)
            }
        }

        fun getEntries(minPriority: DebugPriority = DebugPriority.INFO): List<DebugEntry> {
            return entryList.filter{
                it.priority <= minPriority
            }
        }
    }
}