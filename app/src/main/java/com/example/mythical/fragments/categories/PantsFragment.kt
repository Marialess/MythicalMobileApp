package com.example.mythical.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mythical.data.Category
import com.example.mythical.util.Resource
import com.example.mythical.viewmodel.CategoryViewModel
import com.example.mythical.viewmodel.factory.BaseCategoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PantsFragment: BaseCategoryFragment() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryViewModel> {
        BaseCategoryViewModelFactory(firestore, Category.Pants)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.offerProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showOfferLoading()
                        }
                        is Resource.Success -> {
                            offerAdapter.differ.submitList(it.data)
                            hideOfferLoading()
                        }
                        is Resource.Error -> {
                            Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                            hideOfferLoading()
                        }
                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showBestProductsLoading()
                        }
                        is Resource.Success -> {
                            bestProductAdapter.differ.submitList(it.data)
                            hideBestProductsLoading()
                        }
                        is Resource.Error -> {
                            Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                            hideBestProductsLoading()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onBestProductsRequest() {

    }

    override fun onOfferPagingRequest() {

    }

}