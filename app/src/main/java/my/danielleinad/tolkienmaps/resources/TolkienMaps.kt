package my.danielleinad.tolkienmaps.resources

import android.content.res.Resources
import android.content.res.XmlResourceParser
import my.danielleinad.tolkienmaps.R
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMapId
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import org.xmlpull.v1.XmlPullParserException

fun parseTolkienMaps(resources: Resources): TolkienMaps {
    val xmlParser = resources.getXml(R.xml.tolkien_maps)
    val tolkienMapsXml = TolkienMapsXml(xmlParser)

    val maps: MutableMap<String, TolkienMapsFromResources.TolkienMap> = mutableMapOf()
    for (xmlMap in tolkienMapsXml.maps) {
        val map = TolkienMapsFromResources.TolkienMap(
            id = xmlMap.id,
            translateX = xmlMap.translateX,
            translateY = xmlMap.translateY,
            scale = xmlMap.scale,
            rotate = xmlMap.rotate,
        )
        maps[xmlMap.id] = map
    }

    return TolkienMapsFromResources(maps)
}

private class TolkienMapsFromResources(val tolkienMaps: Map<TolkienMapId, TolkienMap>) : TolkienMaps {
    override fun get(mapId: TolkienMapId): TolkienMaps.TolkienMap {
        return tolkienMaps[mapId] as TolkienMaps.TolkienMap
    }
    class TolkienMap(
        override val id: TolkienMapId,
        override val translateX: Float,
        override val translateY: Float,
        override val scale: Float,
        override val rotate: Float
    ) : TolkienMaps.TolkienMap
}

private class TolkienMapsXml(xmlParser: XmlResourceParser) {
    class Map(
        val id: String,
        val translateX: Float,
        val translateY: Float,
        val scale: Float,
        val rotate: Float,
    ) {

        companion object {
            fun parse(xmlParser: XmlResourceParser): Map {
                var id: String? = null
                var translateX: Float? = null
                var translateY: Float? = null
                var scale: Float? = null
                var rotate = 0F // default value
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                        "translate_x" -> { translateX = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                        "translate_y" -> { translateY = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                        "scale" -> { scale = xmlParser.getAttributeFloatValue(i, 0F)}
                        "rotate" -> { rotate = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                    }
                }
                if (id == null || scale == null || translateX == null || translateY == null) {
                    throw XmlPullParserException("Error while parsing <map>: required attributes not found")
                }

                val map = Map(id, translateX, translateY, scale, rotate)

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