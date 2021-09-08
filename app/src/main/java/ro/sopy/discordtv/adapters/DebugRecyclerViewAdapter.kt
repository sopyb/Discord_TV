package ro.sopy.discordtv.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ro.sopy.discordtv.R
import ro.sopy.discordtv.debuging.Debug
import ro.sopy.discordtv.debuging.DebugEntry
import ro.sopy.discordtv.debuging.DebugPriority


class DebugRecyclerViewAdapter(private var minPriority: DebugPriority): RecyclerView.Adapter<DebugRecyclerViewAdapter.Companion.DebugViewHolder>() {
    private var cachedData = Debug.entryList.value!!.reversed()
    private var currentData = cachedData.filter { it.priority <= minPriority }

    init {
        Debug.entryList.subscribe {
            Handler(Looper.getMainLooper()).post {
                cachedData = it.reversed()

                updateData()
            }
        }
    }

    private fun updateData() {
        currentData = cachedData.filter { it.priority <= minPriority }

        this.notifyDataSetChanged()
    }

    fun changePriority(minPriority: DebugPriority) {
        this.minPriority = minPriority

        updateData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebugViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DebugViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: DebugViewHolder, position: Int) {
        holder.bind(currentData[position])
    }

    override fun getItemCount(): Int {
        return currentData.size
    }

    companion object {
        class DebugViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
            RecyclerView.ViewHolder(inflater.inflate(R.layout.debug_log_entry, parent, false)) {
            private var logEntryTextView: TextView = itemView.findViewById(R.id.EntryMessage)

            fun bind(entry: DebugEntry) {
                logEntryTextView.text = entry.message
                logEntryTextView.setTextColor(
                    when(entry.priority){
                        DebugPriority.CRITICAL -> ContextCompat.getColor(itemView.context, R.color.critical_debug)
                        DebugPriority.ERROR -> ContextCompat.getColor(itemView.context, R.color.error_debug)
                        DebugPriority.WARNING -> ContextCompat.getColor(itemView.context, R.color.warning_debug)
                        DebugPriority.INFO -> ContextCompat.getColor(itemView.context, R.color.info_debug)
                        DebugPriority.DEBUG -> ContextCompat.getColor(itemView.context, R.color.debug_debug)
                        else -> ContextCompat.getColor(itemView.context, R.color.none_debug)
                    }
                )
            }
        }
    }
}