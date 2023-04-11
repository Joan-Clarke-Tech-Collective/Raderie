package org.clarkecollective.raderie

import android.content.Context
import android.widget.Toast
import com.orhanobut.logger.Logger
fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Throwable.log() {
    Logger.d(this.localizedMessage ?: "No Localized Message Found")
}
