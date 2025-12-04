package com.example.gecaranetrinidadfinals

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogOut : Button = findViewById(R.id.btn_logout)
        val usernameTxt : TextView = findViewById(R.id.txt_username)
        val emailTxt : TextView = findViewById(R.id.txtEmail)

        val auth = FirebaseAuth.getInstance()
        val con = FirebaseFirestore.getInstance()

        btnLogOut.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}