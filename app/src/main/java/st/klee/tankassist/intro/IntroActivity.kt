package st.klee.tankassist.intro

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_UNDEFINED
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide
import st.klee.tankassist.R

class IntroActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buttonBackFunction = BUTTON_BACK_FUNCTION_BACK
        isButtonCtaVisible = false

        val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNight = nightModeFlags == UI_MODE_NIGHT_YES || nightModeFlags == UI_MODE_NIGHT_UNDEFINED

        val backgroundColor =  if (isNight) R.color.intro_tos_night_dark else R.color.intro_tos
        val backgroundColorDark =  if (isNight) R.color.intro_tos_night else R.color.intro_tos_dark

        val tosSlide = FragmentSlide.Builder()
            .background(backgroundColor)
            .backgroundDark(backgroundColorDark)
            .fragment(TOSSlideFragment.newInstance())
            .build()
        addSlide(tosSlide)

        val settingsSlide = FragmentSlide.Builder()
            .background(backgroundColor)
            .backgroundDark(backgroundColorDark)
            .fragment(SettingsSlideFragment.newInstance())
            .build()
        addSlide(settingsSlide)
    }

    override fun onIntroFinish() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean(getString(R.string.pref_firstRun), false).apply()

        super.onIntroFinish()
    }
}
