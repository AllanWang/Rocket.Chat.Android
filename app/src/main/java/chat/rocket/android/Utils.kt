package chat.rocket.android

import android.util.Log

inline fun log(message: () -> String) {
    Log.e("Test1234", message())
}