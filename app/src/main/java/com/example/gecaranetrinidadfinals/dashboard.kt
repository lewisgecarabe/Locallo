package com.example.gecaranetrinidadfinals

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class dashboard : AppCompatActivity() {

    private lateinit var postContainer: LinearLayout
    private val db = FirebaseFirestore.getInstance()
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 UI
        postContainer = findViewById(R.id.post_container)

        val btnLogout: Button = findViewById(R.id.btn_logout)
        val txtUsername: TextView = findViewById(R.id.txt_username)
        val txtEmail: TextView = findViewById(R.id.txtEmail)
        val imgProfile: ImageView = findViewById(R.id.img_profile)

        val btnAll: TextView = findViewById(R.id.btn_all)
        val btnFood: TextView = findViewById(R.id.btn_food)
        val btnHotel: TextView = findViewById(R.id.btn_hotels)
        val btnAdventure: TextView = findViewById(R.id.btn_adventure)
        val btnLocation: TextView = findViewById(R.id.btn_location)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val email = auth.currentUser?.email ?: ""

        // 🔹 LOAD USER INFO
        db.collection("tbl_users").document(userId).get()
            .addOnSuccessListener { user ->
                txtUsername.text = user.getString("name") ?: "User"
                txtEmail.text = email

                val avatar = user.getString("profile_picture") ?: ""
                if (avatar.isNotEmpty()) {
                    Glide.with(this).load(avatar).into(imgProfile)
                }
            }

        // 🔹 LOGOUT
        btnLogout.setOnClickListener {
            auth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build()
            GoogleSignIn.getClient(this, gso).signOut()

            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }

        // 🔹 INITIAL LOAD (ALL)
        loadPosts()

        // 🔹 CATEGORY BUTTONS
        btnAll.setOnClickListener {
            currentCategory = null
            loadPosts()
        }

        btnFood.setOnClickListener {
            currentCategory = "Food"
            loadPosts()
        }

        btnHotel.setOnClickListener {
            currentCategory = "Hotel"
            loadPosts()
        }

        btnAdventure.setOnClickListener {
            currentCategory = "Adventure"
            loadPosts()
        }

        btnLocation.setOnClickListener {
            currentCategory = "Location"
            loadPosts()
        }

        // 🔹 BOTTOM NAV
        findViewById<ImageView>(R.id.nav_home)
            .setOnClickListener { startActivity(Intent(this, dashboard::class.java)) }

        findViewById<ImageView>(R.id.nav_explore)
            .setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        findViewById<ImageView>(R.id.nav_docs)
            .setOnClickListener { startActivity(Intent(this, MainActivity2::class.java)) }

        findViewById<ImageView>(R.id.nav_profile)
            .setOnClickListener { startActivity(Intent(this, profile::class.java)) }
    }

    // 🔹 LOAD POSTS (CLIENT-SIDE FILTERING)
    private fun loadPosts() {

        postContainer.removeAllViews()

        db.collection("tbl_posts")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { records ->

                for (doc in records) {

                    val postCategory = doc.getString("category") ?: ""
                    if (currentCategory != null && postCategory != currentCategory) continue

                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.activity_main3, postContainer, false)

                    val txtContent = view.findViewById<TextView>(R.id.txt_description)
                    val txtUser = view.findViewById<TextView>(R.id.txt_post_username)
                    val txtLikes = view.findViewById<TextView>(R.id.txt_like_count)
                    val txtComments = view.findViewById<TextView>(R.id.txt_comment_count)
                    val txtLocation = view.findViewById<TextView>(R.id.txt_post_location)
                    val txtCategory = view.findViewById<TextView>(R.id.txt_post_category)
                    val imgPost = view.findViewById<ImageView>(R.id.img_post_main)
                    val imgAvatar = view.findViewById<ImageView>(R.id.img_avatar)
                    val imgLike = view.findViewById<ImageView>(R.id.btn_like)

                    val postId = doc.id
                    val userId = doc.getString("user_id") ?: ""
                    val imageUrl = doc.getString("image_url") ?: ""

                    txtContent.text = doc.getString("content") ?: ""
                    txtLikes.text = (doc.getLong("likes_count") ?: 0).toString()
                    txtComments.text = (doc.getLong("comments_count") ?: 0).toString()
                    txtLocation.text = doc.getString("location") ?: ""
                    txtCategory.text = postCategory

                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this).load(imageUrl).into(imgPost)
                    } else {
                        imgPost.visibility = View.GONE
                    }

                    // 🔹 USER DATA
                    if (userId.isNotEmpty()) {
                        db.collection("tbl_users").document(userId).get()
                            .addOnSuccessListener { user ->
                                txtUser.text = user.getString("name") ?: "User"
                                val avatar = user.getString("profile_picture") ?: ""
                                if (avatar.isNotEmpty()) {
                                    Glide.with(this).load(avatar).into(imgAvatar)
                                }
                            }
                    }

                    // 🔹 LIKE LOGIC
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val likedBy =
                        doc.get("liked_by") as? ArrayList<String> ?: arrayListOf()

                    imgLike.setImageResource(
                        if (likedBy.contains(currentUserId))
                            R.drawable.after_like
                        else
                            R.drawable.baseline_thumb_up_24
                    )

                    imgLike.setOnClickListener {
                        val postRef = db.collection("tbl_posts").document(postId)

                        if (likedBy.contains(currentUserId)) {
                            postRef.update(
                                "liked_by", FieldValue.arrayRemove(currentUserId),
                                "likes_count", FieldValue.increment(-1)
                            )
                            likedBy.remove(currentUserId)
                            txtLikes.text = (txtLikes.text.toString().toInt() - 1).toString()
                            imgLike.setImageResource(R.drawable.baseline_thumb_up_24)
                        } else {
                            postRef.update(
                                "liked_by", FieldValue.arrayUnion(currentUserId),
                                "likes_count", FieldValue.increment(1)
                            )
                            likedBy.add(currentUserId)
                            txtLikes.text = (txtLikes.text.toString().toInt() + 1).toString()
                            imgLike.setImageResource(R.drawable.after_like)
                        }
                    }

                    // 🔹 DETAILS & COMMENTS
                    view.findViewById<ImageView>(R.id.btn_post_menu).setOnClickListener {
                        val intent = Intent(this, postdetail::class.java)
                        intent.putExtra("post_id", postId)
                        startActivity(intent)
                    }

                    view.findViewById<ImageView>(R.id.btn_comment).setOnClickListener {
                        val intent = Intent(this, CommentsActivity::class.java)
                        intent.putExtra("post_id", postId)
                        startActivity(intent)
                    }

                    postContainer.addView(view)
                }
            }
    }
}
