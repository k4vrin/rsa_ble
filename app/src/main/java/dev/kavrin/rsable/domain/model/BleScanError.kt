package dev.kavrin.rsable.domain.model

sealed interface BleScanError {
    data object AlreadyStarted : BleScanError
    data object BluetoothAdapterRegistrationFailed : BleScanError
    data object InternalError : BleScanError
    data object FeatureUnsupported : BleScanError
    data object BluetoothResourcesUnavailable : BleScanError
    data object ScanningTooFrequently : BleScanError
    data object StopScanFailed : BleScanError
    data class UnknownError(val code: Int) : BleScanError
}