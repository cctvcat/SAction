package cctvcat.saction.utils

import android.os.FileObserver
import java.io.File
import java.util.*

internal interface ApkChangedListener {
    fun onApkChanged()
}

private val mObservers = Collections.synchronizedMap(HashMap<String, ApkChangedObserver>())

internal object ApkChangedObservers {

    @JvmStatic
    fun start(apkPath: String, listener: ApkChangedListener) {
        val path = File(apkPath).parent!!
        val observer = mObservers.getOrPut(path) {
            ApkChangedObserver(path).apply {
                startWatching()
            }
        }
        observer.addListener(listener)
    }

    @JvmStatic
    fun stop(listener: ApkChangedListener) {
        val pathToRemove = mutableListOf<String>()

        for ((path, observer) in mObservers) {
            observer.removeListener(listener)

            if (!observer.hasListeners()) {
                pathToRemove.add(path)
            }
        }

        for (path in pathToRemove) {
            mObservers.remove(path)?.stopWatching()
        }
    }

}

internal class ApkChangedObserver(path: String) : FileObserver(path, DELETE) {

    private val mListeners = mutableSetOf<ApkChangedListener>()

    fun addListener(listener: ApkChangedListener): Boolean {
        return mListeners.add(listener)
    }

    fun removeListener(listener: ApkChangedListener): Boolean {
        return mListeners.remove(listener)
    }

    fun hasListeners(): Boolean {
        return mListeners.isNotEmpty()
    }

    override fun onEvent(evt: Int, path: String?) {
        if ((evt and 0x00008000 /* IN_IGNORED */) != 0 || path == null) {
            return
        }

        if (path == "base.apk") {
            stopWatching()
            ArrayList(mListeners).forEach { it.onApkChanged() }
        }
    }

}