package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import android.content.res.XmlResourceParser
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMapId
import my.danielleinad.tolkienmaps.R
import my.danielleinad.tolkienmaps.ui.TolkienMapUIRepresentation
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIDetails
import org.xmlpull.v1.XmlPullParserException

fun parseTolkienMapsUIDetails(resources: Resources): TolkienMapsUIDetails {
    val xmlParser = resources.getXml(R.xml.tolkien_maps_ui_details)
    val tolkienMapsUIStructureXml = TolkienMapsUIStructureXml(xmlParser)

    val navigations: MutableMap<TolkienMapId, MutableMap<TolkienMapId, Int>> = mutableMapOf()
    val representations: MutableMap<TolkienMapId, TolkienMapUIRepresentation> = mutableMapOf()
    val compasses: MutableMap<TolkienMapId, Int> = mutableMapOf()

    for (xmlMap in tolkienMapsUIStructureXml.maps) {
        val mapId = xmlMap.id

        representations[mapId] = TolkienMapUIRepresentation(xmlMap.bitmap, xmlMap.lowerRes, xmlMap.lowestRes)
        compasses[mapId] = xmlMap.compass

        val currentMapNavigations: MutableMap<TolkienMapId, Int> = mutableMapOf()
        navigations[mapId] = currentMapNavigations

        for (action in xmlMap.actions) {
            currentMapNavigations[action.destination] = action.value
        }
    }

    return TolkienMapsUIDetailsFromResources(representations, navigations, compasses)
}

private class TolkienMapsUIDetailsFromResources(
    private val representations: Map<TolkienMapId, TolkienMapUIRepresentation>,
    private val navigations: Map<TolkienMapId, Map<TolkienMapId, Int>>,
    private val compasses: Map<TolkienMapId, Int>
) : TolkienMapsUIDetails {
    override fun getNavigations(mapId: TolkienMapId): Map<TolkienMapId, Int>? {
        return navigations[mapId]
    }

    override fun getRepresentation(mapId: TolkienMapId): TolkienMapUIRepresentation? {
        return representations[mapId]
    }

    override fun getCompass(mapId: TolkienMapId): Int? {
        return compasses[mapId]
    }
}

private class TolkienMapsUIStructureXml(xmlParser: XmlResourceParser) {
    class Map(
        val id: String,
        val bitmap: Int,
        val lowerRes: Int,
        val lowestRes: Int,
        val compass: Int
    ) {
        val actions: MutableList<Action> = mutableListOf()

        companion object {
            fun parse(xmlParser: XmlResourceParser): Map {
                var id: String? = null
                var bitmap: Int? = null
                var lowerRes: Int? = null
                var lowestRes: Int? = null
                var compass: Int? = null
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                        "bitmap" -> { bitmap = getAttributeResourceValueOrNull(xmlParser, i) }
                        "lower_res" -> { lowerRes = getAttributeResourceValueOrNull(xmlParser, i) }
                        "lowest_res" -> { lowestRes = getAttributeResourceValueOrNull(xmlParser, i) }
                        "compass" -> { compass = getAttributeResourceValueOrNull(xmlParser, i) }
                    }
                }
                if (id == null || bitmap == null || lowerRes == null || lowestRes == null || compass == null) {
                    throw XmlPullParserException("Error while parsing <map>: required attributes not found")
                }

                val map = Map(id, bitmap, lowerRes, lowestRes, compass)

                while (true) {
                    xmlParser.next()
                    if (xmlParser.eventType == XmlResourceParser.END_TAG && xmlParser.name == "map") break
                    if (xmlParser.eventType == XmlResourceParser.START_TAG) {
                        when (xmlParser.name) {
                            "action" -> { map.actions.add(Action.parse(xmlParser)) }
                        }
                    }
                }

                return map
            }
        }
    }
    class Action(val destination: String, val value: Int) {
        companion object {
            fun parse(xmlParser: XmlResourceParser): Action {
                var destination: String? = null
                var value: Int? = null
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "destination" -> { destination = xmlParser.getAttributeValue(i) }
                        "value" -> { value = getAttributeResourceValueOrNull(xmlParser, i)
                        }
                    }
                }
                if (destination == null || value == null) {
                    throw XmlPullParserException("Error while parsing <action>: required attributes not found")
                }

                while (true) {
                    if (xmlParser.eventType == XmlResourceParser.END_TAG && xmlParser.name == "action") {
                        break
                    }
                    xmlParser.next()
                }

                return Action(destination, value)
            }
        }
    }

    val maps: MutableList<Map> = mutableListOf()

    init {
        while (xmlParser.eventType != XmlResourceParser.END_DOCUMENT) {
            when (xmlParser.eventType) {
                XmlResourceParser.START_TAG -> {
                    when (xmlParser.name) {
                        "map" -> { maps.add(Map.parse(xmlParser)) }
                    }
                }
            }
            xmlParser.next()
        }
    }
}