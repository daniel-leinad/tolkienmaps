package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import android.content.res.XmlResourceParser
import my.danielleinad.tolkienmaps.R
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMapId
import org.xmlpull.v1.XmlPullParserException

//TODO I don't like this whole compasses thing
fun parseCompasses(resources: Resources): Map<TolkienMapId, Int> {
    val xmlParser = resources.getXml(R.xml.compasses)
    val tolkienMapsXml = CompassesXml(xmlParser)

    val res: MutableMap<TolkienMapId, Int> = mutableMapOf()

    for (xmlMap in tolkienMapsXml.maps) {
        res[xmlMap.id] = xmlMap.compass
    }
    return res
}

private class CompassesXml(xmlParser: XmlResourceParser) {
    class Map(val id: String, val compass: Int) {

        companion object {
            fun parse(xmlParser: XmlResourceParser): Map {
                var id: String? = null
                var compass: Int? = null
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                        "compass" -> { compass = getAttributeResourceValueOrNull(xmlParser, i)
                        }
                    }
                }
                if (id == null || compass == null) {
                    throw XmlPullParserException("Error while parsing <map>: required attributes not found")
                }

                val map = Map(id, compass)

                while (true) {
                    xmlParser.next()
                    if (xmlParser.eventType == XmlResourceParser.END_TAG && xmlParser.name == "map") break
                }

                return map
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