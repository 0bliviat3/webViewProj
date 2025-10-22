package com.psoffice.webview

import android.content.Context

/**
 * 간단한 설정값은 DB로 저장하지 않고 앱폴더로 저장되는 파일로 관리함
 * 이 파일은 앱 삭제시 같이 삭제된다
 */
class PreferenceManager(context: Context) {

    private val pm = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveUrl(url: String) = pm.edit().putString("server_url", url).apply()
    fun loadUrl(): String? = pm.getString("server_url", null)

}