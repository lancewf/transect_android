package com.finfrock.transect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.network.TransectApiService
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
    @Inject
    lateinit var transectApiService: TransectApiService
    lateinit var dataSource: DataSource
    private lateinit var toolBar: MaterialToolbar

    override fun onResume() {
        super.onResume()
        dataSource.resumeTransect{ hasRunningTransect ->
            if ( hasRunningTransect ) {
                resumeRunningTransectActivity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        dataSource = ViewModelProvider(viewModelStore, DataSource.FACTORY(database, transectApiService))
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

        toolBar = findViewById(R.id.topAppBar)

        toolBar.setNavigationOnClickListener {
            Log.i("lancewf", "clicked")
        }

        toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.unsaved -> {
                    val intent = Intent(this, SavingProgressActivity::class.java)

                    this.startActivity(intent)
                    true
                }
                else -> false
            }
        }

        dataSource.resumeTransect{ hasRunningTransect ->
            if ( hasRunningTransect ) {
                resumeRunningTransectActivity()
            }
        }
        dataSource.hasUnsavedTransects().observe(this){ hasUnsaved ->
            toolBar.menu.getItem(0).isVisible = hasUnsaved
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

