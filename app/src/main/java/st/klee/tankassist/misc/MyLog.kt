package st.klee.tankassist.misc

import android.util.Log
import st.klee.tankassist.BuildConfig

/**
 * Logging classes that suppresses VERBOSE level logging in non-DEBUG builds.
 */
object MyLog {
    fun d(tag: String?, msg: String?, throwable: Throwable?): Int {
        return Log.d(tag, msg, throwable)
    }
    fun d(tag: String?, msg: String): Int {
        return Log.d(tag, msg)
    }
    fun e(tag: String?, msg: String?, throwable: Throwable?): Int {
        return Log.e(tag, msg, throwable)
    }
    fun e(tag: String?, msg: String): Int {
        return Log.e(tag, msg)
    }
    fun i(tag: String?, msg: String?, throwable: Throwable?): Int {
        return Log.i(tag, msg, throwable)
    }
    fun i(tag: String?, msg: String): Int {
        return Log.i(tag, msg)
    }
    fun v(tag: String?, msg: String?, throwable: Throwable?): Int {
        if (BuildConfig.DEBUG)
            return Log.v(tag, msg, throwable)
        return 0
    }
    fun v(tag: String?, msg: String): Int {
        if (BuildConfig.DEBUG)
            return Log.v(tag, msg)
        return 0
    }
    fun w(tag: String?, msg: String?, throwable: Throwable?): Int {
        return Log.w(tag, msg, throwable)
    }
    fun w(tag: String?, msg: String): Int {
        return Log.w(tag, msg)
    }
    fun wtf(tag: String?, msg: String?, throwable: Throwable?): Int {
        return Log.wtf(tag, msg, throwable)
    }
    fun wtf(tag: String?, msg: String): Int {
        return Log.wtf(tag, msg)
    }
}