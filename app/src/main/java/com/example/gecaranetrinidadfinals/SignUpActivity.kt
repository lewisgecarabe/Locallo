package com.example.gecaranetrinidadfinals

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtName : EditText = findViewById(R.id.edtTxt_Name_LG)
        val edtPassword : EditText = findViewById(R.id.edtTxt_Pass_LG)
        val edtEmail: EditText = findViewById(R.id.edtTxt_Email_LG)
        val btnSubmit: Button = findViewById(R.id.btn_sbmt_LG)

        val con = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        btnSubmit.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val pass = edtPassword.text.toString()

            auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener {

                val uid = auth.currentUser!!.uid
                val values =  mapOf(
                    "name" to name,
                    "email" to email
                )

                con.collection("tbl_users").document(uid).set(values).addOnSuccessListener {
                    Toast.makeText(this, "account created", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                }
            }
        }

    }
}