package cctvcat.saction

import android.os.IBinder
import cctvcat.saction.remote.SaService
import cctvcat.saction.utils.AppProcessLauncher
import cctvcat.saction.utils.slog

object SAction {

    private var mSaService: ISaService? = null

    private val DEATH_RECIPIENT = {
        mSaService = null
    }

    internal fun bindService(binder: IBinder) {
        mSaService = ISaService.Stub.asInterface(binder)
        slog("SAction::bindService - success")

        try {
            binder.linkToDeath(DEATH_RECIPIENT, 0)
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }

        try {
            binder.unlinkToDeath(DEATH_RECIPIENT, 0)
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
    }

    fun getStartCommand(
        packageName: String,
        packageClassPath: String,
        invokeClassName: String,
        version: String,
        processNameSuffix: String = "s_action_process",
        args: Array<String> = arrayOf(),
        userId: Int = 0,
    ) = AppProcessLauncher(
        packageName = packageName,
        packageClassPath = packageClassPath,
        className = SaService::class.java.name,
        processNameSuffix = processNameSuffix,
        args = arrayOf(
            packageName,
            packageClassPath,
            invokeClassName,
            version,
            processNameSuffix,
            userId.toString(),
            args.joinToString(" ")
        )
    ).getCommandString()

    fun start(
        packageName: String,
        packageClassPath: String,
        invokeClassName: String,
        version: String,
        processNameSuffix: String = "s_action_process",
        args: Array<String> = arrayOf(),
        userId: Int = 0,
    ) = AppProcessLauncher(
        packageName = packageName,
        packageClassPath = packageClassPath,
        className = SaService::class.java.name,
        processNameSuffix = processNameSuffix,
        args = arrayOf(
            packageName,
            packageClassPath,
            invokeClassName,
            version,
            processNameSuffix,
            userId.toString(),
            args.joinToString(" ")
        )
    ).launch()

    fun exit() {
        if (isAlive()) {
            try {
                mSaService!!.exit()
                mSaService = null
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun isAlive() = mSaService?.isAlive == true

    fun getVersion() = mSaService?.version

    fun getRegisterBinder(key: String) = mSaService?.getRegisteredBinder(key)

}