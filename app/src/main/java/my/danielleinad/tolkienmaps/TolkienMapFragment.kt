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

open class TolkienMapFragment(val mapId: String) : Fragment() {
    private lateinit var binding: FragmentMiddleEarthMapBinding
    private var areNonMainLayersShown: Boolean = false
    private val nonMainLayers: MutableList<NonMainLayer> = mutableListOf()
    private lateinit var mainBitmap: ImageScaleView.BitMapLayer

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
        val mapsDescription = MapsDescription.getMapsDescription(resources)
        val mainMap = mapsDescription.maps[mapId]?: throw Exception("Unknown map: $mapId")
        val imageView = binding.imageView

        mainBitmap = imageView.BitMapLayer(mainMap.bitmap, mainMap.preview)

        val containerPaint = Paint()
        containerPaint.color = Color.argb(20, 0, 50, 250)
        containerPaint.strokeWidth = 5F

        val borderPaint = Paint()
        borderPaint.color = Color.DKGRAY
        borderPaint.strokeWidth = 5F

        val actions = mainMap.actions
        val initialMatrix = Matrix()
        appendMapLayersRecursively(mainMap, initialMatrix, imageView, actions, containerPaint, borderPaint)
    }

    private fun appendMapLayersRecursively(
        mapDescription: MapsDescription.Companion.Map,
        initialMatrix: Matrix,
        imageView: ImageScaleView,
        actions: MutableMap<MapsDescription.Companion.Map, Int>,
        containerPaint: Paint,
        borderPaint: Paint,
    ) {
        for (position in mapDescription.positions) {
            val matrix = Matrix()
            matrix.postRotate(position.rotate)
            matrix.postScale(position.scale, position.scale)
            matrix.postTranslate(position.translateX, position.translateY)
            matrix.postConcat(initialMatrix)

            val otherMap = position.map

            val mapLayer = imageView.LayerDescription(
                imageView.BitMapLayer(
                    otherMap.bitmap,
                    otherMap.preview
                ), matrix
            )
            val action = actions[otherMap]
            if (action == null) {
                MessageShower.warn("Action not found for map ${otherMap.id}")
            } else {
                mapLayer.onDoubleTapListener = {
                    findNavController().navigate(action)
                    true
                }
            }

            val container = imageView.RectangleLayer(
                0F,
                0F,
                otherMap.bitmap.width.toFloat(),
                otherMap.bitmap.height.toFloat(),
                true,
                containerPaint
            )
            val containerLayer = imageView.LayerDescription(container, Matrix(matrix))

            val borders = imageView.RectangleLayer(
                0F,
                0F,
                otherMap.bitmap.width.toFloat(),
                otherMap.bitmap.height.toFloat(),
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

            appendMapLayersRecursively(otherMap, matrix, imageView, actions, containerPaint, borderPaint)
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