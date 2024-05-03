package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMapId
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIDetails

object CachedXmlResourceParser {
    private val tolkienMaps: MutableMap<Resources, TolkienMaps> = mutableMapOf()
    private val tolkienMapsUIDetails: MutableMap<Resources, TolkienMapsUIDetails> = mutableMapOf()
    private val compasses: MutableMap<Resources, Map<TolkienMapId, Int>> = mutableMapOf()

    fun getTolkienMaps(resources: Resources): TolkienMaps {
        val cachedTolkienMaps = tolkienMaps[resources]
        if (cachedTolkienMaps != null) {
            return cachedTolkienMaps
        }

        val res = parseTolkienMaps(resources)

        tolkienMaps[resources] = res

        return res
    }

    fun getTolkienMapsUIStructure(resources: Resources): TolkienMapsUIDetails {
        val cachedTolkienMapsUIStructure = tolkienMapsUIDetails[resources]
        if (cachedTolkienMapsUIStructure != null) {
            return cachedTolkienMapsUIStructure
        }

        val res = parseTolkienMapsUIDetails(resources)

        tolkienMapsUIDetails[resources] = res

        return res
    }

    fun getCompasses(resources: Resources): Map<TolkienMapId, Int> {
        val cachedCompasses = compasses[resources]
        if (cachedCompasses != null) {
            return cachedCompasses
        }

        val res = parseCompasses(resources)

        compasses[resources] = res

        return res
    }
}