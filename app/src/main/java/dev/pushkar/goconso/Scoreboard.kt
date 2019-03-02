package dev.pushkar.goconso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_scoreboard.*

class Scoreboard : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var dailyAdapter: FirestoreRecyclerAdapter<HighScore,ScoreHolder>
    private lateinit var allTimeAdapter: FirestoreRecyclerAdapter<HighScore,ScoreHolder>

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.text = getString(R.string.ds)
                daily.visibility= View.GONE
                all_time.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                daily.visibility= View.VISIBLE
                all_time.visibility = View.GONE
                message.text = getString(R.string.ats)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoreboard)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        db = FirebaseFirestore.getInstance()
        message.text = getString(R.string.ds)
        daily.visibility= View.GONE
        all_time.visibility = View.VISIBLE
        init()
    }

    fun init() {
        val dailyQuery: Query = db
            .collection("Daily")
            .orderBy("score", Query.Direction.DESCENDING)

        val allTimeQuery: Query = db
            .collection("AllTime")
            .orderBy("score", Query.Direction.DESCENDING)

        val dailyOptions = FirestoreRecyclerOptions
            .Builder<HighScore>()
            .setQuery(dailyQuery, HighScore::class.java).build()

        val allTimeOptions = FirestoreRecyclerOptions
            .Builder<HighScore>()
            .setQuery(allTimeQuery, HighScore::class.java).build()

        dailyAdapter = object: FirestoreRecyclerAdapter<HighScore, ScoreHolder>(dailyOptions){
            override fun onBindViewHolder(p0: ScoreHolder, p1: Int, p2: HighScore) {
                p0.name.text = getString(R.string.bd_name, p1+1, p2.uid)
                p0.score.text = getString(R.string.bd_score, p2.score)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false) as CardView
                return ScoreHolder(view)
            }
        }

        allTimeAdapter = object: FirestoreRecyclerAdapter<HighScore, ScoreHolder>(allTimeOptions){
            override fun onBindViewHolder(p0: ScoreHolder, p1: Int, p2: HighScore) {
                p0.name.text = getString(R.string.bd_name, p1+1, p2.uid)
                p0.score.text = getString(R.string.bd_score, p2.score)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false) as CardView
                return ScoreHolder(view)
            }

        }

        daily.layoutManager = LinearLayoutManager(this)
        daily.adapter = allTimeAdapter
        daily.hasFixedSize()

        all_time.layoutManager = LinearLayoutManager(this)
        all_time.adapter = dailyAdapter
        all_time.hasFixedSize()
    }

    inner class ScoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var score: TextView = itemView.findViewById(R.id.score)
    }

    override fun onStart() {
        super.onStart()
        dailyAdapter.startListening()
        allTimeAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        dailyAdapter.stopListening()
        allTimeAdapter.stopListening()
    }



}
