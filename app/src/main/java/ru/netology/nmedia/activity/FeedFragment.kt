package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel


@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModelPost: PostViewModel by activityViewModels()
    private val viewModelAuth: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (viewModelAuth.authenticated) {
                    viewModelPost.likeById(post)
                } else {
                    AlertDialog.Builder(requireActivity()).apply {
                        setTitle(getString(R.string.sign_in))
                        setMessage(getString(R.string.to_interact_with_posts_you_need_to_log_in))
                        setPositiveButton(getString(R.string.sign_in)) { _, _ ->
                            findNavController().navigate(R.id.signInFragment)
                        }
                        setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                        setCancelable(true)
                    }.create().show()
                }
            }

            override fun onShare(post: Post) {
                viewModelPost.shareById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val chooser = Intent.createChooser(intent, null)
                startActivity(chooser)
            }

            override fun onRemove(post: Post) {
                viewModelPost.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                findNavController()
                    .navigate(
                        R.id.action_feedFragment_to_editPostFragment,
                        Bundle().apply {
                            putString("EXTRA_CONTENT", post.content)
                            putLong("EXTRA_ID", post.id)
                        }
                    )
                viewModelPost.edit(post)
            }

            override fun onPost(post: Post) {
                findNavController()
                    .navigate(
                        R.id.action_feedFragment_to_postDetailsFragment,
                        Bundle().apply { putLong("EXTRA_ID", post.id) })
            }

            override fun onPhoto(post: Post) {
                if (post.attachment == null) return
                findNavController()
                    .navigate(
                        R.id.action_feedFragment_to_photoFragment,
                        Bundle().apply { putString("EXTRA_URI", post.attachment?.url) })
            }
        })

        with(binding) {

            list.adapter = adapter

            viewModelPost.dataState.observe(viewLifecycleOwner) { state ->
                progress.isVisible = state.loading
                swiperefresh.isRefreshing = state.refreshing
                if (state.error) {
                    Snackbar.make(root, "Error loading", Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.addNewPostButton)
                        .setAction(R.string.retry_loading) { viewModelPost.load() }
                        .show()
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModelPost.data.collectLatest(adapter::submitData)
                }
            }

//            viewModelPost.newerCount.observe(viewLifecycleOwner) { count ->
//                if (count > 0) {
//                    getNewerPosts.visibility = View.VISIBLE
//                } else getNewerPosts.visibility = View.GONE
//
//                getNewerPosts.setOnClickListener {
//                    viewModelPost.getNewer(count.toLong())
//                    getNewerPosts.visibility = View.GONE
//                }
//            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    adapter.loadStateFlow.collectLatest { state ->
                        binding.swiperefresh.isRefreshing =
                            state.refresh is LoadState.Loading ||
                                    state.prepend is LoadState.Loading ||
                                    state.append is LoadState.Loading
                    }
                }
            }
            swiperefresh.setOnRefreshListener(adapter::refresh)

            addNewPostButton.setOnClickListener {
                if (viewModelAuth.authenticated) {
                    findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
                } else {
                    AlertDialog.Builder(requireActivity()).apply {
                        setTitle(getString(R.string.sign_in))
                        setMessage(getString(R.string.to_interact_with_posts_you_need_to_log_in))
                        setPositiveButton(getString(R.string.sign_in)) { _, _ ->
                            findNavController().navigate(R.id.signInFragment)
                        }
                        setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                        setCancelable(true)
                    }.create().show()
                }
            }
            return root
        }
    }
}