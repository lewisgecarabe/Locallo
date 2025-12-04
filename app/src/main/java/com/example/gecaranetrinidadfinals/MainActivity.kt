package com.example.gecaranetrinidadfinals

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtPost: EditText = findViewById(R.id.edtPost)
        val btnPost: Button = findViewById(R.id.btn_post)

        val con = FirebaseFirestore.getInstance()

        btnPost.setOnClickListener {
            val userPost = edtPost.text.toString()

            //"field" : value
            val values = mapOf(
                "userid_LG" to "235424242fff",
                "content_LG" to userPost,
                "nooflikes_LG" to 0,
                "noofcomments_LG" to 0,
                "createDate_LG" to FieldValue.serverTimestamp()
            )

            con.collection("tbl_posts").add(values).addOnSuccessListener {
                Toast.makeText(this, "New Post Created", Toast.LENGTH_SHORT).show()
            }


        }
    }
}