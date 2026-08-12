package com.xiaoke.pet

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        statusText = findViewById(R.id.statusText)
        startButton = findViewById(R.id.startButton)
        
        startButton.setOnClickListener {
            checkPermissionsAndStart()
        }
        
        updateStatus()
    }
    
    override fun onResume() {
        super.onResume()
        updateStatus()
    }
    
    private fun updateStatus() {
        val overlayOk = Settings.canDrawOverlays(this)
        val usageOk = hasUsageStatsPermission()
        val notifyOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
        
        val status = buildString {
            append("权限检查：\n\n")
            append("✓ 悬浮窗权限：${if (overlayOk) "已授予" else "未授予"}\n")
            append("✓ 使用情况权限：${if (usageOk) "已授予" else "未授予"}\n")
            append("✓ 通知权限：${if (notifyOk) "已授予" else "未授予"}\n\n")
            if (overlayOk && usageOk && notifyOk) {
                append("✅ 所有权限已就绪！")
            } else {
                append("⚠️ 请先授予所有权限")
            }
        }
        statusText.text = status
        
        startButton.isEnabled = overlayOk && usageOk
        startButton.text = if (isServiceRunning()) "停止桌宠" else "启动桌宠"
    }
    
    private fun checkPermissionsAndStart() {
        when {
            !Settings.canDrawOverlays(this) -> {
                Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            !hasUsageStatsPermission() -> {
                Toast.makeText(this, "请先授予使用情况权限", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
            else -> {
                toggleService()
            }
        }
    }
    
    private fun toggleService() {
        if (isServiceRunning()) {
            stopService(Intent(this, PetOverlayService::class.java))
            Toast.makeText(this, "桌宠已停止", Toast.LENGTH_SHORT).show()
        } else {
            startForegroundService(Intent(this, PetOverlayService::class.java))
            Toast.makeText(this, "桌宠已启动！", Toast.LENGTH_SHORT).show()
            finish()
        }
        updateStatus()
    }
    
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PetOverlayService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
