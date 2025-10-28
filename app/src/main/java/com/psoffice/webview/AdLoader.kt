package com.psoffice.webview

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd

class AdLoader : Application(), Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        registerActivityLifecycleCallbacks(this)
        loadAd()
    }

    private fun loadAd() {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            BuildConfig.AD_OPEN_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                }
            })
    }

    fun showAdIfAvailable(activity: Activity, onDone: () -> Unit) {
        if (isShowingAd) {
            onDone()
            return
        }

        if (appOpenAd == null) {
            // 광고가 없을 때는 스플래시를 약간 보여주게 딜레이 후 진행 (UX 목적)
            Handler(Looper.getMainLooper()).postDelayed({
                onDone()
            }, 700) // 700ms 정도 보여주고 넘어가게 (원하면 조정)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                appOpenAd = null
                loadAd()
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isShowingAd = false
                onDone()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }

        appOpenAd?.show(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is SplashActivity) {
            showAdIfAvailable(activity) {
                activity.navigateToMain()
            }
        }
    }

    override fun onActivityCreated(a: Activity, b: Bundle?) {}
    override fun onActivityResumed(a: Activity) {}
    override fun onActivityPaused(a: Activity) {}
    override fun onActivityStopped(a: Activity) {}
    override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
    override fun onActivityDestroyed(a: Activity) {}

}