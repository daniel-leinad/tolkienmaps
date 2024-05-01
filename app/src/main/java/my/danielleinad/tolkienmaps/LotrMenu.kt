package my.danielleinad.tolkienmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import my.danielleinad.tolkienmaps.databinding.FragmentLotrMenuBinding

class LotrMenu : Fragment() {
    private lateinit var binding: FragmentLotrMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLotrMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.middleEarthButton.setOnClickListener {
            findNavController().navigate(R.id.action_lotrMenu_to_middleEarthMap)
        }

        binding.shireButton.setOnClickListener {
            findNavController().navigate(R.id.action_lotrMenu_to_shireMap)
        }

        binding.rohanGondorMordorButton.setOnClickListener {
            findNavController().navigate(R.id.action_lotrMenu_to_rohanGondorMordorMap)
        }

    }
}