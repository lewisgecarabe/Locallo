package com.example.gecaranetrinidadfinals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class profile : AppCompatActivity() {

    // 🔹 SUPABASE CONFIG
    private val SUPABASE_URL = "https://gsrqylctqiagntjviomj.supabase.co"
    private val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzcnF5bGN0cWlhZ250anZpb21qIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjY4MzY2NzksImV4cCI6MjA4MjQxMjY3OX0.wmWVpsMO3fZEG5ODLBG9ZiMuqNYsuX4LXfVqMxewEhQ"
    private val BUCKET_NAME = "post-img"

    private lateinit var imgProfile: ImageView

    // 🔹 IMAGE PICKER
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uploadProfileImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        // 🔹 UI BINDING
        imgProfile = findViewById(R.id.img_profile)
        val txtName: TextView = findViewById(R.id.txt_username)
        val txtPostCount: TextView = findViewById(R.id.txt_post_count)
        val txtLikeCount: TextView = findViewById(R.id.txt_like_count)
        val postContainer: LinearLayout = findViewById(R.id.profile_post_container)
        val btnEdit: Button = findViewById(R.id.btn_edit_profile)

        // 🔹 LOAD USER INFO
        db.collection("tbl_users").document(userId).get()
            .addOnSuccessListener { user ->
                txtName.text = user.getString("name") ?: "User"

                val profilePic = user.getString("profile_picture") ?: ""
                if (profilePic.isNotEmpty()) {
                    Glide.with(this).load(profilePic).into(imgProfile)
                }
            }

        // 🔹 EDIT PROFILE
        btnEdit.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // 🔹 LOAD USER POSTS (TRAVEL HISTORY)
        db.collection("tbl_posts")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { records ->

                txtPostCount.text = records.size().toString()
                postContainer.removeAllViews()

                var totalLikes = 0L

                for (doc in records) {
                    totalLikes += doc.getLong("likes_count") ?: 0L

                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.activity_main3, postContainer, false)

                    // 🔹 POST DATA
                    view.findViewById<TextView>(R.id.txt_description)
                        .text = doc.getString("content") ?: ""

                    view.findViewById<TextView>(R.id.txt_post_username)
                        .text = txtName.text

                    view.findViewById<TextView>(R.id.txt_like_count)
                        .text = (doc.getLong("likes_count") ?: 0).toString()

                    view.findViewById<TextView>(R.id.txt_comment_count)
                        .text = (doc.getLong("comments_count") ?: 0).toString()

                    view.findViewById<TextView>(R.id.txt_post_location)
                        .text = doc.getString("location") ?: ""

                    view.findViewById<TextView>(R.id.txt_post_category)
                        .text = doc.getString("category") ?: ""

                    // 🔹 AVATAR
                    val avatar = view.findViewById<ImageView>(R.id.img_avatar)
                    Glide.with(this).load(imgProfile.drawable).into(avatar)

                    // 🔹 POST IMAGE
                    val imgPost = view.findViewById<ImageView>(R.id.img_post_main)
                    val imageUrl = doc.getString("image_url") ?: ""
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this).load(imageUrl).into(imgPost)
                    } else {
                        imgPost.visibility = ImageView.GONE
                    }

                    postContainer.addView(view)
                }

                txtLikeCount.text = totalLikes.toString()
            }

        // 🔹 BOTTOM NAV
        findViewById<ImageView>(R.id.nav_home)
            .setOnClickListener { startActivity(Intent(this, dashboard::class.java)) }

        findViewById<ImageView>(R.id.nav_explore)
            .setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        findViewById<ImageView>(R.id.nav_docs)
            .setOnClickListener { startActivity(Intent(this, MainActivity2::class.java)) }
    }

    // 🔹 UPLOAD PROFILE IMAGE
    private fun uploadProfileImage(uri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val file = File(cacheDir, "profile_$userId.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }

        val requestBody = file.asRequestBody("image/jpeg".toMediaType())

        val request = Request.Builder()
            .url("$SUPABASE_URL/storage/v1/object/$BUCKET_NAME/profile_$userId.jpg")
            .addHeader("apikey", SUPABASE_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_KEY")
            .addHeader("x-upsert", "true")
            .put(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@profile, "Upload failed", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val publicUrl =
                        "$SUPABASE_URL/storage/v1/object/public/$BUCKET_NAME/profile_$userId.jpg"

                    db.collection("tbl_users").document(userId)
                        .update("profile_picture", publicUrl)

                    runOnUiThread {
                        Glide.with(this@profile).load(publicUrl).into(imgProfile)
                        Toast.makeText(this@profile, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
