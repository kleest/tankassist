package st.klee.tankassist

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import st.klee.tankassist.misc.Dialogs


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(findViewById(R.id.app_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val nightModeEnabled = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        val v = findViewById<CoordinatorLayout>(R.id.main)

        val versionItem: Element = Element()
            .setTitle(
                String.format(
                    getString(R.string.about_version),
                    BuildConfig.VERSION_NAME
                )
            )

        val copyright: Element = Element()
            .setTitle(String.format(getString(R.string.about_copyright)))
            .setIconDrawable(R.drawable.ic_baseline_copyright_24)
            .setOnClickListener { Dialogs.showLicense(this) }

        val licenses: Element = Element()
            .setTitle(getString(R.string.about_licenses))
            .setIconDrawable(R.drawable.ic_baseline_attribution_24)
            .setOnClickListener { startActivity(Intent(this, OssLicensesMenuActivity::class.java)) }

        val tankerkoenigApi: Element = Element()
            .setTitle(getString(R.string.about_tankerkoenig))
            .setIconDrawable(R.drawable.ic_baseline_attribution_24)
            .setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://creativecommons.tankerkoenig.de/")
                startActivity(i)
            }

        val aboutPage: View = AboutPage(this)
            .enableDarkMode(nightModeEnabled)
            .addItem(versionItem)
            .addEmail("steffen.klee@gmail.com")
            .addTwitter("_kleest")
            .addPlayStore("st.klee.tankassist")
            .addGitHub("kleest/tankassist")
            .addItem(copyright)
            .addItem(licenses)
            .addItem(tankerkoenigApi)
            .create()

        v.addView(aboutPage)
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
}