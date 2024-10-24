package com.works.carebook001.patient.services

import android.view.translation.Translator
// import androidx.privacysandbox.tools.core.generator.build
import com.works.carebook001.patient.models.NewsData
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class WebNewsService {
    fun newsList(): List<NewsData> {
        val arr = mutableListOf<NewsData>()
        val url = "https://www.haberler.com/saglik/"
        val document: Document = Jsoup.connect(url).timeout(15000).get()
        val elements: Elements = document.getElementsByClass("boxStyle color-general hbBoxMainText")

        for (item in elements) {
            val img = item.getElementsByTag("img")
            val title = img.attr("alt")
            val src = img.attr("data-src")
            val href = item.attr("abs:href")

            if (title != "" && src != "" && href != "") {
                val news = NewsData(title, src, href)
                arr.add(news)
            }
            if (arr.size >= 14) {
                break // Limit to 14 news items
            }
        }
        return arr
    }
}
//class WebNewsService {
//    fun newsList(): List<NewsData> {
//        val arr = mutableListOf<NewsData>()
//        val url = "https://www.haberler.com/saglik/"
//        val document: Document = Jsoup.connect(url).timeout(15000).get()
//        val elements: Elements = document.getElementsByClass("boxStyle color-general hbBoxMainText")
//
//        // Initialize the translator
//        val options = com.google.mlkit.nl.translate.TranslatorOptions.Builder()
//            .setSourceLanguage(com.google.mlkit.nl.translate.TranslateLanguage.TURKISH)
//            .setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH)
//            .build()
//        val translator = Translator.getInstance(options)
//
//        for (item in elements) {
//            val img = item.getElementsByTag("img")
//            val title = img.attr("alt")
//            val src = img.attr("data-src")
//            val href = item.attr("abs:href")
//
//            if (title != "" && src != "" && href != "") {
//                translator.translate(title)
//                    .addOnSuccessListener { translatedTitle ->
//                        val news = NewsData(translatedTitle, src, href)
//                        arr.add(news)
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.e("Translation", "Translation failed", exception)
//                        val news = NewsData(title, src, href) // Add original if translation fails
//                        arr.add(news)
//                    }
//            }
//            if (arr.size >= 14) {
//                break
//            }
//        }
//        return arr
//    }
//}