package com.example.gecaranetrinidadfinals

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.activity.result.contract.ActivityResultContracts
import java.util.ArrayList

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

        val vlayout2: LinearLayout = findViewById(R.id.linearLayout2_LG)

        val btnLogOut : Button = findViewById(R.id.btn_logout)
        val usernameTxt : TextView = findViewById(R.id.txt_username)
        val emailTxt : TextView = findViewById(R.id.txtEmail)



        val auth = FirebaseAuth.getInstance()
        val conn = FirebaseFirestore.getInstance()

        val email = auth.currentUser!!.email
        val name = auth.currentUser!!.displayName
        val userid_LG = auth.currentUser!!.uid

        conn.collection("tbl_users").document(userid_LG).get().addOnSuccessListener {
            record ->

            val fa_name = record.getString("name")

            usernameTxt.text = name ?: fa_name
            emailTxt.text = email


        }


        btnLogOut.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Web client ID
                .requestEmail()
                .build()

            // Creates Google Sign-In client using the above settings
            val googleClient = GoogleSignIn.getClient(this, gso).signOut()
        }
        conn.collection("tbl_posts").get().addOnSuccessListener {
                records ->

            for (record in records){
                val inflater = LayoutInflater.from(this)
                val template = inflater.inflate(R.layout.activity_main3,vlayout2,false)

                //textview from cardview
                val txtContent: TextView = template.findViewById(R.id.txtContent_LG)
                val txtName: TextView = template.findViewById(R.id.txtUser_LG)
                val txtLikes: TextView = template.findViewById(R.id.txtLikes_LG)
                val txtComments: TextView = template.findViewById(R.id.txtComments_LG)
                val txtDate: TextView = template.findViewById(R.id.textDate_LG)
                val img_likes : ImageView = template.findViewById(R.id.likeBtn_LG)

                //likeby nooflikes .size .container. add .remove


                //field values
                val content = record.getString("content_LG")
                val name = record.getString("userid_LG")
                val noLikes = record.getLong("nooflikes_LG")
                val noComment = record.getLong("noofcomments_LG")
                val created_at = record.getTimestamp("createDate_LG")
                val likeby = record.get("likeby") as? ArrayList<String> ?: arrayListOf()
                val id = record.id

                img_likes.setOnClickListener {

                    if (likeby.contains(name)){
                        likeby.remove(name)
                        conn.collection("tbl_posts").document(id).update(
                            mapOf(
                                " likeby" to likeby,
                                "nooflikes_LG" to likeby.size
                            )).addOnSuccessListener {
                            txtLikes.text = likeby.size.toString()
                            img_likes.setImageResource(R.drawable.baseline_thumb_up_24)
                        }

                    }else{
                        likeby.add(name.toString())
                        conn.collection("tbl_posts").document(id).update(
                            mapOf(
                                " likeby" to likeby,
                                "nooflikes_LG" to likeby.size
                            ))

                    }
                    img_likes.setImageResource(R.drawable.after_baseline_thumb_up_24)
                }



                if (created_at != null){
                    val date = created_at.toDate()
                    val dateFormmated = DateFormat.format("MM dd \n hh:mm", date)

                    txtDate.text = dateFormmated

                }
                //display values
                txtContent.text = content
                txtName.text = name
                txtLikes.text = noLikes.toString()
                txtComments.text = noComment.toString()

                vlayout2.addView(template)
            }
        }


    }
}