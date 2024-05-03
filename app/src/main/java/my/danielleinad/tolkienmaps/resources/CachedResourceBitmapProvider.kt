package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object CachedResourceBitmapProvider {
    private val cache: MutableMap<Pair<Resources, Int>, Bitmap> = mutableMapOf()
    fun get(resources: Resources, resourceId: Int): Bitmap {
        val cachedBitmap = cache[Pair(resources, resourceId)]
        if (cachedBitmap != null) {
            return cachedBitmap
        }
        val res = provideBitmap(resources, resourceId)
        cache[Pair(resources, resourceId)] = res
        return res
    }
}

private fun provideBitmap(resources: Resources, resourceId: Int): Bitmap {
    return BitmapFactory.decodeResource(resources, resourceId)
}