package chat.rocket.android

import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("LogNotTimber")
inline fun log(message: () -> String) {
    if (BuildConfig.DEBUG)
        Log.e("Test1234", message())
}