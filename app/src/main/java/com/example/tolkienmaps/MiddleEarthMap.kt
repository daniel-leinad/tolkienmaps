package com.example.tolkienmaps

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tolkienmaps.databinding.FragmentMiddleEarthMapBinding

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
        return binding.root
    }
}