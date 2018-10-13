package de.hawlandshut.studyla.home;

/**
* Enum: ModelObject
* Listet die einzelnen Layouts welche vom CustomPagerAdapter verwendet werden
* @Fragment: HomeFragment
 */

import de.hawlandshut.studyla.R;

public enum ModelObject {

    RED(R.string.red, R.layout.viewpager_start),
    BLUE(R.string.blue, R.layout.viewpager_video);

    private int mTitleResId;
    private int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}