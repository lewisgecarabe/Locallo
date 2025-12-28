package com.example.gecaranetrinidadfinals

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.result.contract.ActivityResultContracts

class LogInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.log_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginScrollView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

                val edtPassword: EditText = findViewById(R.id.edtTxt_passLogin_LG)
                val edtEmail: EditText = findViewById(R.id.edtTxt_emailLogIN_LG)
                val btnSubmit: Button = findViewById(R.id.btn_sbmt_LG)
                val btnGoogle: Button = findViewById(R.id.btn_google_LG)
                val signUpLink: TextView = findViewById(R.id.don_t_have_)

                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()

                // 🔹 GOOGLE SIGN-IN CONFIG
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val googleClient = GoogleSignIn.getClient(this, gso)

                // 🔹 GOOGLE SIGN-IN RESULT HANDLER
                val googleLauncher = registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->

                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                    try {
                        val account = task.getResult(ApiException::class.java)
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                        auth.signInWithCredential(credential).addOnSuccessListener {
                            val user = auth.currentUser!!
                            val uid = user.uid

                            // ✅ MATCH tbl_users SCHEMA
                            val userData = mapOf(
                                "user_id" to uid,
                                "name" to (user.displayName ?: "Google User"),
                                "profile_picture" to (user.photoUrl?.toString() ?: ""),
                                "travel_history" to arrayListOf<String>()
                            )

                            db.collection("tbl_users").document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, dashboard::class.java))
                                    finish()
                                }
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }

                // 🔹 GOOGLE LOGIN BUTTON
                btnGoogle.setOnClickListener {
                    googleLauncher.launch(googleClient.signInIntent)
                }

                // 🔹 EMAIL/PASSWORD LOGIN
                btnSubmit.setOnClickListener {
                    val email = edtEmail.text.toString().trim()
                    val pass = edtPassword.text.toString().trim()

                    if (email.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, dashboard::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }

                // 🔹 GO TO SIGN UP
                signUpLink.setOnClickListener {
                    startActivity(Intent(this, SignUpActivity::class.java))
                }
            }
        }