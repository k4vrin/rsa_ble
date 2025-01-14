package dev.kavrin.rsable.domain.model

/**
 * A sealed class representing the different states of a Bluetooth Low Energy (BLE) scan operation.
 * It encapsulates the possible outcomes: loading, success, or error.
 *
 * This class follows a resource-oriented design pattern, commonly used for handling asynchronous
 * operations that can have different states during their lifecycle. It's similar to the
 * Result or Resource class patterns often seen in other contexts.
 *
 * @param T The type of data that will be returned upon successful completion of the scan.
 */
sealed class BleScanResource<T> {
    class Loading <T> : BleScanResource<T>()
    data class Success<T>(val value: T) : BleScanResource<T>()
    data class Error<T>(val error: BleScanError) : BleScanResource<T>()
    companion object {
        fun <T> createLoading() = Loading<T>()
        fun <T> createSuccess(value: T) = Success(value)
        fun <T> createError(error: BleScanError) = Error<T>(error)
    }
}