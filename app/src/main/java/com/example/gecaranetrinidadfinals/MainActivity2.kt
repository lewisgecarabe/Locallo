package com.example.gecaranetrinidadfinals

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

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
        val conn = FirebaseFirestore.getInstance()

        // 🔹 LOAD POSTS
        conn.collection("tbl_posts")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { records ->

                for (record in records) {

                    val template = LayoutInflater.from(this)
                        .inflate(R.layout.activity_main3, vlayout, false)

                    // Views
                    val txtContent: TextView = template.findViewById(R.id.txt_description)
                    val txtName: TextView = template.findViewById(R.id.txt_post_username)
                    val txtLikes: TextView = template.findViewById(R.id.txt_like_count)
                    val txtComments: TextView = template.findViewById(R.id.txt_comment_count)
                    val imgLike: ImageView = template.findViewById(R.id.btn_like)
                    val imgPost: ImageView = template.findViewById(R.id.img_post_main)
                    val txtLocation: TextView = template.findViewById(R.id.txt_post_location)
                    val txtCategory: TextView = template.findViewById(R.id.txt_post_category)
                    val imgProfile : ImageView = template.findViewById(R.id.img_avatar)

                    // Firestore fields
                    val content = record.getString("content") ?: ""
                    val userId = record.getString("user_id") ?: ""
                    val imageUrl = record.getString("image_url") ?: ""
                    val likes = record.getLong("likes_count") ?: 0
                    val comments = record.getLong("comments_count") ?: 0
                    val postId = record.id
                    val createdAt = record.getTimestamp("timestamp")
                    val category = record.getString("category") ?: ""
                    val location = record.getString("location") ?: ""


                    // Display text
                    txtContent.text = content
                    txtLikes.text = likes.toString()
                    txtComments.text = comments.toString()
                    txtCategory.text = category.toString()
                    txtLocation.text = location.toString()

                    // 🔹 LOAD USER NAME FROM tbl_users
                    if (userId.isNotEmpty()) {
                        conn.collection("tbl_users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                txtName.text = userDoc.getString("name") ?: "User"
                                val profileUrl = userDoc.getString("profile_picture") ?: ""
                                if (profileUrl.isNotEmpty()) {
                                    Glide.with(this)
                                        .load(profileUrl)
                                        .placeholder(R.drawable.person)
                                        .into(imgProfile)
                                }}
                    } else {
                        txtName.text = "User"
                    }



                    // 🔹 LOAD POST IMAGE
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.place2)
                            .into(imgPost)
                    } else {
                        imgPost.visibility = View.GONE
                    }

                    // 🔹 LIKE BUTTON (FIXED)
                    imgLike.setOnClickListener {
                        val currentLikes = txtLikes.text.toString().toLong()
                        val newLikes = currentLikes + 1

                        conn.collection("tbl_posts")
                            .document(postId)
                            .update("likes_count", newLikes)
                            .addOnSuccessListener {
                                txtLikes.text = newLikes.toString()
                                imgLike.setImageResource(R.drawable.baseline_thumb_up_24)
                            }
                    }

                    // (Optional date formatting – structure kept)
                    if (createdAt != null) {
                        val date = createdAt.toDate()
                        DateFormat.format("MM dd  hh:mm a", date)
                    }

                    vlayout.addView(template)
                }
            }
    }
}
