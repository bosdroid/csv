package com.boris.expert.csvmagic.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.boris.expert.csvmagic.view.fragments.CreateFragment
import com.boris.expert.csvmagic.view.fragments.ScanFragment


class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager,lifecycle){

    override fun createFragment(position: Int): Fragment {
        when (position) {
            1 -> return CreateFragment()
        }
        return ScanFragment()
    }

    override fun getItemCount(): Int {
        return 1
    }

}