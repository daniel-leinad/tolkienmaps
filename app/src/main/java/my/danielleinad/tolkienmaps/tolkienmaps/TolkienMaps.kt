package my.danielleinad.tolkienmaps.tolkienmaps

typealias TolkienMapId = String

interface TolkienMaps {
    fun get(mapId: TolkienMapId): TolkienMap?

    // All properties in this interface are specified relative to the *virtual map*
    interface TolkienMap {
        val id: TolkienMapId
        val translateX: Float
        val translateY: Float
        val targetHeight: Float
        val rotate: Float
    }
}