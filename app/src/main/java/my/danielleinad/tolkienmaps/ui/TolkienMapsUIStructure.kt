package my.danielleinad.tolkienmaps.ui

import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMapId

interface TolkienMapsUIStructure {
    fun getNavigations(mapId: TolkienMapId): Map<TolkienMapId, Int>?
    fun getRepresentation(mapId: TolkienMapId): TolkienMapUIRepresentation?
}