package horvatApps.ImageFind.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import horvatApps.ImageFind.ui.InstructionFragments.InstructionFragment1;
import horvatApps.ImageFind.ui.InstructionFragments.InstructionFragment2;

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
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return num_pages;
    }


}
