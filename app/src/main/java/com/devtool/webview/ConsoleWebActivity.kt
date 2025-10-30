package com.devtool.webview

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class ConsoleWebActivity: AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var input: EditText
    private lateinit var scrollConsole: ScrollView
    private lateinit var consoleContainer: LinearLayout
    private lateinit var consoleOutputContainer: LinearLayout
    private lateinit var titleText: TextView

    private lateinit var prefs: PreferenceManager

    private var consoleInitialized = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager(this)
        setContentView(R.layout.activity_console_web)

        webView = findViewById(R.id.webView)
        input = findViewById(R.id.consoleInput)
        scrollConsole = findViewById(R.id.scrollConsole)
        consoleContainer = findViewById(R.id.consoleContainer)
        consoleOutputContainer = findViewById(R.id.consoleOutputContainer)
        titleText = findViewById(R.id.titleText)

        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        // WebView 설정
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                titleText.text = view?.title ?: "Console WebView"
                injectConsoleHelper()
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
                    appendConsoleBlockFromSimpleText(it.message(), type)
                    //appendConsole(it.message(), type)
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
                    appendConsoleBlockRawInput(code)
                    //appendConsole("> $code")
                    input.text.clear()
                    runJavascriptThroughHelper(code)
                    //runJavascript(code)
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

            // 처음 열릴 때만 헤더 추가
            if (!consoleInitialized) {
                addConsoleHeader()
                consoleInitialized = true
            }
        } else {
            consoleContainer.visibility = LinearLayout.GONE
        }
    }

    @SuppressLint("InflateParams")
    private fun addConsoleHeader() {
        val inflater = LayoutInflater.from(this)
        val headerView = inflater.inflate(R.layout.console_block_item, null)

        val textView = headerView.findViewById<TextView>(R.id.consoleText)
        textView.text = "--- JS Terminal ---"
        textView.setTextColor(0xFF888888.toInt())
        textView.textSize = 14f
        textView.isClickable = false  // 복사 방지
        textView.alpha = 0.7f

        consoleOutputContainer.addView(headerView)
    }

    /** JS Helper 주입 **/
    private fun injectConsoleHelper() {
        val js = loadJSFromAsset("console_helper.js")
        webView.evaluateJavascript(js, null)
    }

    /** assets/console_helper.js 읽기 **/
    private fun loadJSFromAsset(fileName: String): String {
        val sb = StringBuilder()
        assets.open(fileName).use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                reader.forEachLine { sb.append(it).append("\n") }
            }
        }
        return sb.toString()
    }

    /** 사용자 입력 JS 실행 **/
    private fun runJavascriptThroughHelper(userCode: String) {
        val escaped = userCode.replace("\\", "\\\\").replace("\"","\\\"").replace("\n","\\n")
        val call = "typeof window.__consoleHelper !== 'undefined' ? window.__consoleHelper.exec(\"$escaped\") : \"undefined\";"

        webView.evaluateJavascript(call) { result ->
            try {
                val jsonStr = result.trim('"').replace("\\n","\n").replace("\\\"","\"")
                val tree = JSONObject(jsonStr)
                appendConsoleBlockFromTree(tree)
            } catch (e: Exception) {
                appendConsoleBlockFromSimpleText(result, "error")
            }
        }
    }

    private fun appendConsoleBlockFromTree(tree: JSONObject, parent: LinearLayout? = null, indentLevel: Int = 0) {
        val container = parent ?: consoleOutputContainer
        val inflater = layoutInflater

        val blockView = inflater.inflate(R.layout.console_block_item, container, false)
        val textView = blockView.findViewById<TextView>(R.id.consoleText)

        // 색상 지정
        val type = tree.optString("type", "log")
        textView.setTextColor(
            when(type){
                "error" -> 0xFFFF5555.toInt()
                "function" -> 0xFFAA88FF.toInt()
                "info" -> 0xFFAAAAAA.toInt()
                else -> 0xFFDDDDDD.toInt()
            }
        )

        // key:value 표시
        val key = tree.optString("key", "")
        val value = tree.opt("value")
        if(value != null) {
            textView.text = if(key.isNotEmpty()) "$key : $value" else value.toString()
            blockView.setOnClickListener { copyToClipboard(value.toString(), blockView) }
            textView.setPadding(8 + 20*indentLevel, 4, 4, 4)
            container.addView(blockView)
        }

        // children 재귀 처리
        if(tree.has("children")) {
            val foldButton = TextView(this)
            foldButton.text = "+ expand"
            foldButton.setTextColor(0xFFAAAAAA.toInt())
            foldButton.setPadding(8 + 20*indentLevel, 4, 4, 4)
            container.addView(foldButton)

            val childrenContainer = LinearLayout(this)
            childrenContainer.orientation = LinearLayout.VERTICAL
            childrenContainer.visibility = LinearLayout.GONE
            container.addView(childrenContainer)

            foldButton.setOnClickListener {
                if (childrenContainer.visibility == LinearLayout.GONE) {
                    childrenContainer.visibility = LinearLayout.VISIBLE
                    foldButton.text = "- collapse"
                } else {
                    childrenContainer.visibility = LinearLayout.GONE
                    foldButton.text = "+ expand"
                }
            }

            val children = tree.get("children")
            when (children) {
                is JSONObject -> {
                    children.keys().forEach { k ->
                        val child = children.getJSONObject(k)
                        appendConsoleBlockFromTree(child, childrenContainer, indentLevel + 1)
                    }
                }
                is JSONArray -> {
                    for (i in 0 until children.length()) {
                        val child = children.getJSONObject(i)
                        appendConsoleBlockFromTree(child, childrenContainer, indentLevel + 1)
                    }
                }
            }
        }

        scrollConsole.post { scrollConsole.fullScroll(ScrollView.FOCUS_DOWN) }
    }




    private fun appendConsoleBlockFromSimpleText(text: String, type: String = "log") {
        runOnUiThread {
            val inflater = layoutInflater
            val blockView = inflater.inflate(R.layout.console_block_item, consoleOutputContainer, false)
            val textView = blockView.findViewById<TextView>(R.id.consoleText)
            textView.text = text

            // 색상
            textView.setTextColor(
                when(type){
                    "error" -> 0xFFFF5555.toInt()
                    "info" -> 0xFFAAAAAA.toInt()
                    "input" -> 0xFF66CCFF.toInt()
                    else -> 0xFFDDDDDD.toInt()
                }
            )

            // 복사 기능
            blockView.setOnClickListener { copyToClipboard(text, blockView) }

            consoleOutputContainer.addView(blockView)
            scrollConsole.post { scrollConsole.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    private fun appendConsoleBlockRawInput(code: String){
        val inflater = layoutInflater
        val blockView = inflater.inflate(R.layout.console_block_item, consoleOutputContainer, false)
        val textView = blockView.findViewById<TextView>(R.id.consoleText)
        textView.text = "> $code"

        blockView.setOnClickListener { copyToClipboard(code, blockView) }

        consoleOutputContainer.addView(blockView)
        scrollConsole.post { scrollConsole.fullScroll(ScrollView.FOCUS_DOWN) }
    }


    private fun copyToClipboard(text: String, view: View) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // "> "로 시작하면 제거
        val cleanText = if (text.startsWith("> ")) text.removePrefix("> ") else text

        clipboard.setPrimaryClip(ClipData.newPlainText("console_output", cleanText))

        // 간단한 시각 효과
        view.setBackgroundColor(0x22FFFFFF)
        view.postDelayed({ view.setBackgroundColor(0x00000000) }, 300)

        Toast.makeText(this, "복사됨: ${cleanText.take(30)}...", Toast.LENGTH_SHORT).show()
    }


}