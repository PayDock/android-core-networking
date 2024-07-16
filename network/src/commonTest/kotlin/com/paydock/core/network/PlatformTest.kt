package com.paydock.core.network

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

expect fun runBlockingTest(block: suspend CoroutineScope.() -> Unit)

expect val testCoroutineContext: CoroutineContext