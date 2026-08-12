package com.xiaoke.pet

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import java.util.*

class AppUsageMonitor(
    private val context: Context,
    private val onAppSwitch: (packageName: String, appName: String) -> Unit
) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var lastPackageName = ""
    private var isRunning = false
    
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            if (isRunning) {
                handler.postDelayed(this, 3000) // 每3秒检查一次
            }
        }
    }
    
    fun start() {
        if (!isRunning) {
            isRunning = true
            handler.post(checkRunnable)
        }
    }
    
    fun stop() {
        isRunning = false
        handler.removeCallbacks(checkRunnable)
    }
    
    private fun checkForegroundApp() {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 10, // 最近10秒
            currentTime
        )
        
        if (stats != null && stats.isNotEmpty()) {
            val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
            val currentApp = sortedStats.firstOrNull()
            
            currentApp?.let {
                val packageName = it.packageName
                if (packageName != lastPackageName && packageName != context.packageName) {
                    lastPackageName = packageName
                    val appName = getAppName(packageName)
                    onAppSwitch(packageName, appName)
                }
            }
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}