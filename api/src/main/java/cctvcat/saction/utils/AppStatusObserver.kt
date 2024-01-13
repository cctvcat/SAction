package cctvcat.saction.utils

import android.app.ActivityManagerHidden
import android.os.Build
import cctvcat.saction.model.AppStatusEvent
import rikka.hidden.compat.ActivityManagerApis
import rikka.hidden.compat.adapter.ProcessObserverAdapter
import rikka.hidden.compat.adapter.UidObserverAdapter

private class ProcessObserver : ProcessObserverAdapter() {

    override fun onForegroundActivitiesChanged(pid: Int, uid: Int, foregroundActivities: Boolean) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onForegroundActivitiesChanged",
                uid,
                pid,
                arrayOf(foregroundActivities)
            )
        )
    }

    override fun onProcessDied(pid: Int, uid: Int) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onProcessDied",
                uid,
                pid,
            )
        )
    }

    override fun onProcessStateChanged(pid: Int, uid: Int, procState: Int) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onProcessStateChanged",
                uid,
                pid,
                arrayOf(procState)
            )
        )
    }

    override fun onForegroundServicesChanged(pid: Int, uid: Int, serviceTypes: Int) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onForegroundServicesChanged",
                uid,
                pid,
                arrayOf(serviceTypes)
            )
        )
    }

}

private class UidObserver : UidObserverAdapter() {

    override fun onUidActive(uid: Int) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onUidActive",
                uid,
            )
        )
    }

    override fun onUidCachedChanged(uid: Int, cached: Boolean) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onUidCachedChanged",
                uid,
                args = arrayOf(cached),
            )
        )
    }

    override fun onUidIdle(uid: Int, disabled: Boolean) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onUidIdle",
                uid,
                args = arrayOf(disabled),
            )
        )
    }

    override fun onUidGone(uid: Int, disabled: Boolean) {
        AppStatusObserver.instance.trigger(
            AppStatusEvent(
                type = "onUidGone",
                uid,
                args = arrayOf(disabled),
            )
        )
    }

}

class AppStatusObserver private constructor() {

    companion object {
        const val TYPE_SHELL = 0
        const val TYPE_XPOSED = 1

        const val FOREGROUND_ACTIVITIES_CHANGED = "onForegroundActivitiesChanged"
        const val PROCESS_DIED = "onProcessDied"
        const val PROCESS_STATE_CHANGED = "onProcessStateChanged"
        const val FOREGROUND_SERVICES_CHANGED = "onForegroundServicesChanged"

        const val UID_ACTIVE = "onUidActive"
        const val UID_CACHED_CHANGED = "onUidCachedChanged"
        const val UID_IDLE = "onUidIdle"
        const val UID_GONE = "onUidGone"

        const val APP_DEACTIVATED = "onAppDeactivated"
        const val APP_ACTIVE = "onAppActive"
        const val APP_SHOW = "onAppShow"
        const val APP_HIDE = "onAppHide"

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            try {
                ActivityManagerApis.registerProcessObserver(ProcessObserver())
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }

            if (Build.VERSION.SDK_INT >= 26) {
                try {
                    var flags =
                        ActivityManagerHidden.UID_OBSERVER_GONE or ActivityManagerHidden.UID_OBSERVER_IDLE or ActivityManagerHidden.UID_OBSERVER_ACTIVE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        flags = flags or ActivityManagerHidden.UID_OBSERVER_CACHED
                    }

                    ActivityManagerApis.registerUidObserver(
                        UidObserver(),
                        flags,
                        ActivityManagerHidden.PROCESS_STATE_UNKNOWN,
                        null
                    )
                } catch (tr: Throwable) {
                    tr.printStackTrace()
                }
            }

            AppStatusObserver()
        }
    }

    private val mListenerMap = hashMapOf<String, HashSet<(AppStatusEvent) -> Unit>>()

    private fun callback(type: String, evt: AppStatusEvent) {
        mListenerMap[type]?.let { cbs ->
            cbs.forEach {
                it.invoke(evt)
            }
        }
    }

    internal fun trigger(evt: AppStatusEvent) {
        slog("AppStatusObserver::trigger - $evt")
        callback(evt.type, evt)
        when (evt.type) {
            PROCESS_DIED -> callback(APP_DEACTIVATED, evt)

            UID_GONE -> callback(APP_DEACTIVATED, evt)

            PROCESS_STATE_CHANGED -> callback(APP_ACTIVE, evt)

            UID_ACTIVE -> callback(APP_ACTIVE, evt)

            UID_IDLE -> callback(APP_ACTIVE, evt)

            UID_CACHED_CHANGED -> {
                if (evt.args.isNotEmpty() && evt.args[0] == false) {
                    callback(APP_ACTIVE, evt)
                }
            }

            FOREGROUND_ACTIVITIES_CHANGED -> {
                callback(APP_ACTIVE, evt)
                if (evt.args.isNotEmpty() && evt.args[0] == true) {
                    callback(APP_SHOW, evt)
                } else {
                    callback(APP_HIDE, evt)
                }
            }
        }
    }

    fun addEventListener(type: String, callback: ((AppStatusEvent) -> Unit)) {
        var listeners = mListenerMap[type]
        if (listeners == null) {
            listeners = HashSet()
            mListenerMap[type] = listeners
        }
        listeners.add(callback)
    }

    fun removeEventListener(type: String, callback: ((AppStatusEvent) -> Unit)) {
        mListenerMap[type]?.remove(callback)
    }

}