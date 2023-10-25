package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
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
        val sharedPreference = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        val text = sharedPreference?.getString("content", null)


        if (!text.isNullOrBlank()) binding.newContent.setText(text)

        binding.saveNewPost.setOnClickListener {
            if (!binding.newContent.text.isNullOrBlank()) {
                val content = binding.newContent.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
                editor?.clear()
                editor?.apply()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireActivity(), R.string.empty_text_error, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(requireActivity()).apply {
                        setTitle(getString(R.string.cancel_post_creation))
                        setMessage(getString(R.string.cancel_post_creation_text))
                        setPositiveButton(getString(R.string.yes)) { _, _ ->

                            editor?.putString("content", binding.newContent.text.toString())
                            editor?.apply()

                            viewModel.cancel()
                            AndroidUtils.hideKeyboard(binding.newContent)
                            findNavController().navigateUp()
                        }
                        setNegativeButton(getString(R.string.no)) { _, _ -> }
                        setCancelable(true)
                    }.create().show()
                }
            }
        )
        return binding.root
    }
}