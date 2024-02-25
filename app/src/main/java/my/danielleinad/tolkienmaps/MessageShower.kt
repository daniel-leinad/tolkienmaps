package my.danielleinad.tolkienmaps

import android.util.Log

class MessageShower {
    companion object {
        fun show(message: String) {
            Log.d("DAN-MESSAGE-SHOWER", message)
        }

        fun warn(message: String) {
            Log.w("DAN-MESSAGE-SHOWER-WARNING", message)
        }
    }
}