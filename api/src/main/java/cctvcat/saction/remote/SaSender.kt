package cctvcat.saction.remote

import android.content.IContentProvider
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import cctvcat.saction.model.AppStatusEvent
import cctvcat.saction.model.SaContainer
import cctvcat.saction.utils.AppStatusObserver
import cctvcat.saction.utils.IContentProviderUtils
import cctvcat.saction.utils.slog
import rikka.hidden.compat.ActivityManagerApis
import rikka.hidden.compat.PackageManagerApis

internal object SaSender {

    private var mBinder: Binder? = null
    private var mPackageName = ""
    private var mPackageUid: Int = -1
    private var mUserId: Int = -1
    private var mLock = Any()
    private var mSent = false

    private val mAppActiveCallback: ((AppStatusEvent) -> Unit) = {
        if (it.uid == mPackageUid) {
            synchronized(mLock) {
                if (!mSent) {
                    sendBinder(mBinder!!, mPackageName, mUserId)
                }
            }
        }
    }

    private val mAppDeactivatedCallback: ((AppStatusEvent) -> Unit) = {
        if (it.uid == mPackageUid) {
            synchronized(mLock) {
                mSent = false
            }
        }
    }

    private fun sendBinder(binder: Binder, packageName: String, userId: Int, retry: Boolean = true) {
        var provider: IContentProvider? = null
        val token: IBinder? = null
        val name = "$packageName.saction"

        try {
            provider = ActivityManagerApis.getContentProviderExternal(name, userId, token, name)
            if (provider == null) {
                return
            }

            if (!provider.asBinder().pingBinder()) {
                if (retry) {
                    ActivityManagerApis.forceStopPackageNoThrow(packageName, userId)
                    Thread.sleep(1000)
                    sendBinder(binder, packageName, userId, false)
                }
                return
            }

            val extra = Bundle()
            extra.putParcelable("container", SaContainer(binder))
            IContentProviderUtils.call(provider, null, name, "sendSaContainer", null, extra)
            slog("SaSender::sendBinder - sent binder to target app")
        } catch (e: Throwable) {
            slog("SaSender::sendBinder - error: ${e.message}")
            e.printStackTrace()
        } finally {
            provider?.let {
                ActivityManagerApis.removeContentProviderExternal(name, token)
            }
        }
    }

    fun register(binder: Binder, packageName: String, userId: Int) {
        mBinder = binder
        mPackageName = packageName
        mPackageUid = PackageManagerApis.getPackageUid(packageName, 0, userId)
        mUserId = userId

        AppStatusObserver.instance.addEventListener("onAppActive", mAppActiveCallback)
        AppStatusObserver.instance.addEventListener("onAppDeactivated", mAppDeactivatedCallback)
        sendBinder(binder, packageName, userId)
    }

}