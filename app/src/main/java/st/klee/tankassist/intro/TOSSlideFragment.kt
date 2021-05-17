package st.klee.tankassist.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.heinrichreimersoftware.materialintro.app.SlideFragment
import st.klee.tankassist.R
import st.klee.tankassist.misc.Dialogs

class TOSSlideFragment : SlideFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.intro_slide_tos, container, false)
        v.findViewById<MaterialButton>(R.id.btn_license)!!.setOnClickListener {
            Dialogs.showLicense(requireContext())
        }
        v.findViewById<MaterialButton>(R.id.btn_privacy)!!.setOnClickListener {
            Dialogs.showPrivacy(requireContext())
        }
        return v.rootView
    }

    override fun canGoForward(): Boolean {
        // when license and privacy policy not accepted, do not allow to continue
        return requireView().findViewById<MaterialCheckBox>(R.id.check_license)!!.isChecked &&
                requireView().findViewById<MaterialCheckBox>(R.id.check_privacy)!!.isChecked
    }

    companion object {
        fun newInstance(): TOSSlideFragment {
            return TOSSlideFragment()
        }
    }
}