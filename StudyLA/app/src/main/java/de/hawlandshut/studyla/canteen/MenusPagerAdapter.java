package de.hawlandshut.studyla.canteen;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MenusPagerAdapter extends FragmentStatePagerAdapter {
    private MenuList mMenuList;
    private List<Date> mDays;

    public MenusPagerAdapter(FragmentManager fm, MenuList menus) {
        super(fm);
        mMenuList = menus;
        mDays = mMenuList.listDays();
    }

    @Override
    public Fragment getItem(int position) {
        return MenusFragment.newInstance(mMenuList, mDays.get(position));
    }

    @Override
    public int getCount() {
        return mDays.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Date date = mDays.get(position);
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL);
        return dateFormat.format(date).toUpperCase(Locale.GERMAN);
    }

    public int getCurrentDay(){
        Calendar c = Calendar.getInstance();
        Date currentDate = c.getTime();
        return mMenuList.getPositionOfDay(currentDate);
    }
}
