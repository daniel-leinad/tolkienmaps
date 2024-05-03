package my.danielleinad.tolkienmaps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.danielleinad.tolkienmaps.databinding.FragmentAtlasOfMiddleEarthMenuBinding

class AtlasOfMiddleEarthMenuFragment : Fragment() {
    private lateinit var binding: FragmentAtlasOfMiddleEarthMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAtlasOfMiddleEarthMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ardaFirstAgeButton.setOnClickListener {
            findNavController().navigate(R.id.action_atlasOfMiddleEarthMenuFragment_to_ardaFirstAgeMap)
        }

        binding.ardaSecondAgeButton.setOnClickListener {
            findNavController().navigate(R.id.action_atlasOfMiddleEarthMenuFragment_to_ardaSecondAgeMap)
        }

        binding.numenorButton.setOnClickListener {
            findNavController().navigate(R.id.action_atlasOfMiddleEarthMenuFragment_to_numenorMap)
        }
    }
}