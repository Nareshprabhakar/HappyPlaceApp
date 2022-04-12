package com.example.happyplacesapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.happyplacesapp.R
import com.example.happyplacesapp.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")

//flag full screen
@Suppress("DEPRECATION")

class SplashScreenActivity : AppCompatActivity() {

    private var binding: ActivitySplashScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        //   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        //for sdk above 30
        //     window.insetsController!!.hide(WindowInsets.Type.statusBars())
        //   supportActionBar?.hide()
        //}else{
        //  window.setFlags(
        //    WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //  WindowManager.LayoutParams.FLAG_FULLSCREEN
        //)
        //supportActionBar?.hide()
        // }

        window.statusBarColor = ContextCompat.getColor(this, R.color.white_)

        // use handler to make a delay and move to main activity
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000
        )

    }
}