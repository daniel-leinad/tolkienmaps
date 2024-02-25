package my.danielleinad.tolkienmaps

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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

class MiddleEarthMap : Fragment() {
    private lateinit var binding: FragmentMiddleEarthMapBinding
    private var areNonMainLayersShown: Boolean = false
    private val nonMainLayers: MutableList<NonMainLayer> = mutableListOf()
    private lateinit var mainBitmap: ImageScaleView.BitMapLayer
//    private lateinit var wilderlandBitmap: ImageScaleView.BitMapLayer

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
//        mainBitmap = binding.imageView.BitMapLayer(
//            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth),
//            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth_preview_3))
//        wilderlandBitmap = binding.imageView.BitMapLayer(
//            BitmapFactory.decodeResource(resources, R.drawable.map_wilderland),
//            BitmapFactory.decodeResource(resources, R.drawable.map_wilderland_preview))

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
        val mainMap = mapsDescription.maps["middle_earth"]?: throw Exception("Unknown map: middle_earth")
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

//        val wilderlandMatrix = Matrix()
//        wilderlandMatrix.postScale(0.269F, 0.269F)
//        wilderlandMatrix.postTranslate(1720F, 672F)
//        val wilderlandMapLayer =
//            binding.imageView.LayerDescription(wilderlandBitmap, wilderlandMatrix)
//        wilderlandMapLayer.onDoubleTapListener = {
//            findNavController().navigate(R.id.action_middleEarthMap_to_wilderlandMap)
//            true
//        }
//
//        val redPaint = Paint()
//        redPaint.color = Color.RED
//        redPaint.strokeWidth = 5F
//        val wilderlandBorders = binding.imageView.RectangleLayer(
//            0f, 0f, wilderlandBitmap.width, wilderlandBitmap.height, false, redPaint
//        )
//        // is copy of wilderlandMatrix necessary?
//        val wilderlandBordersLayer =
//            binding.imageView.LayerDescription(wilderlandBorders, Matrix(wilderlandMatrix))
//
//        val wilderlandLayer = NonMainLayer(wilderlandMapLayer, wilderlandBordersLayer)
//
//        wilderlandBordersLayer.onSingleTapConfirmedListener = {
//            wilderlandLayer.isMapShown = !wilderlandLayer.isMapShown
//            true
//        }

//        nonMainLayers.add(wilderlandLayer)
    }

    private fun createBitmap(): Bitmap {
        val middleEarthBitMap = BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth)
        val transparentTestBitMap =
            BitmapFactory.decodeResource(resources, R.drawable.transparent_test)
        val bitmap = Bitmap.createBitmap(
            middleEarthBitMap.width,
            middleEarthBitMap.height,
            middleEarthBitMap.config
        )
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(middleEarthBitMap, 0f, 0f, null)
        val paint = Paint()
        canvas.drawBitmap(transparentTestBitMap, 0f, 0f, paint)
        return bitmap
    }
}