package com.skateboard.wordcloud.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.skateboard.wordcloud.R
import kotlin.math.cos
import kotlin.math.sin

class WordCloudView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    /**
     * 球体半径
     */
    var radius = 200.0

    /**
     * 文字最大大小
     */
    var maxTextSize = 120.0F

    /**
     * 文字颜色以及大小比例
     */
    var minFactor = 0.2

    /**
     * 每个圆切面文字数量
     */
    var perNumInCircle = 7

    /**
     * 文字列表
     */
    var wordList: MutableList<String>? = null
        set(value) {
            field = value
            value?.let {
                circleRowNum =
                    it.size / perNumInCircle + if (it.size % perNumInCircle == 0) 0 else 1
                upDegreeGap = 180.0 / (circleRowNum + 1)
                bottomDegreeGap = 360.0 / perNumInCircle
                requestLayout()
            }
        }

    /**
     * 圆切面数量
     */
    private var circleRowNum = 0

    /**
     * 文字颜色
     */
    var textColor = Color.BLACK

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var wordItemList: MutableList<WordItem>? = null

    /**
     * 圆心到球面连线与y轴的夹角(范围[0,π]）
     */
    private var upDegreeGap = 0.0

    /**
     * 圆心到球面连线在x轴和z轴的投影与x轴正向的夹角（范围[0,2π]）
     */
    private var bottomDegreeGap = 0.0

    private val textRect = Rect()

    init {
        paint.color = textColor
        paint.textSize = maxTextSize
        parseAttributes(context, attributeSet)
    }

    private fun parseAttributes(context: Context, attributeSet: AttributeSet?) {
        if (attributeSet != null) {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.WordCloudView)
            radius =
                typedArray.getDimensionPixelSize(R.styleable.WordCloudView_radius, 200).toDouble()
            maxTextSize = typedArray.getDimensionPixelSize(
                R.styleable.WordCloudView_max_text_size,
                120
            ).toFloat()
            minFactor = typedArray.getFloat(R.styleable.WordCloudView_min_factor, 0.2F).toDouble()
            perNumInCircle =
                typedArray.getInt(R.styleable.WordCloudView_per_num_in_circle, perNumInCircle)
            textColor = typedArray.getColor(R.styleable.WordCloudView_text_color, textColor)
            paint.color = textColor
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        wordItemList = genWordItemList()
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            if (widthMode == MeasureSpec.AT_MOST) {
                width = (2 * radius + maxTextSize).toInt()
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = (2 * radius + maxTextSize).toInt()
            }
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun genWordItemList(): MutableList<WordItem>? {
        wordList?.let { list ->
            val wordItemList = mutableListOf<WordItem>()
            var upDegree = 0.0
            for (row in 0 until circleRowNum) {
                upDegree += upDegreeGap
                upDegree %= 180.0
                var bottomDegree = 0.0
                for (col in 0 until perNumInCircle) {
                    val index = row * perNumInCircle + col
                    if (index < wordList?.size ?: 0) {
                        bottomDegree += bottomDegreeGap
                        bottomDegree %= 360.0
                        val wordItem = WordItem(list[index])
                        wordItem.cal(radius, upDegree, bottomDegree, minFactor)
                        wordItemList.add(wordItem)
                    }
                }
            }
            return wordItemList
        }
        return null
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let { canvas ->
            wordItemList?.forEach { wordItem ->
                wordItem.move(radius, 0.0, 1.0, minFactor)
                paint.textSize = (wordItem.factor * maxTextSize).toFloat()
                paint.alpha = 30.coerceAtLeast((wordItem.factor * 255).toInt())
                textRect.setEmpty()
                paint.getTextBounds(wordItem.text, 0, wordItem.text.length, textRect)
                canvas.drawText(
                    wordItem.text,
                    ((width - paddingLeft - paddingRight) / 2 + wordItem.x - textRect.width() / 2).toFloat(),
                    ((height - paddingTop - paddingBottom) / 2 + wordItem.y - textRect.height() / 2).toFloat(),
                    paint
                )
            }
            postInvalidate()
        }
    }
}

class WordItem(
    var text: String,
    var upDegree: Double = 0.0,
    var bottomDegree: Double = 0.0,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var factor: Double = 0.0
) {

    fun cal(radius: Double, upDegree: Double, bottomDegree: Double, minFactor: Double) {
        this.upDegree = upDegree % 180
        this.bottomDegree = bottomDegree % 360
        y = radius * cos(Math.toRadians(this.upDegree))
        z = -radius * sin(Math.toRadians(this.upDegree)) * sin(Math.toRadians(this.bottomDegree))
        x = radius * sin(Math.toRadians(this.upDegree)) * cos(Math.toRadians(this.bottomDegree))
        factor = minFactor.coerceAtLeast(
            when (bottomDegree) {
                in 0.0..90.0 -> {
                    1.0 / Math.PI * Math.toRadians(bottomDegree) + 0.5
                }
                in 270.0..360.0 -> {
                    1.0 / Math.PI * Math.toRadians(bottomDegree) - 1.5
                }
                else -> {
                    -1.0 / Math.PI * Math.toRadians(bottomDegree) + 1.5
                }
            }
        )
    }

    fun move(radius: Double, upOffset: Double, bottomOffset: Double, minFactor: Double) {
        cal(radius, upDegree + upOffset, bottomDegree + bottomOffset, minFactor)
    }
}
