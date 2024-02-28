package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.reformatCount
import ru.netology.nmedia.databinding.FragmentPostDetailsBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.load
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private val viewModelAuth: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostDetailsBinding.inflate(layoutInflater, container, false)
        val post = arguments?.getSerializable("EXTRA_POST") as Post

        viewModel.dataState.observe(viewLifecycleOwner) {
            if (it.error) {
                Toast.makeText(
                    context,
                    "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
                return@observe
            }
        }

        with(binding) {
            author.text = post.author
            published.text = post.published
            avatar.load("${BASE_URL}/avatars/${post.authorAvatar}", true)
            newContent.text = post.content
            like.isChecked = post.likedByMe
            like.text = reformatCount(post.likes)
            share.isClickable = post.sharedByMe
            share.text = reformatCount(post.shares)
            viewingCount.text = reformatCount(post.views)

            if (post.attachment != null) {
                attachment.visibility = View.VISIBLE
                attachment.load("${BASE_URL}/media/${post.attachment!!.url}")
            }

            attachment.setOnClickListener {
                findNavController()
                    .navigate(R.id.action_postDetailsFragment_to_photoFragment,
                        Bundle().apply { putString("EXTRA_URI", post.attachment?.url) })
            }

            like.setOnClickListener {
                if (viewModelAuth.authenticated) {
                    viewModel.likeById(post)
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
            share.setOnClickListener {
                viewModel.shareById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val chooser = Intent.createChooser(intent, null)
                startActivity(chooser)
            }
            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                findNavController()
                                    .navigate(
                                        R.id.action_postDetailsFragment_to_editPostFragment,
                                        Bundle().apply {
                                            putString("EXTRA_CONTENT", post.content)
                                            putLong("EXTRA_ID", post.id)
                                        }
                                    )
                                viewModel.edit(post)
                                true
                            }

                            R.id.remove -> {
                                viewModel.removeById(post.id)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }

        return binding.root
    }
}