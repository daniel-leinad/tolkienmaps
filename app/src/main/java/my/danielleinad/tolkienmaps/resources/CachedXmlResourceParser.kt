package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIStructure

//TODO cache should be immutable
object CachedXmlResourceParser {
    private val tolkienMaps: MutableMap<Resources, TolkienMaps> = mutableMapOf()
    private val tolkienMapsUIStructure: MutableMap<Resources, TolkienMapsUIStructure> = mutableMapOf()

    fun getTolkienMaps(resources: Resources): TolkienMaps {
        val cachedTolkienMaps = tolkienMaps[resources]
        if (cachedTolkienMaps != null) {
            return cachedTolkienMaps
        }

        val res = parseTolkienMaps(resources)

        tolkienMaps[resources] = res

        return res
    }

    fun getTolkienMapsUIStructure(resources: Resources): TolkienMapsUIStructure {
        val cachedTolkienMapsUIStructure = tolkienMapsUIStructure[resources]
        if (cachedTolkienMapsUIStructure != null) {
            return cachedTolkienMapsUIStructure
        }

        val res = parseTolkienMapsUIStructure(resources)

        tolkienMapsUIStructure[resources] = res

        return res
    }
}