package com.example.mythical.fragments.loginRegister

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mythical.R
import com.example.mythical.data.User
import com.example.mythical.databinding.FragmentRegisterBinding
import com.example.mythical.util.RegisterValidation
import com.example.mythical.util.Resource
import com.example.mythical.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val TAG = "RegisterFragment"

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvHaveAnAccount.setOnClickListener{
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.apply {
            buttonRegisterRegister.setOnClickListener {
                val user = User(
                    edFirstName.text.toString().trim(),
                    edLastName.text.toString().trim(),
                    edEmailRegister.text.toString().trim()
                )
                val password = edPasswordRegister.text.toString()
                viewModel.createAccountWithEmailAndPassword(user, password)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.register.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonRegisterRegister.startAnimation()
                        }
                        is Resource.Success -> {
                            Log.d("test", it.data.toString())
                            binding.buttonRegisterRegister.revertAnimation()
                        }
                        is Resource.Error -> {
                            Log.e(TAG, it.message.toString())
                            binding.buttonRegisterRegister.revertAnimation()
                        }
                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { validation ->
                    if (validation.email is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.edEmailRegister.apply {
                                requestFocus()
                                error = validation.email.message
                            }
                        }
                    }

                    if (validation.password is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.edPasswordRegister.apply {
                                requestFocus()
                                error = validation.password.message
                            }
                        }
                    }
                }
            }
        }
    }
}