package com.example.tolkienmaps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class RohanGondorMordorMap : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // TODO pick a style and use one
        return inflater.inflate(R.layout.fragment_rohan_gondor_mordor_map, container, false)
    }
}