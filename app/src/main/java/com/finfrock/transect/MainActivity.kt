package com.finfrock.transect

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.ActiveTransect
import com.finfrock.transect.view.InstructionsPageFragment
import com.finfrock.transect.view.StartPageFragment
import com.finfrock.transect.view.SummaryPageFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var database: AppDatabase
    lateinit var dataSource: DataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        dataSource = ViewModelProvider(viewModelStore, DataSource.FACTORY(database))
            .get(DataSource::class.java)
        setContentView(R.layout.main_activity)

        val viewPager: ViewPager2 = findViewById(R.id.pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)

        viewPager.adapter = ScreenSlidePagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Start"
                1 -> "Summary"
                2 -> "Instructions"
                else -> "None"
            }
        }.attach()

        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            Log.i("lancewf", "clicked")
        }

        dataSource.resumeTransect{ hasRunningTransect ->
            if ( hasRunningTransect ) {
                resumeRunningTransectActivity()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataSource.resumeTransect{ hasRunningTransect ->
           if ( hasRunningTransect ) {
               resumeRunningTransectActivity()
           }
        }
    }
    private fun resumeRunningTransectActivity() {
        startActivity(Intent(this, RunningTransectActivity::class.java))
    }
    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> StartPageFragment()
                1 -> SummaryPageFragment()
                2 -> InstructionsPageFragment()
                else -> SummaryPageFragment()
            }
        }
    }
}

