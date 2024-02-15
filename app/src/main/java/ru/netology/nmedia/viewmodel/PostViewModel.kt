package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    authorAvatar = "",
    published = ""
)
private val noPhoto = PhotoModel()

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<FeedModel> = appAuth
        .authState
        .flatMapLatest { auth ->
            repository.data.map { posts ->
                FeedModel(
                    posts.map { it.copy(ownedByMe = it.authorId == auth.id) },
                    posts.isEmpty()
                )
            }
        }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)


    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0L).asLiveData(Dispatchers.Default)
    }

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _dataState.value = FeedModelState(loading = true)
        try {
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getNewer(id: Long) {
        repository.getNewer(id)
        viewModelScope.launch {
            try {
                repository.updatePosts()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }

    }

    fun savePhoto(photoModel: PhotoModel) {
        _photo.value = photoModel
    }

    fun clear() {
        _photo.value = null
    }

    fun shareById(id: Long) = viewModelScope.launch {
        try {
            repository.shareById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                if (!post.likedByMe) {
                    repository.likeById(post.id)
                } else {
                    repository.dislikeById(post.id)
                }
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it, _photo.value)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
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