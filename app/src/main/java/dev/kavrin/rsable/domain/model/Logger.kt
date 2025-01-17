package dev.kavrin.rsable.domain.model

import dev.kavrin.rsable.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object Logger {
    enum class Level { DEBUG, INFO, WARN, ERROR }

    private val isDebugBuild = BuildConfig.DEBUG

    fun log(
        level: Level,
        module: String,
        operation: String,
        status: String,
        uuid: UUID? = null,
        message: String,
        cause: Throwable? = null
    ) {
        if (!isDebugBuild) return // Do not log in release builds

        val timestamp = System.currentTimeMillis().toFormattedTimestamp()
        val uuidStr = uuid?.toString() ?: "N/A"
        val causeStr = cause?.message ?: "N/A"

        val logMessage = "[${level.name}] [$timestamp] [BLE] [$module] Operation: $operation, Status: $status, UUID: $uuidStr, Message: $message, Cause: $causeStr"
        println(logMessage)
    }
}
fun Long.toFormattedTimestamp(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return dateFormat.format(Date(this))
}