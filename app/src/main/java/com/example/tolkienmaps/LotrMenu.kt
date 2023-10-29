package com.example.tolkienmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tolkienmaps.databinding.FragmentLotrMenuBinding

class LotrMenu : Fragment() {
    private lateinit var binding: FragmentLotrMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO pick a style and use one
        binding = FragmentLotrMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.middleEarthButton.setOnClickListener {
            findNavController().navigate(R.id.action_lotrMenu_to_middleEarthMap)
        }

        binding.shireButton.setOnClickListener {
            // TODO
            showMessage("Shire")
        }

        binding.rohanGondorMordorButton.setOnClickListener {
            findNavController().navigate(R.id.action_lotrMenu_to_rohanGondorMordorMap)
        }

    }

    private fun showMessage(text: String) {
        val myToast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        myToast.show()
    }
}