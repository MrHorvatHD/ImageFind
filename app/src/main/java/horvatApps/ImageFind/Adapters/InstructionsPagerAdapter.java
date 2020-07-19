package horvatApps.ImageFind.Adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import horvatApps.ImageFind.ui.InstructionFragments.InstructionFragment1;
import horvatApps.ImageFind.ui.InstructionFragments.InstructionFragment2;
import horvatApps.ImageFind.ui.InstructionFragments.InstructionFragment3;
import horvatApps.ImageFind.ui.InstructionFragments.InstructionFragment4;
import horvatApps.ImageFind.ui.InstructionFragments.InstructionFragment5;

public class InstructionsPagerAdapter extends FragmentStateAdapter {
    private int num_pages;

    public InstructionsPagerAdapter(FragmentActivity fa, int num_pages) {
        super(fa);
        this.num_pages = num_pages;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new InstructionFragment1();

            case 1:
                return new InstructionFragment2();

            case 2:
                return new InstructionFragment3();

            case 3:
                return new InstructionFragment4();

            case 4:
                return new InstructionFragment5();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return num_pages;
    }


}
