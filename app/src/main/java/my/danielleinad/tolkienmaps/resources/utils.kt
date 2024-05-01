package my.danielleinad.tolkienmaps.resources

import android.content.res.XmlResourceParser
import my.danielleinad.tolkienmaps.R

class IncorrectResourceException(message: String) : Exception(message)

fun getAttributeResourceValueOrNull(xmlParser: XmlResourceParser, index: Int): Int? {
    val value = xmlParser.getAttributeResourceValue(index, R.drawable.empty)
    return if (value == R.drawable.empty) {
        null
    } else {
        value
    }
}