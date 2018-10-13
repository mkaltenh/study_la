package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * FragmentKlasse: CanteenFragment
 * Fragt www.stwno.de nach aktuellen Mensaplänen ab und zeigt diese in Fragments welche
 * durch einen ViewPager verknüpft werden an. Downloadet die Pläne im .csv Format, parsed
 * diese und gibnt sie an den mPagerAdapter zur Darstellung.
 * @Activity: MainActivity
 * @Author: Max Kaltenhauser, Frederic Schuetze
 *******************************************************************************************/

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.canteen.MenuList;
import de.hawlandshut.studyla.canteen.MenusPagerAdapter;

public class CanteenFragment extends BaseFragment {

    @BindView(R.id.pager)
    ViewPager mPager;

    @BindView(R.id.pager_title_strip)
    PagerTitleStrip mTitleStrip;

    private MenusPagerAdapter mPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_canteen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new AsyncTask<Void, Void, MenuList>() {
            @Override
            protected MenuList doInBackground(Void... voids) {
                try {
                    Calendar calendar = Calendar.getInstance();
                    int kw = calendar.get(Calendar.WEEK_OF_YEAR)-1;
                    MenuList allMenus = new MenuList();
                    MenuList weekMenus;

                    do {
                        try {
                            final String urlStr = String.format(Locale.GERMAN,
                                    "http://www.stwno.de/infomax/daten-extern/csv/HS-LA/%d.csv", kw);
                            final URL url = new URL(urlStr);
                            final InputStream is = url.openStream();
                            weekMenus = MenuList.fromCsv(is);
                            allMenus.addAll(weekMenus);
                        } catch (FileNotFoundException e) {
                            break;
                        }
                        kw++;
                    } while (weekMenus.size() > 0);

                    return allMenus;
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                }
                return new MenuList();
            }

            @Override
            protected void onPostExecute(MenuList menus) {
                mPagerAdapter = new MenusPagerAdapter(getChildFragmentManager(), menus);
                mPager.setAdapter(mPagerAdapter);
                mPager.setCurrentItem(mPagerAdapter.getCurrentDay());
            }
        }.execute();
    }
}
