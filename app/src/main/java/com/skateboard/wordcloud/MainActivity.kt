package com.skateboard.wordcloud

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skateboard.wordcloud.view.WordCloudView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val wordCloudView = findViewById<WordCloudView>(R.id.wordCloudView)
        wordCloudView.paint.setShadowLayer(5F, 0F, 5F, Color.WHITE)
        val wordList = mutableListOf<String>()
        for (i in 0 until 50) {
            wordList.add(i.toString())
        }
        wordCloudView.wordList = wordList
    }
}
