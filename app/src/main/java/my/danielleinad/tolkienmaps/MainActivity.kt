package my.danielleinad.tolkienmaps

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import my.danielleinad.tolkienmaps.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}