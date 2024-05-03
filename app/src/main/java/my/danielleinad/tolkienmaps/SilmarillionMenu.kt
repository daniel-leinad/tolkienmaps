package my.danielleinad.tolkienmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import my.danielleinad.tolkienmaps.databinding.FragmentSilmarillionMenuBinding


class SilmarillionMenu : Fragment() {
    private lateinit var binding: FragmentSilmarillionMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSilmarillionMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.beleriandButton.setOnClickListener {
            findNavController().navigate(R.id.action_silmarillionMenu_to_beleriandMap)
        }

    }
}