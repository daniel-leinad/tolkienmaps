package my.danielleinad.tolkienmaps

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.danielleinad.tolkienmaps.databinding.FragmentMiddleEarthMapBinding
import my.danielleinad.tolkienmaps.resources.CachedXmlResourceParser
import my.danielleinad.tolkienmaps.tolkienmaps.TolkienMaps
import my.danielleinad.tolkienmaps.ui.TolkienMapsUIStructure

open class TolkienMapFragment(private val mapId: String) : Fragment() {
    private lateinit var binding: FragmentMiddleEarthMapBinding
    private var areNonMainLayersShown: Boolean = false
    private val nonMainLayers: MutableList<NonMainLayer> = mutableListOf()
    private lateinit var mainBitmap: ImageScaleView.BitMapLayer
    private val containerPaint: Paint = Paint()
    private val borderPaint = Paint()
    init {
        containerPaint.color = Color.argb(20, 0, 50, 250)
        containerPaint.strokeWidth = 5F

        borderPaint.color = Color.DKGRAY
        borderPaint.strokeWidth = 5F
    }

    private class NonMainLayer(val map: ImageScaleView.LayerDescription, val container: ImageScaleView.LayerDescription, val borders: ImageScaleView.LayerDescription) {
        var isMapShown: Boolean = false
            set(value) {
                field = value
                map.activated = value
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentMiddleEarthMapBinding.inflate(layoutInflater)

        constructNonMainLayers()

        binding.imageView.layers.add(binding.imageView.LayerDescription(mainBitmap, Matrix()))
        for (layer in nonMainLayers) {
            binding.imageView.layers.add(layer.map)
            binding.imageView.layers.add(layer.container)
            binding.imageView.layers.add(layer.borders)
        }

        binding.showHideLayers.setOnClickListener {
            areNonMainLayersShown = !areNonMainLayersShown
            hideShowLayers()
        }

        hideShowLayers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    private fun constructNonMainLayers() {
        val tolkienMaps = CachedXmlResourceParser.getTolkienMaps(resources)
        val tolkienMapsUIStructure = CachedXmlResourceParser.getTolkienMapsUIStructure(resources)
        val mainMap = tolkienMaps.maps[mapId]?: throw Exception("Unknown map: $mapId")

        val mainMapRepresentation = tolkienMapsUIStructure.representations[mapId]
            ?: throw Exception("Representation not found for map $mapId")

        mainBitmap = binding.imageView.BitMapLayer(mainMapRepresentation.bitmap, mainMapRepresentation.preview)

        val initialMatrix = Matrix()
        appendMapLayersRecursively(mainMap, initialMatrix, tolkienMapsUIStructure)
    }

    private fun appendMapLayersRecursively(
        tolkienMap: TolkienMaps.TolkienMap,
        initialMatrix: Matrix,
        tolkienMapsUIStructure: TolkienMapsUIStructure,
    ) {
        val imageView = binding.imageView
        for (position in tolkienMap.positions) {
            val matrix = Matrix()
            matrix.postRotate(position.rotate)
            matrix.postScale(position.scale, position.scale)
            matrix.postTranslate(position.translateX, position.translateY)
            matrix.postConcat(initialMatrix)

            val otherMapId = position.map.id
            val otherMapRepresentation = tolkienMapsUIStructure.representations[otherMapId]?: throw Exception("Representation not found for")

            val mapLayer = imageView.LayerDescription(
                imageView.BitMapLayer(
                    otherMapRepresentation.bitmap,
                    otherMapRepresentation.preview
                ), matrix
            )
            val action = tolkienMapsUIStructure.actions[Pair(mapId, otherMapId)]
            if (action == null) {
                MessageShower.warn("Action not found for map $otherMapId")
            } else {
                mapLayer.onDoubleTapListener = {
                    findNavController().navigate(action)
                    true
                }
            }

            val container = imageView.RectangleLayer(
                0F,
                0F,
                otherMapRepresentation.bitmap.width.toFloat(),
                otherMapRepresentation.bitmap.height.toFloat(),
                true,
                containerPaint
            )
            val containerLayer = imageView.LayerDescription(container, Matrix(matrix))

            val borders = imageView.RectangleLayer(
                0F,
                0F,
                otherMapRepresentation.bitmap.width.toFloat(),
                otherMapRepresentation.bitmap.height.toFloat(),
                false,
                borderPaint
            )

            val borderLayer = imageView.LayerDescription(borders, Matrix(matrix))

            val layer = NonMainLayer(mapLayer, containerLayer, borderLayer)

            containerLayer.onSingleTapConfirmedListener = {
                layer.isMapShown = !layer.isMapShown
                true
            }

            nonMainLayers.add(layer)

            appendMapLayersRecursively(position.map, matrix, tolkienMapsUIStructure)
        }
    }

    private fun hideShowLayers() {
        for (layerDescription in nonMainLayers) {
            layerDescription.map.activated = areNonMainLayersShown && layerDescription.isMapShown
            layerDescription.container.activated = areNonMainLayersShown
            layerDescription.borders.activated = areNonMainLayersShown
        }

        binding.imageView.invalidate()
    }
}