package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.reformatCount
import ru.netology.nmedia.databinding.FragmentPostDetailsBinding
import ru.netology.nmedia.util.load
import ru.netology.nmedia.viewmodel.PostViewModel

class PostDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostDetailsBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by activityViewModels()
        val id = arguments?.textArg.orEmpty().toLong()

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val post = state.posts.find { it.id == id }
            if (post == null) {
                findNavController().navigateUp()
                return@observe
            }
            val BASE_URL = "http://10.0.2.2:9999"
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
                    attachment.load("${BASE_URL}/images/${post.attachment!!.url}")
                }

                like.setOnClickListener {
                    viewModel.likeById(post)
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
                                            Bundle().apply { textArg = post.content })
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
        }
        return binding.root
    }
}