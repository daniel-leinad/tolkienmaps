package my.danielleinad.tolkienmaps

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
    private lateinit var wilderlandBitmap: ImageScaleView.BitMapLayer

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
        mainBitmap = binding.imageView.BitMapLayer(
            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth),
            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth_preview_3))
        wilderlandBitmap = binding.imageView.BitMapLayer(
            BitmapFactory.decodeResource(resources, R.drawable.map_wilderland),
            BitmapFactory.decodeResource(resources, R.drawable.map_wilderland_preview))

        constructNonMainLayers()

        binding.imageView.layers.add(binding.imageView.LayerDescription(mainBitmap, Matrix()))
        for (layer in nonMainLayers) {
            binding.imageView.layers.add(layer.map)
            binding.imageView.layers.add(layer.borders)
        }

        fun hideShowLayers() {
            for (layerDescription in nonMainLayers) {
                layerDescription.map.activated = areNonMainLayersShown and layerDescription.isMapShown
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
        val wilderlandMatrix = Matrix()
        wilderlandMatrix.postScale(0.269F, 0.269F)
        wilderlandMatrix.postTranslate(1720F, 672F)
        val wilderlandMapLayer =
            binding.imageView.LayerDescription(wilderlandBitmap, wilderlandMatrix)
        wilderlandMapLayer.onDoubleTapListener = {
            findNavController().navigate(R.id.action_middleEarthMap_to_wilderlandMap)
            true
        }

        val redPaint = Paint()
        redPaint.color = Color.RED
        redPaint.strokeWidth = 5F
        val wilderlandBorders = binding.imageView.RectangleLayer(
            0f, 0f, wilderlandBitmap.width, wilderlandBitmap.height, false, redPaint
        )
        // is copy of wilderlandMatrix necessary?
        val wilderlandBordersLayer =
            binding.imageView.LayerDescription(wilderlandBorders, Matrix(wilderlandMatrix))

        val wilderlandLayer = NonMainLayer(wilderlandMapLayer, wilderlandBordersLayer)

        wilderlandBordersLayer.onSingleTapConfirmedListener = {
            wilderlandLayer.isMapShown = !wilderlandLayer.isMapShown
            true
        }

        nonMainLayers.add(wilderlandLayer)
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