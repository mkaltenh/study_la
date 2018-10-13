package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * FragmentKlasse: BaseFragment
 * Implementierung der FragmentKlasse, hinzufügen der Butterknife.bind Methode
 * Verwendung @Fragment: CanteenFragment, RoomfinderFragment
 * @Activity: MainActivity
 * @Author: Frederic Schuetze
 *******************************************************************************************/

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;

public class BaseFragment extends Fragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
