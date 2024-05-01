package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import android.content.res.XmlResourceParser
import my.danielleinad.tolkienmaps.R
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import org.xmlpull.v1.XmlPullParserException

fun parseTolkienMaps(resources: Resources): TolkienMaps {
    val xmlParser = resources.getXml(R.xml.tolkien_maps)
    val tolkienMapsXml = TolkienMapsXml(xmlParser)

    val maps: MutableMap<String, TolkienMaps.TolkienMap> = mutableMapOf()
    for (xmlMap in tolkienMapsXml.maps) {
        val map = TolkienMaps.TolkienMap(xmlMap.id)
        maps[xmlMap.id] = map
    }

    for (xmlMap in tolkienMapsXml.maps) {
        val map = maps[xmlMap.id]!! // Can't be null: guaranteed from previous loop
        for (position in xmlMap.positions) {
            val otherMap = maps[position.id]
                ?: throw IncorrectResourceException("Error while parsing @xml/maps_description: unknown map id: ${position.id}")
            map.positions.add(
                TolkienMaps.Position(
                    otherMap,
                    position.scale,
                    position.translateX,
                    position.translateY,
                    position.rotate
                )
            )
        }
    }

    return TolkienMaps(maps)
}

private class TolkienMapsXml(xmlParser: XmlResourceParser) {
    class Map(val id: String) {
        val positions: MutableList<Position> = mutableListOf()

        companion object {
            fun parse(xmlParser: XmlResourceParser): Map {
                var id: String? = null
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                    }
                }
                if (id == null) {
                    throw XmlPullParserException("Error while parsing <map>: required attributes not found")
                }

                val map = Map(id)

                while (true) {
                    xmlParser.next()
                    if (xmlParser.eventType == XmlResourceParser.END_TAG && xmlParser.name == "map") break
                    if (xmlParser.eventType == XmlResourceParser.START_TAG) {
                        when (xmlParser.name) {
                            "position" -> { map.positions.add(Position.parse(xmlParser)) }
                        }
                    }
                }

                return map
            }
        }
    }
    class Position(val id: String, val scale: Float, val translateX: Float, val translateY: Float, val rotate: Float) {
        companion object {
            fun parse(xmlParser: XmlResourceParser): Position {
                var id: String? = null
                var scale: Float? = null
                var translateX: Float? = null
                var translateY: Float? = null
                var rotate = 0F // default value
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                        "scale" -> { scale = xmlParser.getAttributeFloatValue(i, 0F)}
                        "translate_x" -> { translateX = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                        "translate_y" -> { translateY = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                        "rotate" -> { rotate = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                    }
                }
                if (id == null || scale == null || translateX == null || translateY == null) {
                    throw XmlPullParserException("Error while parsing <position>: required attributes not found")
                }

                while (true) {
                    if (xmlParser.eventType == XmlResourceParser.END_TAG && xmlParser.name == "position") {
                        break
                    }
                    xmlParser.next()
                }

                return Position(id, scale, translateX, translateY, rotate)
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