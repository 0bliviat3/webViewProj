package com.psoffice.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConsoleWebActivity: AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var consoleView: TextView
    private lateinit var input: EditText
    private lateinit var scrollConsole: ScrollView
    private lateinit var consoleContainer: LinearLayout
    private lateinit var titleText: TextView

    private lateinit var prefs: PreferenceManager

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager(this)
        setContentView(R.layout.activity_console_web)

        webView = findViewById(R.id.webView)
        consoleView = findViewById(R.id.consoleView)
        input = findViewById(R.id.consoleInput)
        scrollConsole = findViewById(R.id.scrollConsole)
        consoleContainer = findViewById(R.id.consoleContainer)
        titleText = findViewById(R.id.titleText)

        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        // WebView 설정
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                titleText.text = view?.title ?: "Console WebView"
            }
        }

        // WebChromeClient로 JS console 메시지 캡처
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    val type = when (it.messageLevel()) {
                        ConsoleMessage.MessageLevel.ERROR -> "error"
                        ConsoleMessage.MessageLevel.WARNING -> "warn"
                        else -> "log"
                    }
                    appendConsole(it.message(), type)
                }
                return true
            }
        }

        val url = prefs.loadUrl() ?: "https://www.google.com"
        webView.loadUrl(url)

        // 버튼 동작
        btnMenu.setOnClickListener { showMenu(it) }

        // 콘솔 입력
        input.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val code = input.text.toString()
                if (code.isNotBlank()) {
                    appendConsole("> $code")
                    input.text.clear()
                    runJavascript(code)
                }
                true
            } else false
        }
    }

    private fun showMenu(anchor: android.view.View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("홈으로")
        popup.menu.add("새로고침")
        popup.menu.add("개발자도구 on/off")
        popup.menu.add("주소 설정")

        val url = prefs.loadUrl() ?: "https://www.google.com"

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.title) {
                "홈으로" -> webView.loadUrl(url)
                "새로고침" -> webView.reload()
                "개발자도구 on/off" -> toggleConsole()
                "주소 설정" -> {
                    val intent = Intent(this, URLActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
        popup.show()
    }

    private fun toggleConsole() {
        if (consoleContainer.visibility == LinearLayout.GONE) {
            consoleContainer.visibility = LinearLayout.VISIBLE
        } else {
            consoleContainer.visibility = LinearLayout.GONE
        }
    }

    fun runJavascript(jsCode: String) {

        webView.evaluateJavascript(jsCode) { result ->
            val display = when (result) {
                null, "null", "undefined" -> "undefined"
                else -> result.trim('"')
            }
            appendConsole(display, "info")
        }
    }

    private fun appendConsole(text: String, type: String = "log") {
        runOnUiThread {
            val coloredText = when (type) {
                "error" -> "<font color='#FF5555'>$text</font>"
                "info" -> "<font color='#AAAAAA'>$text</font>"
                else -> "<font color='#DDDDDD'>$text</font>"
            }
            consoleView.append(android.text.Html.fromHtml(coloredText + "<br>"))
            scrollConsole.post { scrollConsole.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

}