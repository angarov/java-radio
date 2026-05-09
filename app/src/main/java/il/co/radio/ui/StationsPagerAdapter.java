package il.co.radio.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class StationsPagerAdapter extends FragmentStateAdapter {

    private static final String URL_NATIONAL = "https://digital.100fm.co.il/allfm/stations/api/national";
    private static final String URL_LOCAL = "https://digital.100fm.co.il/allfm/stations/api/local";

    public StationsPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return StationListFragment.newInstance(position == 0 ? URL_NATIONAL : URL_LOCAL);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
