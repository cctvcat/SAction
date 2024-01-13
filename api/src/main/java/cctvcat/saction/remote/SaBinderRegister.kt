package cctvcat.saction.remote

import android.os.Binder

object SaBinderRegister {

    private val mBinderMap = hashMapOf<String, Binder>()

    fun getRegisteredBinder(key: String) = mBinderMap[key]

    fun registerBinder(key: String, binder: Binder) {
        mBinderMap[key] = binder
    }

}