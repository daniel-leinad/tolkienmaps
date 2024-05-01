package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.BitmapFactory
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMapId
import my.danielleinad.tolkienmaps.R
import my.danielleinad.tolkienmaps.ui.TolkienMapUIRepresentation
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIStructure
import org.xmlpull.v1.XmlPullParserException

fun parseTolkienMapsUIStructure(resources: Resources): TolkienMapsUIStructure {
    val xmlParser = resources.getXml(R.xml.tolkien_maps_ui_structure)
    val tolkienMapsUIStructureXml = TolkienMapsUIStructureXml(xmlParser)

    val actions: MutableMap<Pair<TolkienMapId, TolkienMapId>, Int> = mutableMapOf()
    val representations: MutableMap<TolkienMapId, TolkienMapUIRepresentation> = mutableMapOf()

    for (xmlMap in tolkienMapsUIStructureXml.maps) {
        val mapId = xmlMap.id

        val bitmap = BitmapFactory.decodeResource(resources, xmlMap.bitmap)
        val previewBitmap = BitmapFactory.decodeResource(resources, xmlMap.preview)
        representations[mapId] = TolkienMapUIRepresentation(bitmap, previewBitmap)

        for (action in xmlMap.actions) {
            actions[Pair(mapId, action.destination)] = action.value
        }
    }

    return TolkienMapsUIStructure(representations, actions)
}

private class TolkienMapsUIStructureXml(xmlParser: XmlResourceParser) {
    class Map(val id: String, val bitmap: Int, val preview: Int) {
        val actions: MutableList<Action> = mutableListOf()

        companion object {
            fun parse(xmlParser: XmlResourceParser): Map {
                var id: String? = null
                var bitmap: Int? = null
                var preview: Int? = null
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                        "bitmap" -> { bitmap = getAttributeResourceValueOrNull(xmlParser, i)
                        }
                        "preview" -> { preview = getAttributeResourceValueOrNull(xmlParser, i) }
                    }
                }
                if (id == null || bitmap == null || preview == null) {
                    throw XmlPullParserException("Error while parsing <map>: required attributes not found")
                }

                val map = Map(id, bitmap, preview)

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