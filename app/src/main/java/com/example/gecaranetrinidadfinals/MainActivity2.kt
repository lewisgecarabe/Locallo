package com.example.gecaranetrinidadfinals

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val vlayout: LinearLayout = findViewById(R.id.linearLayout_LG)

        //start of Database
        val conn = FirebaseFirestore.getInstance()
        conn.collection("tbl_posts").get().addOnSuccessListener {
            records ->

            for (record in records){
                val inflater = LayoutInflater.from(this)
                val template = inflater.inflate(R.layout.activity_main3,vlayout,false)

                //textview from cardview

                val txtContent: TextView = template.findViewById(R.id.txtContent_LG)

                //field values
                val content = record.getString("content_LG")

                //display values
                txtContent.text = content

                vlayout.addView(template)
            }
        }


    }
}