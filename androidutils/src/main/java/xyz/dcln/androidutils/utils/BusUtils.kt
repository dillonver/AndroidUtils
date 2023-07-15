package xyz.dcln.androidutils.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow


object BusUtils {
    /**
     * 1、Use MutableSharedFlow to implement the event channel, which is a powerful and efficient implementation that supports sending and receiving events in a coroutine.
     * 2、Support both sticky and non-sticky events. Sticky events are retained in a map (stickyEvents) and sent immediately to new subscribers upon subscription.
     * 3、Use LifecycleOwner to associate event subscriptions with the lifecycle of the component, which helps to avoid handling events after the component is destroyed and reduces the risk of memory leaks.
     * 4、Provide the sendEvent and sendEventSticky methods for sending events, and also provide the sendTag method for sending events with tags.
     * ----------------------------------------------------------------------------------------------------
     * 1、使用MutableSharedFlow来实现事件通道，这是一个强大且高效的实现，它支持在协程中发送和接收事件。
     * 2、同时支持粘性（sticky）和非粘性事件。粘性事件会保留在一个映射（stickyEvents）中，并在订阅时立即发送给新订阅者。
     * 3、使用了LifecycleOwner来关联事件订阅与组件的生命周期，这有助于避免在组件销毁后继续处理事件，从而降低内存泄漏的风险。
     * 4、提供了sendEvent和sendEventSticky方法来发送事件，同时还提供了sendTag方法来发送带有标签的事件。
     */
    @PublishedApi
    internal val sharedFlow =
        MutableSharedFlow<Event<Any>>(replay = 0, extraBufferCapacity = 1024)

    @PublishedApi
    internal val stickyEvents: MutableMap<String, Event<Any>> = mutableMapOf()

    inline fun <reified T> LifecycleOwner.receive(
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
        sticky: Boolean = false,
        noinline block: suspend CoroutineScope.(value: T) -> Unit
    ): Job {
        return receive(tags = emptyArray(), lifeEvent = lifeEvent, sticky = sticky, block = block)
    }

    fun LifecycleOwner.receive(
        tag: String,
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
        sticky: Boolean = false,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return receive<EventTag>(tags = arrayOf(tag), lifeEvent = lifeEvent, sticky = sticky) {
            block()
        }
    }

    inline fun <reified T> LifecycleOwner.receive(
        vararg tags: String = emptyArray(),
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
        sticky: Boolean = false,
        crossinline block: suspend CoroutineScope.(value: T) -> Unit
    ): Job {
        return receiveInternal(tags, lifeEvent, sticky, Dispatchers.Main.immediate, block)
    }



    inline fun <reified T> LifecycleOwner.receiveOnIO(
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
        sticky: Boolean = false,
        noinline block: suspend CoroutineScope.(value: T) -> Unit
    ): Job {
        return receiveOnIO(tags = emptyArray(), lifeEvent = lifeEvent, sticky = sticky, block = block)
    }

    fun LifecycleOwner.receiveOnIO(
        tag: String,
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
        sticky: Boolean = false,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return receiveOnIO<EventTag>(tags = arrayOf(tag), lifeEvent = lifeEvent, sticky = sticky) {
            block()
        }
    }
    inline fun <reified T> LifecycleOwner.receiveOnIO(
        vararg tags: String = emptyArray(),
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
        sticky: Boolean = false,
        crossinline block: suspend CoroutineScope.(value: T) -> Unit
    ): Job {
        return receiveInternal(tags, lifeEvent, sticky, Dispatchers.IO, block)
    }

    @PublishedApi
    internal inline fun <reified T> LifecycleOwner.receiveInternal(
        tags: Array<out String>,
        lifeEvent: Lifecycle.Event,
        sticky: Boolean,
        dispatcher: CoroutineDispatcher,
        crossinline block: suspend CoroutineScope.(value: T) -> Unit
    ): Job {
        val coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        return coroutineScope.launch {
            if (sticky) {
                stickyEvents.values.forEach { event ->
                    if (event.data is T && (tags.isEmpty() || tags.contains(event.tag))) {
                        withContext(dispatcher) {
                            block(event.data as T)
                        }
                    }
                }
            }
            sharedFlow.collect { event ->
                if (event.data is T && (tags.isEmpty() || tags.contains(event.tag))) {
                    withContext(dispatcher) {
                        block(event.data as T)
                    }
                }
            }
        }.apply {
            this@receiveInternal.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (lifeEvent == event) {
                        coroutineScope.cancel()
                    }
                }
            })
        }
    }

    private fun sendEvent(tag: String = "", event: Any) {
        val eventWrapper = Event(event, tag)
        CoroutineScope(Dispatchers.IO).launch {
            sharedFlow.emit(eventWrapper)
        }
    }

    private fun sendEventSticky(tag: String, event: Any) {
        require(tag.isNotEmpty()) { "Sticky events require a non-empty tag." }
        if (!stickyEvents.containsKey(tag) || stickyEvents[tag]?.data != event) {
            val eventWrapper = Event(event, tag)
            stickyEvents[tag] = eventWrapper
            CoroutineScope(Dispatchers.IO).launch {
                sharedFlow.emit(eventWrapper)
            }
        }
    }

    private class EventTag

    @PublishedApi
    internal class Event<T>(val data: T, val tag: String? = null)

    fun Any.sendEvent(event: Any) =
        BusUtils.sendEvent("", event)

    fun Any.sendEvent(tag: String = "", event: Any) =
        BusUtils.sendEvent(tag, event)

    fun Any.sendEventSticky(tag: String, event: Any) =
        BusUtils.sendEventSticky(tag, event)

    fun Any.sendTag(tag: String, sticky: Boolean = false) =
        if (sticky) BusUtils.sendEventSticky(tag, EventTag()) else BusUtils.sendEvent(
            tag,
            EventTag()
        )
}