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

    private class NonMainLayer(val map: ImageScaleView.LayerDescription, val borders: ImageScaleView.LayerDescription) {
        var isMapShown: Boolean = false
            set(value) {
                field = value
                map.activated = value
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout for this fragment
        // TODO pick a style and use one
        binding = FragmentMiddleEarthMapBinding.inflate(layoutInflater)

        constructNonMainLayers()

        binding.imageView.layers.add(binding.imageView.LayerDescription(mainBitmap, Matrix()))
        for (layer in nonMainLayers) {
            binding.imageView.layers.add(layer.map)
            binding.imageView.layers.add(layer.borders)
        }

        fun hideShowLayers() {
            for (layerDescription in nonMainLayers) {
                layerDescription.map.activated = areNonMainLayersShown && layerDescription.isMapShown
                layerDescription.borders.activated = areNonMainLayersShown
            }

            binding.imageView.invalidate()
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

        val redPaint = Paint()
        redPaint.color = Color.RED
        redPaint.strokeWidth = 5F

        for (position in mainMap.positions) {
            val matrix = Matrix()
            val otherMap = position.map
            matrix.postScale(position.scale, position.scale)
            matrix.postTranslate(position.translateX, position.translateY)
            val mapLayer = imageView.LayerDescription(imageView.BitMapLayer(otherMap.bitmap, otherMap.preview), matrix)
            val action = mainMap.actions[otherMap]
            if (action == null) {
                MessageShower.warn("Action not found for map ${otherMap.id}")
            } else {
                mapLayer.onDoubleTapListener = {
                    findNavController().navigate(action)
                    true
                }
            }

            val borders = imageView.RectangleLayer(
                0F,
                0F,
                otherMap.bitmap.width.toFloat(),
                otherMap.bitmap.height.toFloat(),
                false,
                redPaint)
            // TODO is copying matrix necessary?
            val bordersLayer = imageView.LayerDescription(borders, Matrix(matrix))

            val layer = NonMainLayer(mapLayer, bordersLayer)

            bordersLayer.onSingleTapConfirmedListener = {
                layer.isMapShown = !layer.isMapShown
                true
            }

            nonMainLayers.add(layer)

            // TODO add layers recursively
        }
    }
}