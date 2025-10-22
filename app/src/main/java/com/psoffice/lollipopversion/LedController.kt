package com.psoffice.lollipopversion

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LedController(private val context: Context) {

    fun setLed(url: String, red: Int, green: Int, blue: Int) {
        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val json = JSONObject().apply {
                    put("red", red)
                    put("green", green)
                    put("blue", blue)
                }

                conn.outputStream.use { os ->
                    os.write(json.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = conn.responseCode
                Log.d("LED", "Response code: $responseCode , red: $red, green: $green, blue: $blue")
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("LED", "Error sending LED command", e)
            }
        }.start()
    }

    @JavascriptInterface
    fun setFrontLed(red: Int, green: Int, blue: Int) {
        setLed("http://127.0.0.1:8080/v1/led/front_led", red, green, blue)
    }

    @JavascriptInterface
    fun setSideLed(red: Int, green: Int, blue: Int) {
        setLed("http://127.0.0.1:8080/v1/led/side_led", red, green, blue)
    }

    @JavascriptInterface
    fun setAllLed(red: Int, green: Int, blue: Int) {
        setFrontLed(red, green, blue)
        setSideLed(red, green, blue)
    }

}
