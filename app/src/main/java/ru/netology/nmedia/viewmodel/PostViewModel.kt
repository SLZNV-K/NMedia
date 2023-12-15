package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryRetrofitImpl
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryRetrofitImpl()
    private val _state = MutableLiveData(FeedState())
    val data: LiveData<FeedState>
        get() = _state
    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        load()
    }

    fun load() {
        _state.postValue(FeedState(loading = true))
        repository.getAllAsync(object : PostRepository.GetAllCallBack<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _state.value = FeedState(posts = data, empty = data.isEmpty())
            }

            override fun onError(e: Exception) {
                _state.value = FeedState(error = true)
            }
        })
    }

    fun likeById(post: Post) {
        if (!post.likedByMe) repository.likeById(
            post.id,
            object : PostRepository.GetAllCallBack<Post> {
                override fun onSuccess(data: Post) {
                    val updatePosts =
                        _state.value?.posts?.map { if (it.id == data.id) data else it }.orEmpty()
                    _state.value = FeedState(posts = updatePosts, empty = updatePosts.isEmpty())
                }

                override fun onError(e: Exception) {
                    _state.value = _state.value?.copy(actionError = true)
                }

            }) else repository.dislikeById(post.id,
            object : PostRepository.GetAllCallBack<Post> {
                override fun onSuccess(data: Post) {
                    val updatePosts =
                        _state.value?.posts?.map { if (it.id == data.id) data else it }.orEmpty()
                    _state.value = FeedState(posts = updatePosts, empty = updatePosts.isEmpty())
                }

                override fun onError(e: Exception) {
                    _state.value = _state.value?.copy(actionError = true)
                }
            })
    }

    fun shareById(id: Long) {
        thread { repository.shareById(id) }
    }

    fun removeById(id: Long) {
        repository.removeById(id, object : PostRepository.GetAllCallBack<Unit> {
            override fun onSuccess(data: Unit) {
                _state.value = _state.value?.copy(posts = _state.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            }

            override fun onError(e: Exception) {
                _state.value = FeedState(error = true)
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.GetAllCallBack<Post> {
                override fun onSuccess(data: Post) {
                    edited.value = empty
                    _postCreated.value = Unit
                    load()
                }

                override fun onError(e: Exception) {
                    _state.value = FeedState(error = true)
                }
            })
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        edited.value?.let { post ->
            val text = content.trim()
            if (text != post.content) {
                edited.value = post.copy(content = text)
            }
        }
    }

    fun cancel() {
        edited.value = empty
    }
}