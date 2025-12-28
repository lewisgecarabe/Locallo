package com.example.gecaranetrinidadfinals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // 🔹 Supabase config (REPLACE WITH YOUR OWN)
    private val SUPABASE_URL = "https://gsrqylctqiagntjviomj.supabase.co"
    private val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzcnF5bGN0cWlhZ250anZpb21qIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjY4MzY2NzksImV4cCI6MjA4MjQxMjY3OX0.wmWVpsMO3fZEG5ODLBG9ZiMuqNYsuX4LXfVqMxewEhQ"
    private val BUCKET_NAME = "post-img"

    private var selectedImageUri: Uri? = null
    private lateinit var cameraImageUri: Uri

    // Gallery picker
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                showPreview(it)
            }
        }

    // Camera picker (FileProvider)
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                selectedImageUri = cameraImageUri
                showPreview(cameraImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtPost: EditText = findViewById(R.id.edtPost_LG)
        val btnPost: Button = findViewById(R.id.btn_post_LG)
        val btnImage: ImageView = findViewById(R.id.btnImage)
        val imgPreview: ImageView = findViewById(R.id.imgPreview)

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val spinnerCategory: Spinner = findViewById(R.id.spinner_category)
        val edtLocation: EditText = findViewById(R.id.edt_location)

        val categories = arrayOf("Location", "Hotels", "Food", "Adventure")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategory.adapter = adapter



        // Image picker dialog
        btnImage.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(arrayOf("Camera", "Gallery")) { _, which ->
                    if (which == 0) openCamera()
                    else galleryLauncher.launch("image/*")
                }
                .show()
        }

        // Post button
        btnPost.setOnClickListener {
            val content = edtPost.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val location = edtLocation.text.toString().trim()

            if (location.isEmpty()) {
                Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show()
                btnPost.isEnabled = true
                return@setOnClickListener
            }

            if (content.isEmpty()) {
                Toast.makeText(this, "Post cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPost.isEnabled = false
            val postRef = db.collection("tbl_posts").document()
            val postId = postRef.id

            if (selectedImageUri != null) {
                val file = File(cacheDir, "upload_$postId.jpg")
                contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }

                uploadToSupabase(file, postId) { imageUrl ->
                    savePost(postRef, userId, content, imageUrl, category, location)
                    resetUI(edtPost, imgPreview, btnPost)
                }

            } else {
                savePost(postRef, userId, content, "", category, location)
                resetUI(edtPost, imgPreview, btnPost)
            }
        }

        // Bottom navigation
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, dashboard::class.java))
        }
        findViewById<ImageView>(R.id.nav_docs).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, profile::class.java))
        }
    }

    // Camera using FileProvider
    private fun openCamera() {
        val imageFile = File.createTempFile("camera_", ".jpg", cacheDir)
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            imageFile
        )
        cameraLauncher.launch(cameraImageUri)
    }

    // Supabase upload
    private fun uploadToSupabase(
        file: File,
        postId: String,
        onSuccess: (String) -> Unit
    ) {
        val client = OkHttpClient()

        val mediaType = "image/jpeg".toMediaType()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "$postId.jpg",
                file.asRequestBody(mediaType)
            )
            .build()

        val request = Request.Builder()
            .url("$SUPABASE_URL/storage/v1/object/$BUCKET_NAME/$postId.jpg")
            .addHeader("apikey", SUPABASE_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_KEY")
            .addHeader("x-upsert", "true")
            .post(body) // 🔥 POST, not PUT
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val publicUrl =
                        "$SUPABASE_URL/storage/v1/object/public/$BUCKET_NAME/$postId.jpg"

                    runOnUiThread {
                        onSuccess(publicUrl)
                    }
                } else {
                    val error = response.body?.string()
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Supabase ${response.code}: $error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }


    private fun savePost(
        postRef: com.google.firebase.firestore.DocumentReference,
        userId: String,
        content: String,
        imageUrl: String,
        category: String,
        location: String

    ) {
        val data = mapOf(
            "post_id" to postRef.id,
            "user_id" to userId,
            "content" to content,
            "image_url" to imageUrl,
            "category" to category,
            "location" to location,
            "likes_count" to 0,
            "liked_by" to arrayListOf<String>(),
            "comments_count" to 0,
            "timestamp" to FieldValue.serverTimestamp()
        )


        postRef.set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPreview(uri: Uri) {
        val imgPreview: ImageView = findViewById(R.id.imgPreview)
        imgPreview.setImageURI(uri)
        imgPreview.visibility = ImageView.VISIBLE
    }

    private fun resetUI(
        edtPost: EditText,
        imgPreview: ImageView,
        btnPost: Button
    ) {
        edtPost.text.clear()
        imgPreview.visibility = ImageView.GONE
        selectedImageUri = null
        btnPost.isEnabled = true
    }
}
