package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushContent
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FSMService : FirebaseMessagingService() {
    private val channelId = "server"

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val ourId = appAuth.authState.value.id
        val pushContent = Gson().fromJson(message.data["content"], PushContent::class.java)

        if (pushContent.recipientId == ourId || pushContent.recipientId == null) {
            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(pushContent.content)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()

            notify(notification)
        } else {
            appAuth.sendPushToken()
        }
        println(message)
    }


    private fun handlePost(post: Post) {
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_user_posted, post.userName))
            .setContentText(post.content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(post.content)
            )
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        notify(notification)
    }

    private fun handleLike(like: Like) {
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(
                getString(
                    R.string.notification_user_liked,
                    like.userName,
                    like.postAuthor
                )
            )
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }
}

enum class Actions {
    LIKE, POST, ERROR;

    companion object {
        fun getValueOf(actions: String): Actions {
            return try {
                valueOf(actions)
            } catch (e: RuntimeException) {
                ERROR
            }
        }
    }
}

data class Like(
    val userId: Int,
    val userName: String,
    val postId: Int,
    val postAuthor: String
)

data class Post(
    val userName: String,
    val content: String
)