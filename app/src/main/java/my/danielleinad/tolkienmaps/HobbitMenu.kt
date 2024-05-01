package my.danielleinad.tolkienmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import my.danielleinad.tolkienmaps.databinding.FragmentHobbitMenuBinding


class HobbitMenu : Fragment() {
    private lateinit var binding: FragmentHobbitMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHobbitMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.throrMapButton.setOnClickListener {
            findNavController().navigate(R.id.action_hobbitMenu_to_throrMap)
        }

        binding.wilderlandButton.setOnClickListener {
            findNavController().navigate(R.id.action_hobbitMenu_to_wilderlandMap)
        }

    }
}