package com.xiaoke.pet

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent

class PetGestureDetector(
    private val onSingleTap: (x: Int, y: Int) -> Unit,
    private val onDoubleTap: (x: Int, y: Int) -> Unit,
    private val onLongPress: (x: Int, y: Int) -> Unit,
    private val onDrag: (isDragging: Boolean) -> Unit
) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var lastTapTime = 0L
    private var tapCount = 0
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var isDragging = false
    
    private val longPressRunnable = Runnable {
        if (!isDragging) {
            onLongPress(downX.toInt(), downY.toInt())
        }
    }
    
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                downTime = System.currentTimeMillis()
                isDragging = false
                
                // 启动长按检测
                handler.postDelayed(longPressRunnable, 600)
            }
            
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downX
                val dy = event.rawY - downY
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                
                if (distance > 10 && !isDragging) {
                    isDragging = true
                    handler.removeCallbacks(longPressRunnable)
                    onDrag(true)
                }
            }
            
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                
                if (!isDragging) {
                    val currentTime = System.currentTimeMillis()
                    
                    if (currentTime - lastTapTime < 300) {
                        // 双击
                        tapCount++
                        if (tapCount == 2) {
                            onDoubleTap(event.rawX.toInt(), event.rawY.toInt())
                            tapCount = 0
                        }
                    } else {
                        // 单击
                        tapCount = 1
                        handler.postDelayed({
                            if (tapCount == 1) {
                                onSingleTap(event.rawX.toInt(), event.rawY.toInt())
                            }
                            tapCount = 0
                        }, 300)
                    }
                    lastTapTime = currentTime
                } else {
                    onDrag(false)
                }
                
                isDragging = false
            }
        }
        return true
    }
}