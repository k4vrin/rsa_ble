package dev.kavrin.rsable.util

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

data class CoroutineDispatchers(
    val database: CoroutineDispatcher,
    val disk: CoroutineDispatcher,
    val network: CoroutineDispatcher,
    val ui: CoroutineDispatcher,
)


val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
    Log.e("coroutineExceptionHandler", "Exception in context: $coroutineContext", throwable)
}

fun CoroutineScope.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    exceptionHandler: CoroutineExceptionHandler = coroutineExceptionHandler,
    body: suspend CoroutineScope.() -> Unit,
): Job {
    return this.launch(context + exceptionHandler, block = body)
}