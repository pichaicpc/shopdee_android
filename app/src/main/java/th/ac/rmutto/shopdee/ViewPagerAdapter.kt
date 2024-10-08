package th.ac.rmutto.shopdee

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2 // Number of tabs (fragments)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryOrderFragment() // First tab (fragment)
            else -> ChatListFragment() // Second tab (fragment)
        }
    }
}