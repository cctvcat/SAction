package cctvcat.saction.demo_app.remote

import android.util.Log
import cctvcat.saction.remote.ISaEntry
import cctvcat.saction.remote.SaBinderRegister

class RemoteService : ISaEntry {

    override fun onBeforeConnect(register: SaBinderRegister, args: Array<String>) {
        register.registerBinder("TestService", TestService())
        Log.d("S_ACTION_LOG", "onBeforeConnect: registered")
    }

    override fun onConnected(register: SaBinderRegister, args: Array<String>) {
        Log.d("S_ACTION_LOG", "onConnected")
    }

}