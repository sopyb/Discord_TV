package ro.sopy.discordtv.debuging

import android.util.Log
import io.reactivex.rxjava3.subjects.BehaviorSubject

class Debug {
    companion object {
        private val _entryList = ArrayList<DebugEntry>()
        val entryList : BehaviorSubject<ArrayList<DebugEntry>> = BehaviorSubject.createDefault(
            _entryList)

        init {
            entryList.doOnError {
                log(it.message!!, DebugPriority.ERROR, "Debug")
            }
        }

        fun log(message: String,
                priority: DebugPriority = DebugPriority.NO_PRIORITY,
                title: String = "UndefinedClass") {

            //ADB can't handle titles longer than 23 characters
            val formattedTitle = title.take(23)

            _entryList.add(DebugEntry(message, priority, title))
            entryList.onNext(_entryList)

            when {
                priority <= DebugPriority.ERROR -> Log.e(formattedTitle, message)
                priority <= DebugPriority.DEBUG -> Log.d(formattedTitle, message)
            }
        }
    }
}