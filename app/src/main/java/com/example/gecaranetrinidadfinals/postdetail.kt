package com.example.gecaranetrinidadfinals

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class postdetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postdetail)

        val postId = intent.getStringExtra("post_id") ?: return
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // 🔹 UI
        val imgMain: ImageView = findViewById(R.id.img_main)
        val imgAvatar: ImageView = findViewById(R.id.img_avatar)
        val txtUsername: TextView = findViewById(R.id.txt_username)
        val txtLocation: TextView = findViewById(R.id.txt_location)
        val txtCategory: TextView = findViewById(R.id.txt_category)
        val txtDescription: TextView = findViewById(R.id.txt_description)
        val txtLikeCount: TextView = findViewById(R.id.txt_like_count)

        val btnLike: ImageView = findViewById(R.id.btn_like)
        val btnComment: ImageView = findViewById(R.id.btn_comment)
        val btnBack: ImageButton = findViewById(R.id.btn_back)

        btnBack.setOnClickListener { finish() }

        // 🔹 LOAD POST
        db.collection("tbl_posts").document(postId).get()
            .addOnSuccessListener { post ->

                if (!post.exists()) return@addOnSuccessListener

                val content = post.getString("content") ?: ""
                val imageUrl = post.getString("image_url") ?: ""
                val userId = post.getString("user_id") ?: ""
                val location = post.getString("location") ?: ""
                val category = post.getString("category") ?: ""
                val likesCount = post.getLong("likes_count") ?: 0L
                val likedBy =
                    post.get("liked_by") as? ArrayList<String> ?: arrayListOf()

                txtDescription.text = content
                txtLocation.text = location
                txtCategory.text = category
                txtLikeCount.text = likesCount.toString()

                if (imageUrl.isNotEmpty()) {
                    Glide.with(this).load(imageUrl).into(imgMain)
                }

                // 🔹 LOAD USER INFO
                if (userId.isNotEmpty()) {
                    db.collection("tbl_users").document(userId).get()
                        .addOnSuccessListener { user ->
                            txtUsername.text = user.getString("name") ?: "Traveler"
                            val avatar = user.getString("profile_picture") ?: ""
                            if (avatar.isNotEmpty()) {
                                Glide.with(this).load(avatar).into(imgAvatar)
                            }
                        }
                }

                // 🔹 INITIAL LIKE STATE
                btnLike.setImageResource(
                    if (likedBy.contains(currentUserId))
                        R.drawable.after_like
                    else
                        R.drawable.baseline_thumb_up_24
                )

                // 🔹 LIKE / UNLIKE
                btnLike.setOnClickListener {

                    if (currentUserId.isEmpty()) return@setOnClickListener

                    val postRef = db.collection("tbl_posts").document(postId)

                    if (likedBy.contains(currentUserId)) {

                        postRef.update(
                            "liked_by", FieldValue.arrayRemove(currentUserId),
                            "likes_count", FieldValue.increment(-1)
                        )

                        likedBy.remove(currentUserId)
                        btnLike.setImageResource(R.drawable.baseline_thumb_up_24)
                        txtLikeCount.text =
                            (txtLikeCount.text.toString().toInt() - 1).toString()

                    } else {

                        postRef.update(
                            "liked_by", FieldValue.arrayUnion(currentUserId),
                            "likes_count", FieldValue.increment(1)
                        )

                        likedBy.add(currentUserId)
                        btnLike.setImageResource(R.drawable.after_like)
                        txtLikeCount.text =
                            (txtLikeCount.text.toString().toInt() + 1).toString()
                    }
                }

                // 🔹 OPEN COMMENTS
                btnComment.setOnClickListener {
                    val intent = Intent(this, CommentsActivity::class.java)
                    intent.putExtra("post_id", postId)
                    startActivity(intent)
                }
            }

        loadComments(postId)

    }

    private fun loadComments(postId: String) {

        val db = FirebaseFirestore.getInstance()
        val commentContainer: LinearLayout = findViewById(R.id.comment_container)

        commentContainer.removeAllViews()

        db.collection("tbl_comments")          // ✅ TOP-LEVEL TABLE
            .whereEqualTo("post_id", postId)   // ✅ LINK TO POST
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { records ->

                for (doc in records) {

                    val view = layoutInflater.inflate(
                        R.layout.item_comment,
                        commentContainer,
                        false
                    )

                    val txtName = view.findViewById<TextView>(R.id.txt_comment_user)
                    val txtMessage = view.findViewById<TextView>(R.id.txt_comment_content)
                    val imgAvatar = view.findViewById<ImageView>(R.id.img_comment_avatar)

                    txtMessage.text = doc.getString("content") ?: ""

                    val userId = doc.getString("user_id") ?: ""

                    if (userId.isNotEmpty()) {
                        db.collection("tbl_users").document(userId).get()
                            .addOnSuccessListener { user ->
                                txtName.text = user.getString("name") ?: "User"
                                val avatar = user.getString("profile_picture") ?: ""
                                if (avatar.isNotEmpty()) {
                                    Glide.with(this).load(avatar).into(imgAvatar)
                                }
                            }
                    }

                    commentContainer.addView(view)
                }
            }
    }

}
