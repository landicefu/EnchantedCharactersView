package tw.lifehackers.widget

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import tw.lifehackers.widget.enchantedcharacters.R

class EnchantedCharactersView : View {

    companion object {
        private val defaultTextColor = Color.parseColor("#FF000000")
        private const val NUM_STEPS = 30
    }

    private val defaultTextSize =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, context.resources.displayMetrics)

    private var textPaint: TextPaint = TextPaint()

    var textColor: Int = defaultTextColor
        set(value) {
            field = value
            updateTextPaint()
        }

    var textSize: Float = defaultTextSize
        set(value) {
            field = value
            updateTextPaint()
        }

    var text: String = ""
        set(value) {
            if (field == value) return
            onTextChanged(field, value)
            field = value
        }

    var typeface: String? = null
        set(value) {
            field = value
            updateTextPaint()
        }

    var fadeInForNonMovingChar: Boolean = false

    private var isAtMost: Boolean = false
    private val uiHandler = Handler()

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
        fadeInForNonMovingChar = typedArr.getBoolean(R.styleable.EnchantedCharactersView_fadeInForNonMovingChar, false)
        typedArr.recycle()

        setLayerType(LAYER_TYPE_HARDWARE, null)
        intermediateState = null
    }

    private fun updateTextPaint() {
        if (Color.alpha(textColor) == 0) {
            textPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            textPaint.xfermode = null
        }
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.typeface = if (typeface == null) null
        else tryOrNull { Typeface.createFromAsset(context.assets, typeface) }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        isAtMost = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST
        val intermediateState = intermediateState
        val textWidth = if (intermediateState == null) {
            textPaint.measureText(text)
        } else {
            maxOf(textPaint.measureText(intermediateState.oldStr), textPaint.measureText(intermediateState.newStr))
        }
        setMeasuredDimension(
            resolveSizeAndState(textWidth.toInt() + paddingLeft + paddingRight, widthMeasureSpec, 0),
            resolveSizeAndState(textPaint.height + paddingBottom + paddingTop, heightMeasureSpec, 0)
        )
    }

    private var intermediateState: IntermediateState? = null

    private fun onTextChanged(oldStr: String, newStr: String) {
        uiHandler.removeCallbacks(invalidateRunnable)
        intermediateState = IntermediateState(oldStr, newStr, NUM_STEPS, textColor, textPaint, fadeInForNonMovingChar)
        requestLayout()
    }

    private val invalidateRunnable = Runnable { invalidate() }

    override fun onDraw(canvas: Canvas) {
        val intermediateState = intermediateState
        val offsetY = paddingTop.toFloat() + textPaint.height - textPaint.fontMetrics.bottom
        if (intermediateState == null) {
            val offsetX = paddingLeft.toFloat()
            textPaint.color = textColor
            canvas.drawText(text, offsetX, offsetY, textPaint)
        } else {
            intermediateState.drawStep(canvas, paddingLeft, offsetY)
            if (!intermediateState.isFinalStep()) {
                uiHandler.post(invalidateRunnable)
            } else {
                this@EnchantedCharactersView.intermediateState = null
                if (isAtMost) {
                    // change the width to new string
                    requestLayout()
                }
            }
        }
    }
}

private fun <T> tryOrNull(block: () -> T) = try {
    block()
} catch (e: Exception) {
    null
}

private val TextPaint.height: Int get() = fontMetricsInt.run { bottom - top }

private fun TextPaint.measureChar(char: Char) = measureText(char.toString())

private class IntermediateState(
    val oldStr: String,
    val newStr: String,
    val numberOfSteps: Int,
    val textColor: Int,
    val textPaint: TextPaint,
    val fadeIn: Boolean
) {
    private var currentStep = 0

    private val showIndexInNewString = mutableListOf<Int>()
    private val shiftingCharList = mutableListOf<ShiftingChar>()

    private val oldStrOffset = mutableListOf<Float>()
    private val newStrOffset = mutableListOf<Float>()

    init {
        val oldIndexedCharList = oldStr.mapIndexed(::IndexedChar).toMutableList()

        var accuOffset = 0f
        oldStr.forEach { char ->
            oldStrOffset.add(accuOffset)
            accuOffset += textPaint.measureChar(char)
        }

        accuOffset = 0f
        newStr.forEach { char ->
            newStrOffset.add(accuOffset)
            accuOffset += textPaint.measureChar(char)
        }

        newStr.forEachIndexed { index, char ->
            val availableIndex = oldIndexedCharList.indexOfFirst { it.char == char }
            if (availableIndex == -1) {
                showIndexInNewString.add(index)
            } else {
                val oldIndexedChar = oldIndexedCharList[availableIndex]
                shiftingCharList.add(ShiftingChar(char, oldStrOffset[oldIndexedChar.index], newStrOffset[index]))
                oldIndexedCharList.removeAt(availableIndex)
            }
        }
    }

    fun drawStep(canvas: Canvas, paddingLeft: Int, offsetY: Float) {
        val percentage = (++currentStep).toFloat() / numberOfSteps

        if (fadeIn) {
            textPaint.color = textColor and 0x00FFFFFF or (Color.alpha(textColor) * percentage).toInt().shl(24)
        }

        for (index in showIndexInNewString) {
            canvas.drawText(
                newStr[index].toString(), paddingLeft.toFloat() + newStrOffset[index], offsetY, textPaint
            )
        }

        textPaint.color = textColor
        for (shiftingChar in shiftingCharList) {
            val offsetX = paddingLeft + linearInterpolate(shiftingChar.startOffset, shiftingChar.endOffset, percentage)
            canvas.drawText(shiftingChar.char.toString(), offsetX, offsetY, textPaint)
        }
    }

    fun isFinalStep() = currentStep == numberOfSteps

    private data class IndexedChar(
        val index: Int,
        val char: Char
    )

    private data class ShiftingChar(
        val char: Char,
        val startOffset: Float,
        val endOffset: Float
    )
}

private fun linearInterpolate(oldValue: Float, newValue: Float, percentage: Float) =
    oldValue + (newValue - oldValue) * percentage
