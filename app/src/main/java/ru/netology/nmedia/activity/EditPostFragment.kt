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
import ru.netology.nmedia.databinding.ActivityEditPostBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class EditPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ActivityEditPostBinding.inflate(layoutInflater)

        val viewModel: PostViewModel by activityViewModels()

        with(binding){
            editContent.requestFocus()
            editContent.setText(arguments?.textArg.orEmpty())
            saveEditPost.setOnClickListener {
                if (!binding.editContent.text.isNullOrBlank()) {
                    val content = binding.editContent.text.toString()
                    viewModel.changeContent(content)
                    viewModel.save()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireActivity(), R.string.empty_text_error, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            cancelButton.setOnClickListener {
                viewModel.cancel()
                findNavController().navigateUp()
            }
        }

        return binding.root
    }
}