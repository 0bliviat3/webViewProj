package com.devtool.webview

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class URLActivity : AppCompatActivity() {
    private lateinit var serverUrl: EditText
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_settings)

        prefs = PreferenceManager(this)
        serverUrl = findViewById(R.id.server_url)
        serverUrl.setText(prefs.loadUrl() ?: "https://www.google.com")

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val input = serverUrl.text.toString().trim()

            if (!isUrlAllowed(input)) {
                Toast.makeText(this, "허용되지 않는 URL입니다. (https 만 허용)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.saveUrl(input)
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            // mainactivity로 이동
            val intent = Intent(this, ConsoleWebActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun isUrlAllowed(urlString: String?): Boolean {
        // 1. null 또는 빈 문자열 검사
        if (urlString.isNullOrBlank()) {
            Log.w(TAG, "URL is null or blank. Blocking.")
            return false
        }

        // 2. URI 파싱: MalformedURLException을 방지하고 정확한 URI 구성요소 추출
        val uri = try {
            Uri.parse(urlString)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid URI format: $urlString", e)
            return false
        }

        // 3. 스키마 검증: http와 https만 허용
        val scheme = uri.scheme
        if (scheme == null || scheme.lowercase() != "https") {
            Log.w(TAG, "Invalid scheme: $urlString. Blocking.")
            return false
        }

        // 모든 검증 통과
        return true
    }
}