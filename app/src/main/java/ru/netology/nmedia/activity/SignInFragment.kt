package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.SignInViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private val authViewModel: SignInViewModel by activityViewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(layoutInflater)

        with(binding) {
            val log = login.text
            val pass = password.text

            authViewModel.authStateModel.observe(viewLifecycleOwner) { state ->
                if (state!!.error) {
                    login.error = getString(R.string.invalid_login)
                    password.error = getString(R.string.invalid_password)
                } else {
                    val previousFragment =
                        findNavController().previousBackStackEntry?.destination?.id
                    if (previousFragment == R.id.signUpFragment) {
                        findNavController().navigate(R.id.feedFragment)
                    } else findNavController().navigateUp()
                }
            }

            signInButton.setOnClickListener {
                if (log.isBlank() || pass.isBlank()) {
                    Toast.makeText(
                        requireActivity(),
                        R.string.empty_text_error,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    authViewModel.updateUser(log.toString(), pass.toString())
                    postViewModel.updatePosts()
                }
            }

            signUpButton.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
            }
        }
        return binding.root
    }
}