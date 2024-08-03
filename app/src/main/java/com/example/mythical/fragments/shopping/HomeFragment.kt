package com.example.mythical.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mythical.R
import com.example.mythical.adapters.HomeViewpagerAdapter
import com.example.mythical.databinding.FragmentHomeBinding
import com.example.mythical.fragments.categories.AccesoryFragment
import com.example.mythical.fragments.categories.MainCategoryFragment
import com.example.mythical.fragments.categories.PantsFragment
import com.example.mythical.fragments.categories.ShirtFragment
import com.example.mythical.fragments.categories.ShoeFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var  binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment(),
            ShirtFragment(),
            PantsFragment(),
            ShoeFragment(),
            AccesoryFragment()
        )

        binding.viewpagerHome.isUserInputEnabled = false

        val viewPager2Adapter = HomeViewpagerAdapter(categoriesFragments,childFragmentManager,lifecycle)
        binding.viewpagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout,binding.viewpagerHome){tab, position ->
            when (position){
                0 -> tab.text = "Main"
                1 -> tab.text = "Camisas"
                2 -> tab.text = "Pantalones"
                3 -> tab.text = "Zapatos"
                4 -> tab.text = "Accesorios"
            }
        }.attach()

    }
}