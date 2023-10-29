package com.example.tolkienmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.example.tolkienmaps.databinding.FragmentHobbitMenuBinding


class HobbitMenu : Fragment() {
    private lateinit var binding: FragmentHobbitMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO pick a style and use one
        binding = FragmentHobbitMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.throrMapButton.setOnClickListener {
            // TODO
            showMessage("Thror's map")
        }

        binding.wilderlandButton.setOnClickListener {
            // TODO
            showMessage("Wilderland")
        }

    }

    private fun showMessage(text: String) {
        val myToast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        myToast.show()
    }
}