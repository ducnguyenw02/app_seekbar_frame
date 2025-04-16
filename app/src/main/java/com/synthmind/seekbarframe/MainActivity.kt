package com.synthmind.seekbarframe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.synthmind.seekbarframe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val poseList = listOf("pose1", "pose2", "pose3", "pose4", "pose5", "pose6", "pose7", "pose8")
    private val frameList = mutableListOf<android.graphics.Bitmap>()
    private val specialFrames = listOf(5, 10, 15, 20, 25, 30, 35, 40)
    private var isPlaying = false
    private var currentFrameIndex = 0
    private val frameDelay = 100L // Thời gian delay giữa các frame (ms)
    private val handler = Handler(Looper.getMainLooper())
    private val updateFramesRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                showNextFrame()
                handler.postDelayed(this, frameDelay)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.buttonRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.buttonRecyclerView.adapter = ButtonAdapter(poseList) { poseIndex ->
            moveToSpecialFrame(poseIndex)
        }

        loadFramesFromAssets()

        // Hiển thị frame đầu tiên
        if (frameList.isNotEmpty()) {
            binding.imageview.setImageBitmap(frameList[0])
        }

        // Xử lý sự kiện thay đổi trên seekbar
        binding.customSeekBar.onSeekBarChangeListener = object : CustomSeekBarView.OnSeekBarChangeListener {
            override fun onProgressChanged(progress: Float) {
                // Tính toán frame index dựa trên progress
                val frameIndex = (progress * (frameList.size - 1)).toInt()
                // Đảm bảo frameIndex nằm trong giới hạn
                val safeIndex = frameIndex.coerceIn(0, frameList.size - 1)
                // Hiển thị frame tương ứng
                binding.imageview.setImageBitmap(frameList[safeIndex])
            }
        }
        binding.btnPlayPause.setOnClickListener {
            togglePlayback()
        }

        setupSeekbarWithSpecialFrames()
    }
    private fun moveToSpecialFrame(poseIndex: Int) {
        if (poseIndex in 0 until specialFrames.size && frameList.isNotEmpty()) {
            isPlaying = false
            handler.removeCallbacks(updateFramesRunnable)
            binding.btnPlayPause.text = "Play"

            val frameIndex = specialFrames[poseIndex]
            val safeIndex = frameIndex.coerceIn(0, frameList.size - 1)

            binding.imageview.setImageBitmap(frameList[safeIndex])
            currentFrameIndex = safeIndex

            val targetProgress = safeIndex.toFloat() / (frameList.size - 1)

            // Di chuyển seekbar để marker ra giữa
            binding.customSeekBar.centerMarkerAtPosition(targetProgress)

            // Animation mượt mà
            animateSeekbarToCenter(targetProgress)
        }
    }

    private fun animateSeekbarToCenter(targetProgress: Float) {
        val animator = ValueAnimator.ofFloat(binding.customSeekBar.progress, targetProgress)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            binding.customSeekBar.progress = progress
        }

        animator.start()
    }

    private fun animateSeekbarToPosition(targetProgress: Float) {
        val currentProgress = binding.customSeekBar.progress
        val animator = ValueAnimator.ofFloat(currentProgress, targetProgress)

        animator.duration = 300 // Thời gian animation 300ms
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            binding.customSeekBar.progress = progress
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Đảm bảo progress chính xác khi kết thúc
                binding.customSeekBar.progress = targetProgress
            }
        })

        animator.start()
    }
    private fun setupSeekbarWithSpecialFrames() {
        if (frameList.isNotEmpty()) {
            // Tính toán vị trí các frame đặc biệt (từ 0.0 đến 1.0)
            val specialFramePositions = specialFrames.map { frameIndex ->
                frameIndex.toFloat() / (frameList.size - 1)
            }.filter { it <= 1.0f } // Lọc các frame vượt quá kích thước

            // Chuyển đổi sang milliseconds giả định (nếu cần)
            val specialFrameTimes = specialFrames.map { it * 100L } // Giả sử mỗi frame 100ms

            // Thiết lập marker cho seekbar
            binding.customSeekBar.videoDuration = frameList.size * 100L // Tổng thời gian
            binding.customSeekBar.setMarkers(specialFrameTimes)

            // Hoặc có thể dùng trực tiếp positions nếu CustomSeekBar hỗ trợ
            binding.customSeekBar.markerPositions = specialFramePositions
        }
    }

    private fun togglePlayback() {
        isPlaying = !isPlaying
        binding.btnPlayPause.text = if (isPlaying) "Pause" else "Play"

        if (isPlaying) {
            // Bắt đầu chạy các frame
            handler.post(updateFramesRunnable)
        } else {
            // Dừng chạy các frame
            handler.removeCallbacks(updateFramesRunnable)
        }
    }
    private fun loadFramesFromAssets() {
        try {
            val assetManager = assets
            val frameFiles = assetManager.list("golf_frame")?.sorted() ?: emptyList()

            for (fileName in frameFiles) {
                val inputStream = assetManager.open("golf_frame/$fileName")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                frameList.add(bitmap)
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi tải frame: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showNextFrame() {
        if (frameList.isEmpty()) return

        currentFrameIndex = (currentFrameIndex + 1) % frameList.size
        binding.imageview.setImageBitmap(frameList[currentFrameIndex])

        // Cập nhật vị trí seekbar tương ứng
        val progress = currentFrameIndex.toFloat() / (frameList.size - 1)
        binding.customSeekBar.progress = progress
    }
}