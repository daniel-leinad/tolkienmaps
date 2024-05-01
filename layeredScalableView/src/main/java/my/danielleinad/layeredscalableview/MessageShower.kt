package my.danielleinad.layeredscalableview

import android.util.Log

//TODO delete this
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