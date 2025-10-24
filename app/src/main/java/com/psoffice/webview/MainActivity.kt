package com.psoffice.webview

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var prefs: PreferenceManager
    private var tapCount = 0
    private var lastTouchTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager(this)

        // 전원 연결 상태 확인
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, filter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

        val isPlugged =
            (status == BatteryManager.BATTERY_PLUGGED_AC || status == BatteryManager.BATTERY_PLUGGED_USB || status == BatteryManager.BATTERY_PLUGGED_WIRELESS)

        if (isPlugged) {
            // 전원이 연결되어 있으면 화면 꺼짐 방지
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            // 전원 해제 시 다시 꺼짐 허용
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // 전체화면 설정
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        setContentView(R.layout.activity_main)

        // WebView 설정
        webView = findViewById(R.id.webview)

        // 앱 실행시 캐시 초기화
        clearWebViewData(webView);

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true


        // WebViewClient 설정
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // 새로운 페이지 요청이 오면, 외부 브라우저가 아니라 WebView에서 처리하도록 함
                view.loadUrl(request.url.toString())
                return true
            }

            // 구버전 호환 (Android 5~)
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        val url = prefs.loadUrl() ?: "http://10.40.49.243:8088/com/smartofc/mtgTablet_choice.do"
        webView.loadUrl(url)

        setupExitTouchArea()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // 뒤로가기 방지
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupExitTouchArea() {
        val touchArea = findViewById<View>(R.id.exit_area)

        touchArea.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val now = System.currentTimeMillis()
                if (now - lastTouchTime < 1000) {
                    tapCount++
                } else {
                    tapCount = 1
                }
                lastTouchTime = now

                Log.d(TAG, "Touch count: $tapCount")

                if (tapCount >= 4) {
                    Log.d(TAG, "Exiting app")
                    finish()
                }
            }

            // 이벤트를 소비하지 않음 → WebView로도 전달됨
            false
        }
    }

    // 캐시 초기화
    private fun clearWebViewData(webView: WebView) {
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()

        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        WebStorage.getInstance().deleteAllData()
    }
}

