package dev.kavrin.rsable.data.mappers

import android.bluetooth.le.ScanCallback
import dev.kavrin.rsable.domain.model.BleScanError

fun mapScanError(errorCode: Int): BleScanError {
    return when (errorCode) {
        ScanCallback.SCAN_FAILED_ALREADY_STARTED -> BleScanError.AlreadyStarted
        ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> BleScanError.BluetoothAdapterRegistrationFailed
        ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> BleScanError.InternalError
        ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> BleScanError.FeatureUnsupported
        ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> BleScanError.BluetoothResourcesUnavailable
        ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> BleScanError.ScanningTooFrequently
        else -> BleScanError.UnknownError(errorCode)
    }
}