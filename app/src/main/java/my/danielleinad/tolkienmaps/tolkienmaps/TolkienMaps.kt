package my.danielleinad.tolkienmaps.tolkienmaps

typealias TolkienMapId = String

interface TolkienMaps {
    fun get(mapId: TolkienMapId): TolkienMap?

    // All properties in this interface are specified relative to the *virtual map*
    interface TolkienMap {
        val id: TolkienMapId
        val scale: Float
        val translateX: Float
        val translateY: Float
        val rotate: Float
    }
}