package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Post

class PostRepositoryRetrofitImpl : PostRepository {
    override fun getAllAsync(callBack: PostRepository.GetAllCallBack<List<Post>>) {
        return PostApiService.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    if (!response.isSuccessful) {
                        callBack.onError(Exception(response.errorBody()?.string()))
                        return
                    }
                    val body = response.body() ?: throw RuntimeException("body is null")
                    try {
                        callBack.onSuccess(body)
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callBack.onError(Exception(t))
                }
            })
    }

    override fun likeById(id: Long, callBack: PostRepository.GetAllCallBack<Post>) {
        return PostApiService.service.like(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callBack.onError(Exception(response.errorBody()?.string()))
                        return
                    }
                    val body = response.body() ?: throw RuntimeException("body is null")
                    try {
                        callBack.onSuccess(body)
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callBack.onError(Exception(t))
                }
            })
    }

    override fun dislikeById(id: Long, callBack: PostRepository.GetAllCallBack<Post>) {
        return PostApiService.service.dislike(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callBack.onError(Exception(response.errorBody()?.string()))
                        return
                    }
                    val body = response.body() ?: throw RuntimeException("body is null")
                    try {
                        callBack.onSuccess(body)
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callBack.onError(Exception(t))
                }
            })
    }

    override fun shareById(id: Long) {
    }

    override fun removeById(id: Long, callBack: PostRepository.GetAllCallBack<Unit>) {
        return PostApiService.service.delete(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (!response.isSuccessful) {
                    callBack.onError(Exception(response.errorBody()?.string()))
                    return
                }
                callBack.onSuccess(Unit)
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callBack.onError(Exception(t))
            }
        })
    }

    override fun save(post: Post, callBack: PostRepository.GetAllCallBack<Post>) {
        return PostApiService.service.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    callBack.onError(Exception(response.errorBody()?.string()))
                    return
                }
                val body = response.body() ?: throw RuntimeException("body is null")
                callBack.onSuccess(body)
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callBack.onError(Exception(t))
            }
        })
    }
}