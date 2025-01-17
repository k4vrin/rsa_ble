package dev.kavrin.rsable.domain.model

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
sealed class Resource<out T, out E> {
    data class Success<out T>(val data: T) : Resource<T, Nothing>()
    data class Error<out E>(val cause: E? = null) : Resource<Nothing, E>()

    inline fun <R> fold(onSuccess: (T) -> R, onError: (E?) -> R): R =
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(cause)
        }

    inline fun <T, R, E> Resource<T, E>.map(transform: (T) -> R): Resource<R, E> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }

    inline fun <T, R, E> Resource<T, E>.flatMap(transform: (T) -> Resource<R, E>): Resource<R, E> =

        when (this) {
            is Success -> transform(data)
            is Error -> this
        }

    inline fun <T, E> Resource<T, E>.runCatching(block: () -> T): Resource<T, Throwable> =
        try {
            Success(block())
        } catch (e: Throwable) {
            Error(e)
        }

    inline fun <R, T : R, E> Resource<T, E>.getOrElse(onFailure: (E?) -> R): R {
        contract {
            callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
        }
        return when (this) {
            is Success -> data
            is Error -> onFailure(cause)
        }
    }


    companion object {
        fun <T> success(data: T): Resource<T, Nothing> = Success(data)
        fun <E> error(cause: E? = null): Resource<Nothing, E> = Error(cause)
    }

}