@file:Suppress("unused")

package com.example.app.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.app.fragments.FifthFragment
import com.example.app.fragments.FirstFragment
import com.example.app.fragments.SecondFragment
import com.example.app.fragments.SeventhFragment
import com.example.app.fragments.SixthFragment
import com.example.app.fragments.ThirdFragment

class SectionsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstFragment()
            1 -> SecondFragment()
            2 -> ThirdFragment()
            3 -> FifthFragment()
            4 -> SixthFragment()
            5 -> SeventhFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun getItemCount(): Int = 6
}