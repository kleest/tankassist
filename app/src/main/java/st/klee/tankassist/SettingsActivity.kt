package st.klee.tankassist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import st.klee.tankassist.misc.Permissions

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val RESULT_CHANGED_RADIUS = RESULT_FIRST_USER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.app_bar))

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // if location is disabled, reset the preference
                preferenceManager.findPreference<SwitchPreferenceCompat>(getString(R.string.pref_requestCurrentLocationOnResume))!!.isChecked = false
            }

            preferenceManager.findPreference<SwitchPreferenceCompat>(getString(R.string.pref_requestCurrentLocationOnResume))!!
                .onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                if (newValue == true) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            // dialog about WHY we request location permissions
                             Permissions.requestLocationPermission(requireContext(), requestPermissionLauncher)
                        }
                        else {
                            // request permission
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        false
                    } else
                        true
                } else
                    true
            }

            preferenceManager.findPreference<SeekBarPreference>(getString(R.string.pref_searchRadius))!!
                .onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                    requireActivity().setResult(RESULT_CHANGED_RADIUS)
                    true
            }
        }

        private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted)
                    preferenceManager.findPreference<SwitchPreferenceCompat>(getString(R.string.pref_requestCurrentLocationOnResume))!!.isChecked = true
            }
    }
}