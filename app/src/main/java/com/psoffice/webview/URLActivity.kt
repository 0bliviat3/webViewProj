package com.psoffice.webview

import android.content.Intent
import android.os.Bundle
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
        serverUrl.setText(prefs.loadUrl() ?: "http://10.40.49.243:8088/com/smartofc/mtgTablet_choice.do")

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val input = serverUrl.text.toString().trim()

            // TODO: url 검증로직 필요하다면 여기서 검증

            prefs.saveUrl(input)
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()

            // mainactivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}