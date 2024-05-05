package my.danielleinad.tolkienmaps.tolkienmaps

typealias TolkienMapId = String

// This interface represents the *virtual map* relative to which all maps are positioned
interface TolkienMaps {
    fun get(mapId: TolkienMapId): TolkienMap?

    interface TolkienMap {
        val id: TolkienMapId
        val translateX: Float
        val translateY: Float
        val targetHeight: Float
        val rotate: Float
    }
}