package com.hardik.messageapp.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hardik.messageapp.presentation.ui.fragment.MessageFragment
import com.hardik.messageapp.presentation.ui.fragment.PrivateFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val fragments = mutableListOf<Fragment>()
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        val fragment =  when (position) {
            0 -> MessageFragment()
            1 -> PrivateFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
        fragments.add(fragment)
        return fragment
    }

    fun getFragment(position: Int): Fragment? {
        return fragments.getOrNull(position)
    }
}
