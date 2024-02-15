package ru.netology.nmedia.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        binding.newContent.requestFocus()
        val sharedPreference = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        val text = sharedPreference?.getString("content", null)


        if (!text.isNullOrBlank()) binding.newContent.setText(text)

        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                        return@registerForActivityResult
                    }

                    Activity.RESULT_OK -> {
                        val uri = it.data?.data ?: return@registerForActivityResult
                        viewModel.savePhoto(PhotoModel(uri, uri.toFile()))
                    }
                }
            }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.previewGroup.visibility = View.GONE
                return@observe
            }
            binding.previewGroup.visibility = View.VISIBLE
            binding.previewPhoto.setImageURI(it.uri)
        }

        binding.clearButton.setOnClickListener {
            viewModel.clear()
        }

        binding.galleryButton.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .createIntent { launcher.launch(it) }
        }

        binding.takePhotoButton.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .createIntent { launcher.launch(it) }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (!binding.newContent.text.isNullOrBlank()) {
                            viewModel.changeContent(binding.newContent.text.toString())
                            viewModel.save()
                            binding.previewGroup.visibility = View.GONE
                            AndroidUtils.hideKeyboard(requireView())
                            editor?.clear()
                            editor?.apply()
                            findNavController().navigateUp()
                            true
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                R.string.empty_text_error,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            false
                        }
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        viewModel.postCreated.observe(viewLifecycleOwner) {
            AndroidUtils.hideKeyboard(requireView())
            editor?.clear()
            editor?.apply()
            findNavController().navigateUp()
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