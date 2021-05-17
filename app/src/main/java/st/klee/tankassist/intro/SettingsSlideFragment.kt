package st.klee.tankassist.intro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.heinrichreimersoftware.materialintro.app.SlideFragment
import st.klee.tankassist.R
import st.klee.tankassist.misc.Dialogs
import st.klee.tankassist.misc.Permissions

class SettingsSlideFragment : SlideFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.intro_slide_settings, container, false)
        v.findViewById<MaterialButton>(R.id.btn_location)!!.setOnClickListener {
            requestLocationPermission()
        }
        v.findViewById<MaterialCheckBox>(R.id.check_location_onresume)!!.setOnCheckedChangeListener { _, isChecked ->
            // save settings
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                .putBoolean(getString(R.string.pref_requestCurrentLocationOnResume), isChecked)
                .apply()
        }
        return v.rootView
    }

    override fun canGoForward(): Boolean {
        // when location features wanted, check if required permissions granted
        if (requireView().findViewById<MaterialCheckBox>(R.id.check_location_onresume)!!.isChecked) {
            // location features requested, require permissions
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            )
                return false
        }
        return true
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // dialog about WHY we request location permissions
                Permissions.requestLocationPermission(requireContext(), requestPermissionLauncher)
            } else {
                // request permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else
            // permission granted, enable checkbox
            requireView().findViewById<MaterialCheckBox>(R.id.check_location_onresume)!!.isEnabled =
                true
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            requireView().findViewById<MaterialCheckBox>(R.id.check_location_onresume)!!.isEnabled =
                isGranted
        }

    companion object {
        fun newInstance(): SettingsSlideFragment {
            return SettingsSlideFragment()
        }
    }
}