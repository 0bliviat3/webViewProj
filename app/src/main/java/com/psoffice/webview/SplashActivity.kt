package com.psoffice.webview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    fun navigateToMain() {

        startActivity(Intent(this, URLActivity::class.java))
        finish()
//        Handler(Looper.getMainLooper()).postDelayed({
//            startActivity(Intent(this, URLActivity::class.java))
//            finish()
//        }, 2000) // 2초 후 메인으로 이동
    }
}