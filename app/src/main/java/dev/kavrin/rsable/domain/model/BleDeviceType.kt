package dev.kavrin.rsable.domain.model

enum class BleDeviceType(val uuid: String) {
    HEART_RATE_MONITOR("0000180D-0000-1000-8000-00805f9b34fb"),
    PROXIMITY_MONITOR("00001802-0000-1000-8000-00805f9b34fb"),
    BLOOD_PRESSURE_MONITOR("00001810-0000-1000-8000-00805f9b34fb"),
    CYCLING_MONITOR("00001816-0000-1000-8000-00805f9b34fb"),
    RUNNING_MONITOR("00001814-0000-1000-8000-00805f9b34fb"),
    UNKNOWN(""); // For unknown devices

    companion object {
        fun fromUuid(uuid: String): BleDeviceType = entries.find { it.uuid.uppercase() == uuid.uppercase() } ?: UNKNOWN
    }
}