package com.example.gecaranetrinidadfinals

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loader)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo: ImageView = findViewById(R.id.logoImage)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        logo.startAnimation(fadeIn)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LogInActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            finish()
        }, 6000)
    }
}
