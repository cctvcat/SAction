package cctvcat.saction.remote

import android.os.Build
import android.os.Looper
import cctvcat.saction.ISaService
import cctvcat.saction.utils.ApkChangedListener
import cctvcat.saction.utils.ApkChangedObservers
import cctvcat.saction.utils.slog
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.hidden.compat.PackageManagerApis
import kotlin.system.exitProcess

class SaService : ISaService.Stub() {

    private lateinit var mPackageName: String
    private lateinit var mClassName: String
    private lateinit var mVersion: String
    private lateinit var mProcessNameSuffix: String
    private lateinit var mArgs: Array<String>
    private var mUserId: Int = -1

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                slog("SaService::main - start to run service")
                Looper.prepare()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    HiddenApiBypass.addHiddenApiExemptions("L")
                }

                val packageName = args[0]
                val classPath = args[1]
                val userId = args[5].toInt()
                ApkChangedObservers.start(classPath, object : ApkChangedListener {
                    override fun onApkChanged() {
                        if (PackageManagerApis.getApplicationInfoNoThrow(packageName, 0, userId) == null) {
                            slog("ApkChangedListener::onApkChanged - uninstalled and will exit")
                            exitProcess(-1)
                        }
                    }
                })

                val service = SaService()
                service.mPackageName = packageName
                service.mClassName = args[2]
                service.mUserId = userId
                service.mVersion = args[3]
                service.mProcessNameSuffix = args[4]
                service.mArgs = args.takeLast(5).toTypedArray()

                val cls = Class.forName(service.mClassName)
                val constructor = cls.getDeclaredConstructor()
                constructor.isAccessible = true
                val instance = constructor.newInstance() as ISaEntry
                instance.onBeforeConnect(SaBinderRegister, service.mArgs)
                SaSender.register(service, packageName, userId)
                instance.onConnected(SaBinderRegister, service.mArgs)

                slog("SaService::main - started successfully")
                Looper.loop()
            } catch (tr: Throwable) {
                slog("SaService::main - error: ${tr.message}")
                tr.printStackTrace()
            }
        }
    }

    override fun exit() =  exitProcess(0)

    override fun isAlive() = true

    override fun getVersion() = mVersion

    override fun getRegisteredBinder(key: String) = SaBinderRegister.getRegisteredBinder(key)

}