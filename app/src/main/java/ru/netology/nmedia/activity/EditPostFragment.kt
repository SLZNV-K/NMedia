package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityEditPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.PostDiffCallBack
import ru.netology.nmedia.util.PostListCallback
import ru.netology.nmedia.util.load
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class EditPostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ActivityEditPostBinding.inflate(layoutInflater)
        val id = arguments?.getLong("EXTRA_ID")

        val differ = AsyncPagingDataDiffer(
            diffCallback = PostDiffCallBack,
            updateCallback = PostListCallback(),
            workerDispatcher = Dispatchers.Main
        )

        with(binding) {
            editContent.requestFocus()
            editContent.setText(arguments?.getString("EXTRA_CONTENT"))
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


//            viewModel.data.observe(viewLifecycleOwner) { state ->
//                val post = state.posts.find { it.id == id }
//                if (post?.attachment != null) {
//                    attachment.visibility = View.VISIBLE
//                    attachment.load("${BASE_URL}/media/${post.attachment!!.url}")
//                }
//            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.data.collectLatest { pagerData: PagingData<Post> ->
                    differ.submitData(pagerData)
                    val post = differ.snapshot().items.find { it.id == id }
                    if (post?.attachment != null) {
                        attachment.visibility = View.VISIBLE
                        attachment.load("${BASE_URL}/media/${post.attachment!!.url}")
                    }
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        AlertDialog.Builder(requireActivity()).apply {
                            setTitle(getString(R.string.cancel_editing))
                            setMessage(getString(R.string.cancel_editing_text))
                            setPositiveButton(getString(R.string.yes)) { _, _ ->
                                viewModel.cancel()
                                AndroidUtils.hideKeyboard(binding.editContent)
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
}

