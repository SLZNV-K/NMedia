package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel by activityViewModels<PostViewModel>()
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        binding.newContent.requestFocus()
        binding.saveNewPost.setOnClickListener {
            if (!binding.newContent.text.isNullOrBlank()) {
                val content = binding.newContent.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireActivity(), R.string.empty_text_error, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return binding.root
    }
}