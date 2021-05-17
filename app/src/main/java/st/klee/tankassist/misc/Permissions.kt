package st.klee.tankassist.misc

import android.Manifest
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import st.klee.tankassist.R

object Permissions {
    fun requestLocationPermission(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>) {
        // dialog about WHY we request location permissions
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.dialog_perm_location_title))
            .setMessage(context.getString(R.string.dialog_perm_location_message))
            .setNegativeButton(context.getString(R.string.dialog_perm_location_btn_negative)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.dialog_perm_location_btn_positive)) { dialog, which ->  // request permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                dialog.dismiss()
            }
            .show()
    }
}