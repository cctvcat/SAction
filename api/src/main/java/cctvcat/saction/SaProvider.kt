package cctvcat.saction

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import cctvcat.saction.model.SaContainer
import cctvcat.saction.utils.slog

class SaProvider : ContentProvider() {

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        try {
            slog("SaProvider::call - received and start to bind")
            if (method == "sendSaContainer") {
                extras!!.classLoader = SaContainer::class.java.classLoader
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable("container", SaContainer::class.java)
                } else {
                    extras.getParcelable("container")
                }?.let {
                    SAction.bindService(it.binder)
                }
            }
        } catch (e: Throwable) {
            slog("SaProvider::call - error: ${e.message}")
            e.printStackTrace()
        }
        return Bundle()
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return -1
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return -1
    }

}