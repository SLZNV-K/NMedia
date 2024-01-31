package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.load
import ru.netology.nmedia.viewmodel.SignUpViewModel


class SignUpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(layoutInflater)
        val viewModel: SignUpViewModel by activityViewModels()

        with(binding) {
            val name = name.text
            val login = login.text
            val password = password.text
            val passwordConfirmation = passwordConfirmation.text
            var photo: PhotoModel? = null

            val launcher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    when (it.resultCode) {
                        ImagePicker.RESULT_ERROR -> {
                            Snackbar.make(
                                binding.root,
                                ImagePicker.getError(it.data),
                                Snackbar.LENGTH_LONG
                            ).show()
                            return@registerForActivityResult
                        }

                        Activity.RESULT_OK -> {
                            val uri = it.data?.data ?: return@registerForActivityResult
                            avatar.load(uri.toString(), true)
                            photo = PhotoModel(uri, uri.toFile())
                        }
                    }
                }

            addAvatarButton.setOnClickListener {
                ImagePicker.with(this@SignUpFragment)
                    .crop()
                    .compress(64)
                    .createIntent { launcher.launch(it) }
            }

            signUpButton.setOnClickListener {
                if (password.equals(passwordConfirmation)) {
                    Toast.makeText(
                        context,
                        getString(R.string.passwords_don_t_match),
                        Toast.LENGTH_LONG
                    ).show()
                } else
                    if (login.isBlank() || name.isBlank() ||
                        password.isBlank() || passwordConfirmation.isBlank()
                    ) {
                        Toast.makeText(
                            context,
                            getString(R.string.fields_cannot_be_empty),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        viewModel.registration(
                            login.toString(),
                            password.toString(),
                            name.toString(),
                            photo
                        )
                        val previousFragment =
                            findNavController().previousBackStackEntry?.destination?.id
                        if (previousFragment == R.id.signInFragment) {
                            findNavController().navigate(R.id.feedFragment)
                        } else findNavController().navigateUp()
                    }
            }

            signInButton.setOnClickListener {
                findNavController().navigate(R.id.signInFragment)
            }
        }
        return binding.root
    }

}