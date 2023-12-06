package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryOkhttp
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryOkhttp()
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
        thread {
            _state.postValue(FeedState(loading = true))
            try {
                val posts = repository.getAll()
                _state.postValue(FeedState(posts = posts, empty = posts.isEmpty()))
            } catch (e: Exception) {
                _state.postValue(FeedState(error = true))
            }
        }
    }

    fun likeById(post: Post) {
        thread {
            val updatePost =
                if (!post.likedByMe) repository.likeById(post.id) else repository.dislikeById(post.id)
            val updatePosts = _state.value?.posts?.map {
                if (it.id == updatePost.id) updatePost else it
            }
            _state.postValue(_state.value?.copy(posts = updatePosts.orEmpty()))
        }
    }

    fun shareById(id: Long) {
        thread { repository.shareById(id) }
    }

    fun removeById(id: Long) {
        thread {
            // Оптимистичная модель
            val old = _state.value?.posts.orEmpty()
            _state.postValue(
                _state.value?.copy(posts = _state.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _state.postValue(_state.value?.copy(posts = old))
            }
        }
    }

    fun save() {
        thread {
            edited.value?.let {
                repository.save(it)
                edited.postValue(empty)
                _postCreated.postValue(Unit)
                load()
            }
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