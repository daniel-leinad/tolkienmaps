package my.danielleinad.tolkienmaps.ui

import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMapId

class TolkienMapsUIStructure(
    val representations: Map<TolkienMapId, TolkienMapUIRepresentation>,
    val actions: Map<Pair<TolkienMapId, TolkienMapId>, Int>
)