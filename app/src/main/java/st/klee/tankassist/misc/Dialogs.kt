package st.klee.tankassist.misc

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import st.klee.tankassist.R

object Dialogs {
    fun showLicense(context: Context) {
        val msg = context.assets.open("LICENSE").bufferedReader().use { it.readText() }
        MaterialAlertDialogBuilder(context)
            .setMessage(msg)
            .setNeutralButton(R.string.dialog_ok, null)
            .show()
    }
    fun showPrivacy(context: Context) {
        val msg = context.assets.open("PRIVACY").bufferedReader().use { it.readText() }
        MaterialAlertDialogBuilder(context)
            .setMessage(msg)
            .setNeutralButton(R.string.dialog_ok, null)
            .show()
    }
}