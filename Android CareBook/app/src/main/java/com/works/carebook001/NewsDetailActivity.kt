package com.works.carebook001

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.works.carebook001.R

class NewsDetailActivity : AppCompatActivity() {
    lateinit var newsWebView : WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        newsWebView = findViewById(R.id.newsWebView)

        val href = intent.getStringExtra("href")
        if (href != null) {
            newsWebView.loadUrl(href)
        }
    }
}