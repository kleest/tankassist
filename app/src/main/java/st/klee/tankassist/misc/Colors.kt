package st.klee.tankassist.misc

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources

@ColorInt
fun Context.getThemeColor(@AttrRes attribute: Int) =
    TypedValue().let { theme.resolveAttribute(attribute, it, true); it.data }

fun Context.getThemeColorState(@AttrRes attribute: Int): ColorStateList = TypedValue().let {
    theme.resolveAttribute(
        attribute,
        it,
        true
    ); AppCompatResources.getColorStateList(this, it.resourceId)
}
