package dev.kavrin.rsable.domain.model


sealed class BleScanResource<out T> {
    data object Loading : BleScanResource<Nothing>()
    data class Success<T>(val value: T) : BleScanResource<T>()
    data class Error(val error: BleScanError) : BleScanResource<Nothing>()

    inline fun <R> flatMap(transform: (T) -> BleScanResource<R>): BleScanResource<R> = when (this) {
        is Loading -> Loading
        is Success -> transform(value)
        is Error -> Error(error)
    }
}