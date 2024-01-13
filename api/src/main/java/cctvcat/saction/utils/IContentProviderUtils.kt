package cctvcat.saction.utils

import kotlin.Throws
import android.content.IContentProvider
import android.os.Bundle
import android.os.Build
import android.content.AttributionSource
import android.os.RemoteException
import android.system.Os

object IContentProviderUtils {

    @Throws(RemoteException::class)
    fun call(
        provider: IContentProvider,
        callingPkg: String?,
        authority: String?,
        method: String?,
        arg: String?,
        extras: Bundle?
    ): Bundle {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            provider.call(
                AttributionSource.Builder(Os.getuid()).setPackageName(callingPkg).build(),
                authority,
                method,
                arg,
                extras
            )
        } else if (Build.VERSION.SDK_INT >= 30) {
            provider.call(callingPkg, null as String?, authority, method, arg, extras)
        } else if (Build.VERSION.SDK_INT >= 29) {
            provider.call(callingPkg, authority, method, arg, extras)
        } else {
            provider.call(callingPkg, method, arg, extras)
        }
    }

}