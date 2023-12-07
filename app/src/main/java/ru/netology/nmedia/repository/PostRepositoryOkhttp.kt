package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepositoryOkhttp : PostRepository {

    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}.type

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()

    }

    override fun getAllAsync(callBack: PostRepository.GetAllCallBack<List<Post>>) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callBack.onSuccess(gson.fromJson(body, typeToken))
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }
            })
    }


    override fun likeById(id: Long, callBack: PostRepository.GetAllCallBack<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/posts/$id/likes")
            .post(EMPTY_REQUEST)
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callBack.onSuccess(gson.fromJson(body, Post::class.java))
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }

            })
    }

    override fun dislikeById(id: Long, callBack: PostRepository.GetAllCallBack<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/posts/$id/likes")
            .delete(EMPTY_REQUEST)
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    callBack.onSuccess(gson.fromJson(body, Post::class.java))
                }
            })
    }

    override fun shareById(id: Long) {

    }

    override fun removeById(id: Long, callBack: PostRepository.GetAllCallBack<Unit>) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    callBack.onSuccess(Unit)
                }
            })
    }

    override fun save(post: Post, callBack: PostRepository.GetAllCallBack<Post>) {
        val request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    callBack.onSuccess(gson.fromJson(body, Post::class.java))
                }
            })
    }
}