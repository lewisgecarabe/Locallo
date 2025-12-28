package com.example.gecaranetrinidadfinals

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue


class CommentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comments)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val postId = intent.getStringExtra("post_id") ?: return
        val commentList: LinearLayout = findViewById(R.id.comment_list)
        val edtComment: EditText = findViewById(R.id.edt_comment)
        val btnSend: ImageView = findViewById(R.id.btn_send)

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 🔹 LOAD COMMENTS
        db.collection("tbl_comments")
            .whereEqualTo("post_id", postId)
            .get()
            .addOnSuccessListener { records ->
                for (doc in records) {
                    val view = layoutInflater.inflate(
                        R.layout.item_comment,
                        commentList,
                        false
                    )

                    val txtUser = view.findViewById<TextView>(R.id.txt_comment_user)
                    val txtContent = view.findViewById<TextView>(R.id.txt_comment_content)
                    val imgAvatar = view.findViewById<ImageView>(R.id.img_comment_avatar)


                    txtContent.text = doc.getString("content")

                    val uid = doc.getString("user_id") ?: ""
                    db.collection("tbl_users").document(uid).get()
                        .addOnSuccessListener { it ->
                            val avatar = it.getString("profile_picture") ?: ""
                            if (avatar.isNotEmpty()) {
                                Glide.with(this).load(avatar).into(imgAvatar)
                            }
                            txtUser.text = it.getString("name") ?: "User"
                        }

                    commentList.addView(view)
                }
            }

        // 🔹 ADD COMMENT
        btnSend.setOnClickListener {
            val text = edtComment.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val data = mapOf(
                "post_id" to postId,
                "user_id" to userId,
                "content" to text,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("tbl_comments").add(data).addOnSuccessListener {
                edtComment.text.clear()

                db.collection("tbl_posts").document(postId)
                    .update("comments_count", FieldValue.increment(1))

                recreate() // refresh comments
            }
        }
    }
}