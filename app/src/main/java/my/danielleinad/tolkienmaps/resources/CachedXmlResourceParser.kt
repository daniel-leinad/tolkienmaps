package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIDetails

object CachedXmlResourceParser {
    private val tolkienMaps: MutableMap<Resources, TolkienMaps> = mutableMapOf()
    private val tolkienMapsUIDetails: MutableMap<Resources, TolkienMapsUIDetails> = mutableMapOf()

    fun getTolkienMaps(resources: Resources): TolkienMaps {
        val cachedTolkienMaps = tolkienMaps[resources]
        if (cachedTolkienMaps != null) {
            return cachedTolkienMaps
        }

        val res = parseTolkienMaps(resources)

        tolkienMaps[resources] = res

        return res
    }

    fun getTolkienMapsUIDetails(resources: Resources): TolkienMapsUIDetails {
        val cachedTolkienMapsUIDetails = tolkienMapsUIDetails[resources]
        if (cachedTolkienMapsUIDetails != null) {
            return cachedTolkienMapsUIDetails
        }

        val res = parseTolkienMapsUIDetails(resources)

        tolkienMapsUIDetails[resources] = res

        return res
    }
}