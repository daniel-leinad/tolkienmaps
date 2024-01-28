package my.danielleinad.tolkienmaps

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import my.danielleinad.tolkienmaps.databinding.FragmentMiddleEarthMapBinding

class MiddleEarthMap : Fragment() {
    private lateinit var binding: FragmentMiddleEarthMapBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // TODO pick a style and use one
        binding = FragmentMiddleEarthMapBinding.inflate(layoutInflater)
        binding.imageView.addLayer(
            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth),
            BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth_preview_2),
            Matrix()
        )
        binding.imageView.addLayer(
            BitmapFactory.decodeResource(resources, R.drawable.transparent_test),
            BitmapFactory.decodeResource(resources, R.drawable.transparent_test_preview),
            Matrix()
        )
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