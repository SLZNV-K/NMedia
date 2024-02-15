package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.databinding.FragmentPhotoBinding
import ru.netology.nmedia.util.load
@AndroidEntryPoint
class PhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        val uri = arguments?.getString("EXTRA_URI")
        with(binding) {
            fullScreenPhoto.load("${BASE_URL}/media/${uri}")

            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            return root
        }
    }
}