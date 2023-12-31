package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositoryFilesImpl(private val context: Context) : PostRepository {

    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val filename = "posts.json"

    private var nextId = 1L
    private var posts = emptyList<Post>()
        set(value) {
            field = value
            sync()
        }
    private val data = MutableLiveData(posts)

    init {
        val file = context.filesDir.resolve(filename)
        if (file.exists()) {
            context.openFileInput(filename).bufferedReader().use {
                posts = gson.fromJson(it, type)
                nextId = posts.maxOf { it.id } + 1
                data.value = posts
            }
        }

    }

    override fun getAllAsync(callBack: PostRepository.GetAllCallBack<List<Post>>) {
    }

    override fun likeById(id: Long, callBack: PostRepository.GetAllCallBack<Post>) {
        TODO("Not yet implemented")
    }

    override fun dislikeById(id: Long, callBack: PostRepository.GetAllCallBack<Post>) {
        TODO("Not yet implemented")
    }


//    override fun get(): LiveData<List<Post>> = data

//    override fun likeById(id: Long) {
//        posts = posts.map {
//            if (it.id != id) it else it.copy(
//                likedByMe = !it.likedByMe,
//                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
//            )
//        }
//        data.value = posts
//    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                sharedByMe = true,
                shares = it.shares + 1
            )
        }
        data.value = posts
    }

    override fun removeById(id: Long, callBack: PostRepository.GetAllCallBack<Unit>) {
        TODO("Not yet implemented")
    }

//    override fun removeById(id: Long) {
//        posts = posts.filter { it.id != id }
//        data.value = posts
//    }

    override fun save(post: Post, callBack: PostRepository.GetAllCallBack<Post>) {
        TODO("Not yet implemented")
    }


//    override fun save(post: Post) {
//        posts = if (post.id == 0L) {
//            listOf(
//                post.copy(
//                    id = nextId++,
//                    author = "Me",
//                    published = "Now"
//                )
//            ) + posts
//        } else {
//            posts.map { if (it.id != post.id) it else it.copy(content = post.content) }
//        }
//        data.value = posts
//    }

    private fun sync() {
        context.openFileOutput(filename, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }
}