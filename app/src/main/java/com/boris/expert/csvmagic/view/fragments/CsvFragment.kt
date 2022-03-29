package com.boris.expert.csvmagic.view.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.boris.expert.csvmagic.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class CsvFragment : Fragment() {

    private lateinit var bottomNavigation: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_csv, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View) {
        bottomNavigation = view.findViewById(R.id.bottom_navigation1)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_scanner -> {
                    childFragmentManager.beginTransaction()
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container1, ScannerFragment(), "scanner")
                        .addToBackStack("scanner")
                        .commit()
                }
                R.id.bottom_tables -> {
                    childFragmentManager.beginTransaction()
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container1, ScanFragment(), "tables")
                        .addToBackStack("tables")
                        .commit()
                }
                else -> {

                }
            }

            true
        }

        childFragmentManager.beginTransaction()
//            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .add(
                R.id.fragment_container1,
                ScannerFragment(),
                "scanner"
            )
                .addToBackStack("scanner")
                .commit()
    }

}