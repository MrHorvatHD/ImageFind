package horvatApps.ImageFind.ui;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import horvatApps.ImageFind.Adapters.InstructionsPagerAdapter;
import horvatApps.ImageFind.R;

public class InstructionsActivity extends FragmentActivity{

    private static final int NUM_PAGES = 5;


    /* The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 viewPager;


    //The pager adapter, which provides the pages to the view pager widget.
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        initUI();
    }

    // sets navbar and status bar color, instantiates Viewpager and its adapter, starts tabLayout
    public void initUI(){
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.view_pager2);
        pagerAdapter = new InstructionsPagerAdapter(this, NUM_PAGES);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

}
