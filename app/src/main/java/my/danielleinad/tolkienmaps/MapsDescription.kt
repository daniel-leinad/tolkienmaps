package my.danielleinad.tolkienmaps

import android.graphics.Bitmap
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import androidx.navigation.fragment.findNavController
import org.xmlpull.v1.XmlPullParserException
// TODO the reason to rename MapsDescription.Map
import kotlin.collections.Map as ImmutableMap


// TODO make all of it immutable!!
class MapsDescription private constructor(val maps: ImmutableMap<String, Map>) {
    companion object {
        private val cachedMapsDescriptions: MutableMap<Resources, MapsDescription> = mutableMapOf()

        // TODO Map is a bad and confusing name because it has other meaning... what do?
        class Map(val id: String, val bitmap: Bitmap, val preview: Bitmap) {
            class Position(
                val map: Map,
                val scale: Float,
                val translateX: Float,
                val translateY: Float
            )

            val positions: MutableList<Position> = mutableListOf()
            val actions: MutableMap<Map, Int> = mutableMapOf()
        }

        fun getMapsDescription(resources: Resources): MapsDescription {
            val cachedMapsDescription = cachedMapsDescriptions[resources]
            if (cachedMapsDescription != null) {
                return cachedMapsDescription
            }

            val mapsDescription = createMapsDescription(resources)

            cachedMapsDescriptions[resources] = mapsDescription

            return mapsDescription
            }

        private fun createMapsDescription(resources: Resources): MapsDescription {
            val xmlParser = resources.getXml(R.xml.maps_description)

            // Read XML resource to memory
            val xmlStructure = MapsDescriptionXmlStructure(xmlParser)

            val maps: MutableMap<String, Map> = mutableMapOf()
            for (xmlMap in xmlStructure.maps) {
                val map = Map(
                    xmlMap.id,
                    BitmapFactory.decodeResource(resources, xmlMap.bitmap),
                    BitmapFactory.decodeResource(resources, xmlMap.preview))
                maps[xmlMap.id] = map
            }

            for (xmlMap in xmlStructure.maps) {
                val map = maps[xmlMap.id]!! // Can't be null: guaranteed from previous loop
                for (action in xmlMap.actions) {
                    val destinationMap = maps[action.destination]
                        // TODO maybe change exception
                        ?: throw Exception("Error while parsing @xml/maps_description: unknown map id: ${action.destination}")
                    map.actions[destinationMap] = action.value
                }
                for (position in xmlMap.positions) {
                    val otherMap = maps[position.id]
                        // TODO maybe change exception
                        ?: throw Exception("Error while parsing @xml/maps_description: unknown map id: ${position.id}")
                    map.positions.add(Map.Position(otherMap, position.scale, position.translateX, position.translateY))
                }
            }

            return MapsDescription(maps.toMap())
        }
    }
}


private class MapsDescriptionXmlStructure(xmlParser: XmlResourceParser) {
    class Map(val id: String, val bitmap: Int, val preview: Int) {
        val positions: MutableList<Position> = mutableListOf()
        val actions: MutableList<Action> = mutableListOf()

        companion object {
            fun parse(xmlParser: XmlResourceParser): Map {
                var id: String? = null
                var bitmap: Int? = null
                var preview: Int? = null
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                        // TODO is -1 the best default value? what are possible resource values?
                        "bitmap" -> { bitmap = xmlParser.getAttributeResourceValue(i, -1)}
                        "preview" -> { preview = xmlParser.getAttributeResourceValue(i, -1) }
                    }
                }
                if (id == null || bitmap == null || preview == null) {
                    // TODO is the right exception?
                    throw XmlPullParserException("Error while parsing <map>: required attributes not found")
                }

                val map = Map(id, bitmap, preview)

                while (true) {
                    xmlParser.next()
                    if (xmlParser.eventType == XmlResourceParser.END_TAG && xmlParser.name == "map") break
                    if (xmlParser.eventType == XmlResourceParser.START_TAG) {
                        when (xmlParser.name) {
                            "position" -> { map.positions.add(Position.parse(xmlParser)) }
                            "action" -> { map.actions.add(Action.parse(xmlParser)) }
                        }
                    }
                }

                return map
            }
        }
    }
    class Position(val id: String, val scale: Float, val translateX: Float, val translateY: Float) {
        companion object {
            fun parse(xmlParser: XmlResourceParser): Position {
                var id: String? = null
                var scale: Float? = null
                var translateX: Float? = null
                var translateY: Float? = null
                for (i in 0 until xmlParser.attributeCount) {
                    when (xmlParser.getAttributeName(i)) {
                        "id" -> { id = xmlParser.getAttributeValue(i) }
                        "scale" -> { scale = xmlParser.getAttributeFloatValue(i, 0F)}
                        "translate_x" -> { translateX = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                        "translate_y" -> { translateY = xmlParser.getAttributeIntValue(i, 0).toFloat() }
                    }
                }
                if (id == null || scale == null || translateX == null || translateY == null) {
                    // TODO is the right exception?
                    throw XmlPullParserException("Error while parsing <position>: required attributes not found")
                }

                while (true) {
                    if (xmlParser.eventType == XmlResourceParser.END_TAG && xmlParser.name == "position") {
                        break
                    }
                    xmlParser.next()
                }

                return Position(id, scale, translateX, translateY)
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
                        // TODO is -1 the best default value? what are possible resource values?
                        "value" -> { value = xmlParser.getAttributeResourceValue(i, -1)}
                    }
                }
                if (destination == null || value == null) {
                    // TODO is the right exception?
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