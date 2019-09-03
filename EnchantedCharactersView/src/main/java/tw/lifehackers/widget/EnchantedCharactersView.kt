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
        private val TAG = EnchantedCharactersView::class.java.simpleName
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
        canvas.drawText(text, 0f, textBounds.height().toFloat(), textPaint)
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
        setMeasuredDimension(
            resolveSizeAndState(textBounds.width(), widthMeasureSpec, 0),
            resolveSizeAndState(textBounds.height(), heightMeasureSpec, 0)
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
