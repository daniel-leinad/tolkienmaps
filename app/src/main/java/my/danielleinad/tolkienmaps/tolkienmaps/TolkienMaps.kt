package my.danielleinad.tolkienmaps.tolkienmaps

typealias TolkienMapId = String

class TolkienMaps constructor(val maps: Map<TolkienMapId, TolkienMap>) {
    class TolkienMap(
        val id: TolkienMapId,
    ) {
        val positions: MutableList<Position> = mutableListOf()
    }

    class Position(
        val map: TolkienMap,
        val scale: Float,
        val translateX: Float,
        val translateY: Float,
        val rotate: Float,
    )
}