package my.danielleinad.tolkienmaps

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
//        binding.imageView.setImage(ImageSource.resource(R.drawable.map_middle_earth))
        binding.imageView.imageSource1 = BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth)
        binding.imageView.imageSource2 = BitmapFactory.decodeResource(resources, R.drawable.transparent_test)
        binding.imageView.imageSource1Preview = BitmapFactory.decodeResource(resources, R.drawable.map_middle_earth_preview_2)
        binding.imageView.imageSource2Preview = BitmapFactory.decodeResource(resources, R.drawable.transparent_test_preview)
//        binding.imageView.image2Matrix.postScale(0.5f, 0.5f)
//        binding.imageView.imageSource2 = BitmapFactory.decodeResource(resources, R.drawable.transparent_test)
//        binding.imageView.imageSource1 = createBitmap()
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