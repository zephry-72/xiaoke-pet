package com.xiaoke.pet

import android.content.Context
import com.google.gson.Gson
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class GestureLog(
    val gesture_type: String,
    val position_x: Int,
    val position_y: Int,
    val extra_data: String? = null
)

@Serializable
data class AppUsage(
    val package_name: String,
    val app_name: String,
    val duration_seconds: Int = 0
)

@Serializable
data class PetState(
    val id: Long = 0,
    val reaction_text: String = "",
    val expression: String = "",
    val trigger_event: String = "",
    val is_read: Boolean = false
)

class SupabaseClient(private val context: Context) {
    
    private val client = createSupabaseClient(
        supabaseUrl = "https://loclplmnrqmjjymstcvy.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxvY2xwbG1ucnFtamp5bXN0Y3Z5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU3MTk3MzUsImV4cCI6MjEwMTI5NTczNX0.rFJUPNPMIpP_P_h_sYiLDIXuS29NoiOFMFEoLrfujlo"
    ) {
        install(Postgrest)
    }
    
    private val gson = Gson()
    
    suspend fun logGesture(gestureType: String, x: Int, y: Int, extraData: Map<String, Any>? = null) {
        try {
            client.from("gesture_log").insert(
                GestureLog(
                    gesture_type = gestureType,
                    position_x = x,
                    position_y = y,
                    extra_data = extraData?.let { gson.toJson(it) }
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun logAppUsage(packageName: String, appName: String, duration: Int = 0) {
        try {
            client.from("app_usage").insert(
                AppUsage(
                    package_name = packageName,
                    app_name = appName,
                    duration_seconds = duration
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun getLatestPetState(): PetState? {
        return try {
            val result = client.from("pet_state")
                .select {
                    filter {
                        eq("is_read", false)
                    }
                    order("created_at", ascending = false)
                    limit(1)
                }
                .decodeList<PetState>()
            result.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun markStateAsRead(id: Long) {
        try {
            client.from("pet_state").update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
