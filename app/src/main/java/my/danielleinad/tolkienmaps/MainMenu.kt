package my.danielleinad.tolkienmaps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.danielleinad.tolkienmaps.databinding.FragmentMainMenuBinding


class MainMenu : Fragment() {

    private lateinit var binding: FragmentMainMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.aboutButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_aboutFragment)
        }

        binding.mainMapButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_middleEarthMap)
        }

        binding.hobbitButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_hobbitMenu)
        }

        binding.lotrButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_lotrMenu)
        }

        binding.silmarillionButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_silmarillionMenu)
        }

        binding.atlasOfMiddleEarthButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_atlasOfMiddleEarthMenuFragment)
        }
    }

}