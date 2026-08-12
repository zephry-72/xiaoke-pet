package com.xiaoke.pet

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class PetOverlayService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var webView: WebView
    private lateinit var gestureDetector: PetGestureDetector
    private lateinit var appMonitor: AppUsageMonitor
    private lateinit var supabaseClient: SupabaseClient
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var lastX = 0
    private var lastY = 0
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    override fun onCreate() {
        super.onCreate()
        
        // 创建前台通知
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)
        
        // 初始化WindowManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // 初始化Supabase客户端
        supabaseClient = SupabaseClient(this)
        
        // 创建悬浮窗视图
        createOverlayView()
        
        // 初始化手势检测器
        gestureDetector = PetGestureDetector(
            onSingleTap = { x, y -> handleSingleTap(x, y) },
            onDoubleTap = { x, y -> handleDoubleTap(x, y) },
            onLongPress = { x, y -> handleLongPress(x, y) },
            onDrag = { isDragging -> if (isDragging) playAnimation("drag") else playAnimation("idle") }
        )
        
        // 初始化App监听器
        appMonitor = AppUsageMonitor(this) { packageName, appName ->
            handleAppSwitch(packageName, appName)
        }
        appMonitor.start()
        
        // 启动状态同步
        startStateSyncLoop()
    }
    
    private fun createOverlayView() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_pet, null)
        webView = overlayView.findViewById(R.id.petWebView)
        
        // 配置WebView
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            setBackgroundColor(0x00000000) // 透明背景
            webViewClient = WebViewClient()
            loadUrl("file:///android_asset/pet.html")
        }
        
        // 配置窗口参数
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }
        
        lastX = params.x
        lastY = params.y
        
        // 添加触摸监听
        overlayView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                }
                MotionEvent.ACTION_UP -> {
                    lastX = params.x
                    lastY = params.y
                }
            }
            true
        }
        
        // 添加到窗口
        windowManager.addView(overlayView, params)
        
        // 默认播放待机动画
        playAnimation("idle")
    }
    
    private fun handleSingleTap(x: Int, y: Int) {
        serviceScope.launch {
            supabaseClient.logGesture("single_tap", x, y)
        }
        playAnimation("shy")
        serviceScope.launch {
            delay(1500)
            playAnimation("idle")
        }
    }
    
    private fun handleDoubleTap(x: Int, y: Int) {
        serviceScope.launch {
            supabaseClient.logGesture("double_tap", x, y)
        }
        playAnimation("happy")
        serviceScope.launch {
            delay(2000)
            playAnimation("idle")
        }
    }
    
    private fun handleLongPress(x: Int, y: Int) {
        serviceScope.launch {
            supabaseClient.logGesture("long_press", x, y)
        }
        playAnimation("happy")
    }
    
    private fun handleAppSwitch(packageName: String, appName: String) {
        serviceScope.launch {
            supabaseClient.logAppUsage(packageName, appName)
        }
        
        // 根据App类型播放动画
        val animation = when {
            packageName.contains("taobao") || packageName.contains("jd") -> "shop"
            packageName.contains("douyin") || packageName.contains("tiktok") -> "jealous"
            packageName.contains("wechat") || packageName.contains("qq") -> "work"
            packageName.contains("music") || packageName.contains("spotify") -> "music"
            packageName.contains("game") -> "play"
            packageName.contains("wps") || packageName.contains("office") -> "study"
            else -> "work"
        }
        
        playAnimation(animation)
        serviceScope.launch {
            delay(3000)
            playAnimation("idle")
        }
    }
    
    private fun playAnimation(animationName: String) {
        webView.post {
            webView.evaluateJavascript("playAnimation('$animationName')", null)
        }
    }
    
    private fun startStateSyncLoop() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val state = supabaseClient.getLatestPetState()
                    state?.let {
                        if (it.expression.isNotEmpty()) {
                            playAnimation(it.expression)
                        }
                        if (it.reactionText.isNotEmpty()) {
                            showReaction(it.reactionText)
                        }
                        supabaseClient.markStateAsRead(it.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30000) // 每30秒同步一次
            }
        }
    }
    
    private fun showReaction(text: String) {
        webView.post {
            webView.evaluateJavascript("showReaction('$text')", null)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pet_service",
                "小克桌宠",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持小克在屏幕上陪伴你"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, "pet_service")
            .setContentTitle("小克正在陪伴你")
            .setContentText("点击返回设置")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        appMonitor.stop()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
