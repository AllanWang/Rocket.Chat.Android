package chat.rocket.android.util.extension

import chat.rocket.android.core.lifecycle.CancelStrategy
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Launches a coroutine on the UI context.
 *
 * @param strategy a CancelStrategy for canceling the coroutine job
 */
fun launchUI(strategy: CancelStrategy, block: suspend CoroutineScope.() -> Unit): Job {
    return launch(context = UI + strategy.jobs, block = block)
}

val UI = Dispatchers.Main

val CommonPool = Dispatchers.Default

val DefaultDispatcher = Dispatchers.Default

// Temp bindings. None of these should actually be global
fun launch(
        context: CoroutineContext = kotlin.coroutines.EmptyCoroutineContext,
        start: CoroutineStart = kotlinx.coroutines.CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
) = GlobalScope.launch(context, start, block)