package com.synthmind.seekbarframe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.unit.fontscaling.MathUtils
import androidx.xr.runtime.math.clamp

class CustomSeekBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var centerX = 0f
    private var markerRadius = 20f
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun centerMarkerAtPosition(position: Float) {
        val targetOffset = width * position - centerX
        progress = MathUtils.clamp(targetOffset / width + 0.5f, 0f, 1f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
    }
    private val centerLinePaint = Paint().apply {
        color = Color.parseColor("#0077CC")
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val barPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val markerPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateBarsPosition()
            invalidate()
        }

    var onSeekBarChangeListener: OnSeekBarChangeListener? = null
    var markerPositions: List<Float> = emptyList() // Danh sách các vị trí marker (0-1)
    var videoDuration: Long = 0 // Thời lượng video (ms)

    interface OnSeekBarChangeListener {
        fun onProgressChanged(progress: Float)
    }

    private var barsOffset = 0f
    private var isTouching = false
    private var lastTouchX = 0f

    fun setMarkers(seconds: List<Long>) {
        if (videoDuration > 0) {
            markerPositions = seconds.map { it / (videoDuration / 1000f) }
            invalidate()
        }
    }

    private fun updateBarsPosition() {
        barsOffset = progress * width
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cornerRadius = height / 2f
        val centerX = width / 2f

        // Vẽ nền bo tròn
        canvas.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            cornerRadius, cornerRadius, backgroundPaint
        )

        // Vẽ vạch màu xanh dương ở giữa (cố định)
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), centerLinePaint)

        // Vẽ các thanh xám (chỉ từ giữa sang phải)
        val spacing = 20f
        val barCount = (width / spacing).toInt() + 1

        // Tính toán vị trí bắt đầu của các thanh xám
        val startPosition = centerX - barsOffset

        for (i in 0 until barCount) {
            val x = startPosition + (i * spacing)
            if (x >= 0 && x <= width) {
                val barHeight = height * 0.6f
                val barTop = (height - barHeight) / 2f
                val barBottom = barTop + barHeight

                // Vẽ thanh xám
                canvas.drawLine(x, barTop, x, barBottom, barPaint)

                // Kiểm tra và vẽ chấm đỏ nếu đây là vị trí marker
                val currentPos = (x - centerX + barsOffset) / width
                if (markerPositions.any { Math.abs(it - currentPos) < 0.01f }) {
                    canvas.drawCircle(x, height / 2f, 20f, markerPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                lastTouchX = event.x
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTouching) {
                    val deltaX = event.x - lastTouchX
                    barsOffset -= deltaX
                    barsOffset = barsOffset.coerceIn(0f, width.toFloat())

                    val newProgress = barsOffset / width
                    if (progress != newProgress) {
                        progress = newProgress
                        onSeekBarChangeListener?.onProgressChanged(progress)
                    }

                    lastTouchX = event.x
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}