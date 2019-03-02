package dev.pushkar.goconso

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_info.*

class Info : Activity() {

    val updates = mutableListOf<Update>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        recycler_view.layoutManager = LinearLayoutManager(this)
        val query = FirebaseFirestore.getInstance()
            .collection("Info")
            .orderBy("pri")
        query.get().addOnSuccessListener {
            it.forEach { doc ->
                updates.add(doc.toObject(Update::class.java))
                Toast.makeText(this, "${doc.toObject(Update::class.java).pri}", Toast.LENGTH_LONG).show()
            }
            val adapter = PhotoAdapter(updates, this)
            recycler_view.adapter = adapter
            adapter.notifyDataSetChanged()
            recycler_view.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onBackPressed(){
        startActivity(Intent(this,MainActivity::class.java))
    }
}
