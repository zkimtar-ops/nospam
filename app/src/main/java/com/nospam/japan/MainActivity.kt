package com.nospam.japan

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // إنشاء الـ WebView برمجياً لملء الشاشة
        webView = WebView(this)
        setContentView(webView)

        // إعدادات الـ WebView
        webView.settings.apply {
            javaScriptEnabled = true // تفعيل الجافا سكريبت
            domStorageEnabled = true // تفعيل التخزين المحلي
            allowFileAccess = true
        }

        // منع فتح الروابط في متصفح خارجي
        webView.webViewClient = WebViewClient()

        // إضافة "الجسر" (Interface) للربط بين JavaScript وكود Kotlin
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        
        // تحميل واجهة المستخدم من مجلد assets
        webView.loadUrl("file:///android_asset/index.html")
    }

    // الكلاس المسؤول عن استقبال الأوامر من صفحة الـ HTML
    class WebAppInterface(private val activity: Activity) {
        
        @JavascriptInterface
        fun requestDefaultCallerIdApp() {
            // طلب إذن جعل التطبيق هو المسؤول عن فحص المكالمات (أندرويد 10+)
            val roleManager = activity.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                activity.startActivityForResult(intent, 123)
            }
        }
    }

    // منع إغلاق التطبيق عند الضغط على زر الرجوع إذا كان هناك تاريخ للتصفح
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
