package ru.netology.nmedia.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var provideGoogleApiAvailability: GoogleApiAvailability

    @Inject
    lateinit var provideRequest: FirebaseMessaging

    private val authViewModel: AuthViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationsPermission()

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                Toast.makeText(this, R.string.empty_text_error, Toast.LENGTH_SHORT).show()
                return@let
            }
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.let {
                    it.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
                    it.setGroupVisible(R.id.authenticated, authViewModel.authenticated)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signIn -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
                        true
                    }

                    R.id.signUp -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.signUpFragment)
                        true
                    }

                    R.id.signOut -> {
                        if (findNavController(R.id.nav_host_fragment).currentDestination?.id == R.id.newPostFragment) {
                            AlertDialog.Builder(this@AppActivity).apply {
                                setTitle(getString(R.string.are_you_sure))
                                setMessage(getString(R.string.if_you_exit_now_progress_will_be_lost))
                                setPositiveButton(getString(R.string.get_out_anyway)) { _, _ ->
                                    appAuth.removeAuth()
                                    findNavController(R.id.nav_host_fragment).navigateUp()
                                }
                                setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                }
                                setCancelable(true)
                            }.create().show()

                        } else {
                            appAuth.removeAuth()
                        }
                        postViewModel.updatePosts()
                        true
                    }

                    else -> false
                }
        })
        checkGoogleApiAvailability()
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(permission), 1)
        provideRequest.token.addOnCompleteListener(::println)
    }

    private fun checkGoogleApiAvailability() {
        with(provideGoogleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, "Google ApiUnavailable", Toast.LENGTH_LONG).show()
        }
    }
}