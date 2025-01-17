package dev.kavrin.rsable.domain.model

import dev.kavrin.rsable.domain.model.BleScanResource.Loading

sealed class BleConnectionResource<T> {
    data object Loading : BleConnectionResource<Nothing>()
    data class Success<T>(val data: T) : BleConnectionResource<T>()
    data class Error<T>(val message: T) : BleConnectionResource<Nothing>()

    companion object {
        fun createLoading() = Loading
        fun <T> createSuccess(value: T) =
            Success(value)
        fun <T> createError(message: T) = Error(message)
    }
}