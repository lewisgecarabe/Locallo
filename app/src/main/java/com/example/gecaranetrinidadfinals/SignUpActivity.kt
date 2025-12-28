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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signupScrollView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtName: EditText = findViewById(R.id.edtTxt_Name_LG)
        val edtEmail: EditText = findViewById(R.id.edtTxt_Email_LG)
        val edtPassword: EditText = findViewById(R.id.edtTxt_Pass_LG)
        val btnSubmit: Button = findViewById(R.id.btn_sbmt_LG)
        val signInLink: TextView = findViewById(R.id.signin_link)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // 🔹 Create Account
        btnSubmit.setOnClickListener {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser!!.uid

                    // ✅ tbl_users schema (matches your requirement)
                    val userData = hashMapOf(
                        "user_id" to uid,
                        "name" to name,
                        "email" to email,
                        "profile_picture" to "",
                        "travel_history" to arrayListOf<String>()
                    )

                    db.collection("tbl_users").document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this, LogInActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }

        // 🔹 Go to Login Page
        signInLink.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }
    }
}