package com.boris.expert.csvmagic.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout


class TablesFragment : Fragment() {


    var tabLayout: TabLayout? = null
    var pager2: ViewPager2? = null
    var viewPagerAdapter: ViewPagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_tables, container, false)

        initViews(v)
        setUpViewPager()
        return v
    }


    private fun initViews(view: View){
        tabLayout = view.findViewById(R.id.tab_layout)
        pager2 = view.findViewById(R.id.viewpager)
    }

    private fun setUpViewPager(){
        val fm: FragmentManager = childFragmentManager
        viewPagerAdapter = ViewPagerAdapter(fm, lifecycle)
        pager2!!.adapter = viewPagerAdapter

        tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.tables)))
//        tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.create)))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                pager2!!.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        pager2!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout!!.selectTab(tabLayout!!.getTabAt(position))
            }
        })

    }
}