package com.example.tolkienmaps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.davemorrissey.labs.subscaleview.ImageSource
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
        binding.imageView.setImage(ImageSource.resource(R.drawable.map_middle_earth))
        return binding.root
    }
}