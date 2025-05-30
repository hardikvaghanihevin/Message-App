package com.hardik.messageapp.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hardik.messageapp.presentation.ui.fragment.BlockMessageFragment
import com.hardik.messageapp.presentation.ui.fragment.BlockNumberFragment

class ViewPagerBlockAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> BlockNumberFragment()
            1 -> BlockMessageFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
        fragmentMap[position] = fragment
        return fragment
    }

    fun getFragment(position: Int): Fragment? = fragmentMap[position]
}
