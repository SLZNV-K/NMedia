package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val apiService: PostApiService
) :
    ViewModel() {

    fun registration(login: String, pass: String, name: String, photo: PhotoModel?) {
        viewModelScope.launch {
            try {
                val response = if (photo != null) {
                    val media = MultipartBody.Part.createFormData(
                        "file",
                        photo.file!!.name,
                        photo.file.asRequestBody()
                    )
                    apiService.registerWithPhoto(
                        login.toRequestBody("text/plain".toMediaType()),
                        pass.toRequestBody("text/plain".toMediaType()),
                        name.toRequestBody("text/plain".toMediaType()),
                        media
                    )
                } else apiService.registration(login, pass, name)

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                appAuth.setAuth(body.id, body.token!!)
            } catch (e: IOException) {
                throw UnknownError()
            } catch (e: Exception) {
                throw UnknownError()
            }
        }
    }
}