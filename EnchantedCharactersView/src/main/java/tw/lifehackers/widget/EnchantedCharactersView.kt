package tw.lifehackers.widget

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import tw.lifehackers.widget.enchantedcharacters.R

class EnchantedCharactersView : View {

    companion object {
        private val defaultTextColor = Color.parseColor("#FF000000")
        private const val duration: Int = 1
        private const val steps: Int = 30
    }

    private val defaultTextSize =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, context.resources.displayMetrics)

    private var textPaint: TextPaint = TextPaint()

    private var textColor: Int = defaultTextColor
        set(value) {
            field = value
            updateTextPaint()
        }

    private var textSize: Float = defaultTextSize
        set(value) {
            field = value
            updateTextPaint()
        }

    private var text: String = ""
        set(value) {
            if (field == value) return
            onTextChanged(field, value)
            field = value
        }

    private var typeface: String? = null
        set(value) {
            field = value
            updateTextPaint()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        init(attr, 0)
    }

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(context, attr, defStyleAttr) {
        init(attr, defStyleAttr)
    }

    private fun init(attr: AttributeSet?, defStyleAttr: Int) {
        val typedArr = context.obtainStyledAttributes(attr, R.styleable.EnchantedCharactersView, defStyleAttr, 0)
        textColor = typedArr.getColor(R.styleable.EnchantedCharactersView_textColor, defaultTextColor)
        val textSizeFromAttr = typedArr.getDimension(R.styleable.EnchantedCharactersView_textSize, -1f)
        textSize = if (textSizeFromAttr < 0) defaultTextSize else textSizeFromAttr
        typeface = typedArr.getString(R.styleable.EnchantedCharactersView_typeface)
        text = typedArr.getString(R.styleable.EnchantedCharactersView_text) ?: ""
        typedArr.recycle()

        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        val textBounds = textPaint.getTextBounds(text)
        var offsetX = paddingLeft.toFloat()
        val offsetY = paddingTop.toFloat() + textBounds.height()
        text.forEach {
            val charAsStr = it.toString()
            canvas.drawText(charAsStr, 0, 1, offsetX, offsetY, textPaint)
            offsetX += textPaint.measureText(charAsStr)
        }
    }

    private fun updateTextPaint() {
        if (Color.alpha(textColor) == 0) {
            textPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            textPaint.xfermode = null
        }
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.typeface = if (typeface == null) null else Typeface.createFromAsset(context.assets, typeface)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val textBounds = textPaint.getTextBounds(text)
        val additionalBottomPadding = textPaint.fontMetricsInt.run { bottom - baseline }
        setMeasuredDimension(
            resolveSizeAndState(textBounds.width() + paddingLeft + paddingRight, widthMeasureSpec, 0),
            resolveSizeAndState(textBounds.height() + paddingBottom + paddingTop + additionalBottomPadding, heightMeasureSpec, 0)
        )
    }

    private fun onTextChanged(field: CharSequence, value: CharSequence) {
        requestLayout()
    }
}

private fun TextPaint.getTextBounds(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)
    rect.left = 0
    rect.right = measureText(text).toInt()
    return rect
}
