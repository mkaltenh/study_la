package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * @Fragment: StudyFragment
 * Wird in der aktuellen Version noch nicht verwendet
 * @Activity: MainActivity
 * @Author: Max Kaltenhauser, Frederic Schuetze
 *******************************************************************************************/

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.hawlandshut.studyla.R;

public class StudyFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
