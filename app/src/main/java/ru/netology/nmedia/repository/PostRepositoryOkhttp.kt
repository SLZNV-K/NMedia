package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryOkhttp : PostRepository {

    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}.type

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()

    }

    override fun getAll(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .build()

        return client.newCall(request).execute().body?.string().let { gson.fromJson(it, typeToken) }

    }

    override fun likeById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}/api/posts/$id/likes")
            .post(EMPTY_REQUEST)
            .build()

        return client.newCall(request).execute().body?.string()
            .let { gson.fromJson(it, Post::class.java) }
    }

    override fun dislikeById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}/api/posts/$id/likes")
            .delete(EMPTY_REQUEST)
            .build()

        return client.newCall(request).execute().body?.string()
            .let { gson.fromJson(it, Post::class.java) }
    }

    override fun shareById(id: Long) {
        //TODO
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/posts/$id")
            .build()

        client.newCall(request).execute().close()
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        return client.newCall(request).execute().body?.string()
            .let { gson.fromJson(it, Post::class.java) }
    }
}