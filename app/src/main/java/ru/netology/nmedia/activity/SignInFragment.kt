package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel


class SignInFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(layoutInflater)
        val viewModel by activityViewModels<SignInViewModel>()

        with(binding) {
            val login = login.text
            val password = password.text

//            viewModel.authStateModel.observe(viewLifecycleOwner) { state ->
//                if (state.error) {
//                    groupSuccess.visibility = View.GONE
//                    groupError.visibility = View.VISIBLE
//                } else {
//                    findNavController().navigateUp()
//                }
//            }
            signInButton.setOnClickListener {
                if (login.isBlank() || password.isBlank()) {
                    Toast.makeText(
                        requireActivity(),
                        R.string.empty_text_error,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    viewModel.updateUser(login.toString(), password.toString())
                    val previousFragment =
                        findNavController().previousBackStackEntry?.destination?.id
                    if (previousFragment == R.id.signUpFragment) {
                        findNavController().navigate(R.id.feedFragment)
                    } else findNavController().navigateUp()
                }
            }

            signUpButton.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
            }
        }
        return binding.root
    }
}