package com.example.gecaranetrinidadfinals

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.format.DateFormat
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList


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

        //start of Database
        val conn = FirebaseFirestore.getInstance()
        conn.collection("tbl_posts").get().addOnSuccessListener {
            records ->

            for (record in records){
                val inflater = LayoutInflater.from(this)
                val template = inflater.inflate(R.layout.activity_main3,vlayout,false)

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

                vlayout.addView(template)
            }
        }


    }
}