package com.hardik.messageapp.presentation.custom_view.shadow_lay

import android.graphics.*
import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import java.lang.NumberFormatException
import kotlin.math.sqrt

class Gradient(
    var gradientStartColor: Int = ViewHelper.NOT_SET_COLOR,
    var gradientCenterColor: Int = ViewHelper.NOT_SET_COLOR,
    var gradientEndColor: Int = ViewHelper.NOT_SET_COLOR,
    var gradientAngle: Int = 0,
    var gradientOffsetX: Float = 0f,
    var gradientOffsetY: Float = 0f,
    var gradientColors: IntArray? = null,
    var gradientPositions: FloatArray? = null
) {

    val isEnable: Boolean
        get() = gradientShader != null || (((gradientStartColor != ViewHelper.NOT_SET_COLOR && gradientEndColor != ViewHelper.NOT_SET_COLOR)
                || (gradientColors != null && gradientColors?.isNotEmpty() == true)) && gradientAngle != -1)

    private var localMatrix: Matrix? = null

    private var gradientShader: LinearGradient? = null

    fun getGradientShader(offsetLeft: Float, offsetTop: Float, offsetRight: Float, offsetBottom: Float): LinearGradient {

        if (gradientShader != null) {
            return gradientShader!!.apply {
                setLocalMatrix(localMatrix)
            }
        }

        val colors = if (gradientColors != null && gradientColors?.isNotEmpty() == true) {
            gradientColors!!
        } else {
            if (gradientCenterColor == ViewHelper.NOT_SET_COLOR) {
                intArrayOf(gradientStartColor, gradientEndColor)
            } else {
                intArrayOf(gradientStartColor, gradientCenterColor, gradientEndColor)
            }
        }

        var realAngle = 0

        if (gradientAngle > 0) {
            val trueAngle = gradientAngle % 360
            realAngle = trueAngle + 360
        }

        val trueAngle = realAngle % 360

        val width = offsetRight - offsetLeft
        val height = offsetBottom - offsetTop

        return when (trueAngle / 45) {
            0 -> {
                val x = offsetRight + gradientOffsetX
                LinearGradient(x, 0f, offsetLeft, 0f, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
            1 -> {
                val x = offsetRight + gradientOffsetX
                val y = offsetTop + gradientOffsetY
                LinearGradient(x, offsetTop, offsetLeft, y, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
            2 -> {
                val y = offsetTop + gradientOffsetY
                LinearGradient(0f, y, 0f, offsetBottom, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
            3 -> {
                val x = width + gradientOffsetX
                val y = (height * 2) + gradientOffsetY
                LinearGradient(0f, y, x, offsetBottom, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
            4 -> {
                val y = offsetBottom + gradientOffsetY
                LinearGradient(0f, y, 0f, 0f, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
            5 -> {
                val x = offsetRight + gradientOffsetX
                val y = offsetTop + gradientOffsetY
                LinearGradient(0f, y, x, offsetTop, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
            6 -> {
                val x = offsetTop + gradientOffsetX
                LinearGradient(x, 0f, offsetRight, 0f, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
            else -> {
                val x = offsetRight + gradientOffsetX
                val y = offsetTop + gradientOffsetY
                LinearGradient(0f, y, x, offsetBottom, colors, gradientPositions, Shader.TileMode.CLAMP)
            }
        }.apply {
            if (localMatrix != null) {
                setLocalMatrix(localMatrix)
            }
        }
    }

    fun updateGradientColor(startColor: Int, centerColor: Int, endColor: Int) {
        this.gradientStartColor = startColor
        this.gradientCenterColor = centerColor
        this.gradientEndColor = endColor
    }

    fun updateGradientColor(startColor: Int, endColor: Int) {
        this.updateGradientColor(startColor, ViewHelper.NOT_SET_COLOR, endColor)
    }

    fun updateGradientAngle(angle: Int) {
        this.gradientAngle = angle
    }

    fun updateGradientOffsetX(offset: Float) {
        this.gradientOffsetX = offset
    }

    fun updateGradientOffsetY(offset: Float) {
        this.gradientOffsetY = offset
    }

    fun updateLocalMatrix(matrix: Matrix?) {
        this.localMatrix = matrix
    }

    fun updateGradientPositions(positions: FloatArray?) {
        this.gradientPositions = positions
    }

    fun updateGradientColors(colors: IntArray?) {
        this.gradientColors = colors
    }

    fun updateGradientShader(shader: LinearGradient?) {
        this.gradientShader = shader
    }
}



class Radius(
    var topLeftRadius: Float = 0f,
    var topRightRadius: Float = 0f,
    var bottomLeftRadius: Float = 0f,
    var bottomRightRadius: Float = 0f
) {

    constructor(radius: Float) : this(radius, radius, radius, radius)

    val isEnable: Boolean
        get() = (topLeftRadius > 0f || topRightRadius > 0f || bottomLeftRadius > 0f || bottomRightRadius > 0f) || radiusHalf

    var radiusHalf = false
    var radiusWeight = 1f

    fun updateRadius(radius: Float) {
        this.topLeftRadius = radius
        this.topRightRadius = radius
        this.bottomLeftRadius = radius
        this.bottomRightRadius = radius
    }

    fun updateRadius(tl: Float, tr: Float, bl: Float, br: Float) {
        this.topLeftRadius = tl
        this.topRightRadius = tr
        this.bottomLeftRadius = bl
        this.bottomRightRadius = br
    }

    fun getRadiusArray(height: Float): FloatArray {

        val targetTopLeftRadius = if (radiusHalf) {
            height.div(2f)
        } else {
            topLeftRadius * radiusWeight
        }
        val targetTopRightRadius = if (radiusHalf) {
            height.div(2f)
        } else {
            topRightRadius * radiusWeight
        }
        val targetBottomLeftRadius = if (radiusHalf) {
            height.div(2f)
        } else {
            bottomLeftRadius * radiusWeight
        }
        val targetBottomRightRadius = if (radiusHalf) {
            height.div(2f)
        } else {
            bottomRightRadius * radiusWeight
        }

        return floatArrayOf(
            targetTopLeftRadius,
            targetTopLeftRadius,
            targetTopRightRadius,
            targetTopRightRadius,
            targetBottomRightRadius,
            targetBottomRightRadius,
            targetBottomLeftRadius,
            targetBottomLeftRadius
        )
    }

    fun getRadiusArray(): FloatArray {

        val targetTopLeftRadius = topLeftRadius * radiusWeight
        val targetTopRightRadius = topRightRadius * radiusWeight
        val targetBottomLeftRadius = bottomLeftRadius * radiusWeight
        val targetBottomRightRadius = bottomRightRadius * radiusWeight

        return floatArrayOf(
            targetTopLeftRadius,
            targetTopLeftRadius,
            targetTopRightRadius,
            targetTopRightRadius,
            targetBottomRightRadius,
            targetBottomRightRadius,
            targetBottomLeftRadius,
            targetBottomLeftRadius
        )
    }
}


class Shadow(
    var blurSize: Float = 0f,
    var shadowColor: Int = ViewHelper.NOT_SET_COLOR,
    var shadowOffsetX: Float = 0f,
    var shadowOffsetY: Float = 0f,
    var shadowSpread: Float = 0f
) {

    private val paint by lazy { Paint() }
    private val path by lazy { Path() }

    val isEnable: Boolean
        get() = (blurSize != 0f || shadowSpread != 0f) && shadowColor != ViewHelper.NOT_SET_COLOR

    fun updatePaint() {

        paint.apply {
            isAntiAlias = true
            color = shadowColor
            style = Paint.Style.FILL

            if (blurSize != 0f) {
                maskFilter = BlurMaskFilter(blurSize, BlurMaskFilter.Blur.NORMAL)
            } else {
                maskFilter = null
            }
        }
    }

    fun updatePath(offset: RectF, radius: Radius?) {

        val rect = RectF(
            offset.left + shadowOffsetX,
            offset.top + shadowOffsetY,
            offset.right + shadowOffsetX,
            offset.bottom + shadowOffsetY
        )

        if (shadowSpread != 0f) {
            rect.inset(-shadowSpread, -shadowSpread)
        }

        path.apply {
            reset()

            if (radius == null) {
                addRect(rect, Path.Direction.CW)
            } else {
                val height = rect.height()
                addRoundRect(rect, radius.getRadiusArray(height), Path.Direction.CW)
            }

            close()
        }
    }

    fun updateShadowColor(color: Int) {
        this.shadowColor = color
    }

    fun updateShadowOffsetX(offset: Float) {
        this.shadowOffsetX = offset
    }

    fun updateShadowOffsetY(offset: Float) {
        this.shadowOffsetY = offset
    }

    fun updateShadowSpread(spread: Float) {
        this.shadowSpread = spread
    }

    fun updateShadowBlurSize(size: Float) {
        this.blurSize = size
    }

    fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }
}


class Stroke(var strokeWidth: Float = 0f,
             var strokeColor: Int = ViewHelper.NOT_SET_COLOR,
             var strokeType: StrokeType = StrokeType.INSIDE
) {

    var blur: Float = 0f
    var blurType = BlurMaskFilter.Blur.NORMAL
    val isEnable: Boolean
        get() = strokeWidth != 0f && strokeColor != ViewHelper.NOT_SET_COLOR

    fun updateStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
    }

    fun updateStrokeColor(color: Int) {
        this.strokeColor = color
    }
}


//----------------------------------------------------------------Model



data class ARGB(
    val alpha: Int,
    val red: Int,
    val green: Int,
    val blue: Int
)

data class Offset(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) : RectF(left, top, right, bottom)


data class Padding(
    var start: Int,
    var top: Int,
    var end: Int,
    var bottom: Int
) {

    fun setPadding(padding: Int) {
        this.start = padding
        this.top = padding
        this.end = padding
        this.bottom = padding
    }

    fun setPadding(start: Int, top: Int, end: Int, bottom: Int) {
        this.start = start
        this.top = top
        this.end = end
        this.bottom = bottom
    }
}


enum class StrokeType(val type: Int) {
    INSIDE(0),
    CENTER(1),
    OUTSIDE(2),
}


//----------------------------------------------------------------Helper




object ViewHelper {

    const val NOT_SET_COLOR = -101

    fun parseGradientColors(arrays: String?): List<Int>? {

        if (arrays.isNullOrEmpty())
            return null

        val list = mutableListOf<Int>()

        val split = arrays.split(",").map {
            val text = it
            text.trim()
        }


        if (split.isEmpty())
            return null

        split.map { it.trim() }

        split.forEach {
            list.add(Color.parseColor(it))
        }

        return list
    }

    fun parseGradientPositions(arrays: String?): List<Float>? {

        if (arrays.isNullOrEmpty())
            return null

        val list = mutableListOf<Float>()

        val split = arrays.split(",").map {
            val text = it
            text.trim()
        }


        if (split.isEmpty())
            return null

        split.map { it.trim() }

        split.forEach {
            list.add(it.toFloat())
        }

        return list
    }

    fun parseShadowArray(context: Context, arrays: String?): List<Shadow>? {

        if (arrays.isNullOrEmpty()) {
            return null
        }

        val list = mutableListOf<Shadow>()
        val split = arrays.split("},").map {
            var text = it
            text = text.replace("{", "")
            text = text.replace("}", "")
            text.trim()
        }

        if (split.isEmpty()) {
            return null
        }

        split.map { it.trim() }

        split.forEach {

            val splitArray = it.split(",")

            val shadow = if (splitArray.size == 4) {
                try {
                    val blurSize = splitArray[0].toFloat().toPx(context)
                    val offsetX = splitArray[1].toFloat().toPx(context)
                    val offsetY = splitArray[2].toFloat().toPx(context)
                    val color = Color.parseColor(splitArray[3])

                    Shadow(blurSize, color, offsetX, offsetY, 0f)
                } catch (e: NumberFormatException) {
                    Shadow(0f, Color.WHITE, 0f, 0f, 0f)
                }
            } else {
                try {
                    val blurSize = splitArray[0].toFloat().toPx(context)
                    val offsetX = splitArray[1].toFloat().toPx(context)
                    val offsetY = splitArray[2].toFloat().toPx(context)
                    val spread = splitArray[3].toFloat().toPx(context)
                    val color = Color.parseColor(splitArray[4])
                    Shadow(blurSize, color, offsetX, offsetY, spread)
                } catch (e: NumberFormatException) {
                    Shadow(0f, Color.WHITE, 0f, 0f, 0f)
                }
            }

            list.add(shadow)
        }

        return list
    }

    fun Float.toPx(context: Context): Float {
        return context.resources.displayMetrics.density * this
    }

    fun intToColorModel(color: Int): ARGB {

        val alpha = Color.alpha(color)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        return ARGB(alpha, red, green, blue)
    }

    fun onSetAlphaFromAlpha(alpha: Float, currentAlpha: Int): Boolean {

        if (alpha !in 0f..1f) {
            return false
        }

        return (alpha * 255) < currentAlpha
    }

    fun onSetAlphaFromColor(alpha: Float, color: Int): Boolean {

        if (alpha !in 0f..1f) {
            return false
        }

        return (alpha * 255) < Color.alpha(color)
    }

    fun getIntAlpha(alpha: Float): Int {

        if (alpha !in 0f..1f) {
            return 255
        }

        return (255 * alpha).toInt()
    }

    fun Path.getInnerPath(strokeWidth: Float): Path {

        val offset = strokeWidth / 2
        val rect = RectF().apply {
            computeBounds(this, true)
            inset(offset, offset)
        }
        val pathMeasure = PathMeasure(this, false)

        return Path().apply {

            for (distance in 0 until pathMeasure.length.toInt()) {

                val pos = FloatArray(2)
                val tan = FloatArray(2)

                pathMeasure.getPosTan(distance.toFloat(), pos, tan)

                val dx = tan[0]
                val dy = tan[1]

                var normalX = -dy
                var normalY = dx

                val lengthNormal = sqrt((normalX * normalX + normalY * normalY).toDouble()).toFloat()

                normalX /= lengthNormal
                normalY /= lengthNormal

                val innerX = pos[0] + normalX * offset
                val innerY = pos[1] + normalY * offset

                if (innerX < rect.left || innerX > rect.right) {
                    continue
                }

                if (innerY < rect.top || innerY > rect.bottom) {
                    continue
                }

                if (distance == 0) {
                    moveTo(innerX, innerY)
                } else {
                    lineTo(innerX, innerY)
                }
            }
        }
    }
}