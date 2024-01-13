package cctvcat.saction.remote

interface ISaEntry {

    fun onBeforeConnect(register: SaBinderRegister, args: Array<String>)

    fun onConnected(register: SaBinderRegister, args: Array<String>)

}