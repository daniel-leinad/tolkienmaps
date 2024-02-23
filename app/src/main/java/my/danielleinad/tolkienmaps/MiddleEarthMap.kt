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
import my.danielleinad.tolkienmaps.databinding.FragmentMiddleEarthMapBinding

class MiddleEarthMap : Fragment() {
    private lateinit var binding: FragmentMiddleEarthMapBinding
    private var areLayersShown: Boolean = false
    private lateinit var mainBitmap: ImageScaleView.BitMapLayer
    private lateinit var wilderlandBitmap: ImageScaleView.BitMapLayer
    private val nonMainLayers: MutableList<ImageScaleView.LayerDescription> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // TODO pick a style and use one
        binding = FragmentMiddleEarthMapBinding.inflate(layoutInflater)
        mainBitmap = binding.imageView.BitMapLayer(
            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth),
            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth_preview_3))
        wilderlandBitmap = binding.imageView.BitMapLayer(
            BitmapFactory.decodeResource(resources, R.drawable.map_wilderland),
            BitmapFactory.decodeResource(resources, R.drawable.map_wilderland_preview))

        binding.imageView.layers.add(binding.imageView.LayerDescription(mainBitmap, Matrix()))

        val wilderlandMatrix = Matrix()
        wilderlandMatrix.postScale(0.269F, 0.269F)
        wilderlandMatrix.postTranslate(1720F, 672F)
        val wilderlandLayer = binding.imageView.LayerDescription(wilderlandBitmap, wilderlandMatrix)
        wilderlandLayer.onClickListener = {
            MessageShower.show("clicked wilderland map!")
            true
        }
        binding.imageView.layers.add(wilderlandLayer)
        nonMainLayers.add(wilderlandLayer)

        val redPaint = Paint()
        redPaint.color = Color.RED
        redPaint.strokeWidth = 5F
        val wilderlandBorders = binding.imageView.RectangleLayer(
            0f, 0f, wilderlandBitmap.width, wilderlandBitmap.height, false, redPaint)
        // is copy of wilderlandMatrix necessary?
        val wilderlandBordersLayer = binding.imageView.LayerDescription(wilderlandBorders, Matrix(wilderlandMatrix))
        wilderlandBordersLayer.onClickListener = {
            wilderlandLayer.activated = !wilderlandLayer.activated
            true
        }
        binding.imageView.layers.add(wilderlandBordersLayer)
        nonMainLayers.add(wilderlandBordersLayer)

        fun hideShowLayers() {
            for (layerDescription in nonMainLayers) {
                layerDescription.activated = areLayersShown
            }

            binding.imageView.invalidate()
        }

        binding.showHideLayers.setOnClickListener {
            areLayersShown = !areLayersShown
            hideShowLayers()
        }

        hideShowLayers()

        return binding.root
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