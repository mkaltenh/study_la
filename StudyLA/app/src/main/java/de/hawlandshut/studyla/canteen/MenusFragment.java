package de.hawlandshut.studyla.canteen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.R;

public class MenusFragment extends Fragment {
    private Date mDay;
    private MenuList mMenus;

    @BindView(R.id.soup_layout)
    LinearLayout mSoupsLayout;

    @BindView(R.id.main_dish_layout)
    LinearLayout mMainDishLayout;

    @BindView(R.id.side_dish_layout)
    LinearLayout mSideDishLayout;

    @BindView(R.id.desert_layout)
    LinearLayout mDesertLayout;

    public static MenusFragment newInstance(MenuList menus, Date day) {
        MenusFragment fragment = new MenusFragment();
        fragment.mMenus = menus.menusForDay(day);
        fragment.mDay = day;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menus_day, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mMenus == null || mMenus.size() == 0)
            return;

        inflateMenus(mMenus.menusForGroup(Menu.Group.Soup), mSoupsLayout, savedInstanceState);
        inflateMenus(mMenus.menusForGroup(Menu.Group.MainDish), mMainDishLayout, savedInstanceState);
        inflateMenus(mMenus.menusForGroup(Menu.Group.SideDish), mSideDishLayout, savedInstanceState);
        inflateMenus(mMenus.menusForGroup(Menu.Group.Dessert), mDesertLayout, savedInstanceState);
    }

    private void inflateMenus(final MenuList menus, final LinearLayout layout, Bundle bundle) {
        for (Menu menu : menus) {
            View v = getLayoutInflater(bundle)
                    .inflate(R.layout.item_menu, null);
            ((TextView) v.findViewById(R.id.menu_name)).setText(menu.name);
            ((TextView) v.findViewById(R.id.menu_price)).setText(
                    String.format(Locale.GERMAN, "€ %.2f", menu.priceStudent)
            );

            layout.addView(v);
        }
    }
}
