package ro.sopy.discordtv.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.sopy.discordtv.R
import ro.sopy.discordtv.adapters.DebugRecyclerViewAdapter
import ro.sopy.discordtv.debuging.Debug
import ro.sopy.discordtv.debuging.DebugPriority

class DebugActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {
    private lateinit var radioGroup: RadioGroup
    private lateinit var recyclerView: RecyclerView
    private val adapter = DebugRecyclerViewAdapter(DebugPriority.INFO)

    /**
     * Activity callbacks overwrites
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        radioGroup = findViewById(R.id.DebugLevel)
        recyclerView = findViewById(R.id.LogRecyclerView)

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm
        recyclerView.adapter = this.adapter
        radioGroup.setOnCheckedChangeListener(this)
    }

    /**
     * OnCheckedChangeListener callback overwrites
     */
    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.noPriorityRadioButton -> adapter.changePriority(DebugPriority.NO_PRIORITY)
            R.id.debugPriorityRadioButton -> adapter.changePriority(DebugPriority.DEBUG)
            R.id.infoPriorityRadioButton -> adapter.changePriority(DebugPriority.INFO)
            R.id.warningPriorityRadioButton -> adapter.changePriority(DebugPriority.WARNING)
            R.id.errorPriorityRadioButton ->  adapter.changePriority(DebugPriority.ERROR)
            R.id.criticalPriorityRadioButton -> adapter.changePriority(DebugPriority.CRITICAL)
            else -> throw IllegalArgumentException("Unknown button pressed")
        }

//        radioGroup.check(checkedId)
    }


}