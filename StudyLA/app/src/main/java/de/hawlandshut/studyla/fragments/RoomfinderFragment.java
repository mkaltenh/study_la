package de.hawlandshut.studyla.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qozix.tileview.TileView;
import com.qozix.tileview.graphics.BitmapProvider;
import com.qozix.tileview.markers.MarkerLayout;
import com.qozix.tileview.tiles.Tile;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hawlandshut.studyla.MainActivity;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.StudyLa;
import de.hawlandshut.studyla.fragments.BaseFragment;
import de.hawlandshut.studyla.roomfinder.SearchActivity;
import de.hawlandshut.studyla.roomfinder.model.Position;
import de.hawlandshut.studyla.roomfinder.model.Room;
import de.hawlandshut.studyla.roomfinder.model.RoomDatabase;

/**
 * Haupt-Activity mit der Anzeige des Gebäudeplans.
 *
 * @author Frederic Schuetze, Max Kaltenhauser
 *         Created: 01.04.2016.
 */
public class RoomfinderFragment extends BaseFragment implements View.OnClickListener {

    /**
     * Request-Code für OnActivityResult.
     */
    public static final int REQUEST_CODE_SEARCH = 1;

    /**
     * Bundle-Key für markierten Raum.
     */
    public static final String KEY_MARKED_ROOM = "MARKED_ROOM";

    /**
     * Breite und Höhe des Gebäudeplans.
     */
    private static final int MAP_WIDTH = 4000, MAP_HEIGHT = 6600; // px

    // region Fields
    /**
     * Aktuelles Stockwerk.
     */
    private int mCurrentFloor = 0;

    /**
     * Aktueller Marker.
     * Wenn kein Marker gesetzt: null.
     */
    private ImageView mCurrentMarker;

    /**
     * Liste der Favoriten-Marker.
     */
    private List<ImageView> mFavoriteMarkers = new ArrayList<>();

    DrawerLayout mDrawer;
    private Button[] mFloorButtons;
    private LinearLayout mFloorButtonLayout;
    private Handler mHandler = new Handler();
    private ImageButton mButtonFloorUp, mButtonFloorDown;

    private View mDetailView;
    private TextView mDetailTitle, mDetailDescription;

    private CardView mAppBar;

    private TileView mTileView;

    private FrameLayout mMapContainer;

    private Runnable mHideFloorButtons = new Runnable() {
        @Override
        public void run() {
            collapseFloorButtons();
        }
    };

    private SharedPreferences mPrefs;

    // endregion

    // region Android LifeCycle


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orientation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final boolean firstSync = mPrefs.getBoolean("FIRST_SYNC", false);

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    getDatabase().sync();
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(@Nullable Exception e) {
                if (e == null) {
                    if (!firstSync) {
                        mPrefs.edit().putBoolean("FIRST_SYNC", true).apply();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        }.execute();

        // Toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);

        initializeViews(view);
        initFloorButtons();
        setFloor(0); // Stockwerk beim Start auf Erdgeschoss stellen
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SEARCH);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setFavoriteMarkers(getDatabase().getFavoritesForFloor(mCurrentFloor));
        if (requestCode == REQUEST_CODE_SEARCH && resultCode == RESULT_OK) {
            Room room = (Room) data.getSerializableExtra(KEY_MARKED_ROOM);
            markRoom(room);
        }
    }
    */

    @Override
    public void onDestroy() {
        if (mTileView != null)
            mTileView.destroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (mTileView != null)
            mTileView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mTileView != null)
            mTileView.resume();
        super.onResume();
    }

    // endregion

    /**
     * Views vorbereiten.
     */
    private void initializeViews(View view) {
        mAppBar = (CardView) view.findViewById(R.id.appbar);
        mMapContainer = (FrameLayout) view.findViewById(R.id.map_container);
        mFloorButtonLayout = (LinearLayout) view.findViewById(R.id.map_floor_button_layout);
        mButtonFloorUp = (ImageButton) view.findViewById(R.id.map_button_floor_up);
        mButtonFloorDown = (ImageButton) view.findViewById(R.id.map_button_floor_down);
        ImageButton buttonZoomIn = (ImageButton) view.findViewById(R.id.map_button_zoom_in);
        ImageButton buttonZoomOut = (ImageButton) view.findViewById(R.id.map_button_zoom_out);

        mAppBar.setOnClickListener(this);

        mButtonFloorUp.setOnClickListener(this);
        mButtonFloorDown.setOnClickListener(this);
        if (buttonZoomIn != null)
            buttonZoomIn.setOnClickListener(this);
        if (buttonZoomOut != null)
            buttonZoomOut.setOnClickListener(this);

        mDetailView = view.findViewById(R.id.map_room_details);
        mDetailTitle = (TextView) view.findViewById(R.id.room_detail_name);
        mDetailDescription = (TextView) view.findViewById(R.id.room_detail_description);
    }

    // region Marker Functions

    /**
     * Marker an eine Position setzen.
     * Es kann immer nur ein Marker gesetzt sein.
     *
     * @param position Marker-Position
     */
    private synchronized void setMarker(final Position position) {
        if (mCurrentMarker != null)
            mTileView.removeMarker(mCurrentMarker);

        mCurrentMarker = new ImageView(getActivity());
        mCurrentMarker.setImageResource(R.drawable.marker);
        mTileView.addMarker(mCurrentMarker, position.x, position.y, -0.5f, -1.0f);
    }

    /**
     * Liest die Position eines Raumes und setzt einen Marker.
     *
     * @param room zu markierender Raum
     */
    private synchronized void setMarker(final Room room) {
        Position position = new Position(room.getPositionX() * 2, room.getPositionY() * 2);
        setMarker(position);
    }

    /**
     * Setzt eine Liste von Favoriten-Markern
     *
     * @param roomList Favoriten Liste
     */
    public synchronized void setFavoriteMarkers(List<Room> roomList) {
        for (ImageView v : mFavoriteMarkers)
            mTileView.removeMarker(v);
        mFavoriteMarkers.clear();

        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);

            ImageView marker = new ImageView(getActivity());
            marker.setImageResource(R.drawable.marker_star);
            marker.setTag(r);
            mTileView.addMarker(marker, r.getPositionX() * 2, r.getPositionY() * 2, -0.5f, -0.5f);
            mFavoriteMarkers.add(marker);
        }
    }

    /**
     * Markiert einen Raum und stellt das stockwerk das Raumes ein und zoomt zum Marker
     * und zeigt die Beschreibung des Raumes an.
     *
     * @param room zu markiernder Raum
     */
    public void markRoom(Room room) {
        clearSelection();

        setFloor(room.getFloor());

        setMarker(room);
        frameTo(room);

        mDetailView.setVisibility(View.VISIBLE);
        mDetailTitle.setText(room.getName());
        mDetailDescription.setText(room.getDescriptionLocalized());
    }

    /**
     * Löscht den Marker und verbirgt die Anzeige der Bsschreibung.
     */
    private void clearSelection() {
        if (mCurrentMarker != null)
            mTileView.removeMarker(mCurrentMarker);
        mDetailView.setVisibility(View.GONE);
    }

    /**
     * Zoomt zu einem Bestimmten Raum.
     *
     * @param room Raum
     */
    private void frameTo(final Room room) {
        mTileView.post(new Runnable() {
            @Override
            public void run() {
                mTileView.setScale(1f);
                mTileView.scrollToAndCenter(room.getPositionX() * 2, room.getPositionY() * 2);
            }
        });
    }

    // endregion

    // region Floor Functions

    /**
     * Initialisiert die Buttons zur änderung der Stockwerke.
     */
    private void initFloorButtons() {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        mFloorButtons = new Button[]{
                (Button) inflater.inflate(R.layout.view_floor_button, mFloorButtonLayout, false),
                (Button) inflater.inflate(R.layout.view_floor_button, mFloorButtonLayout, false),
                (Button) inflater.inflate(R.layout.view_floor_button, mFloorButtonLayout, false)
        };
        mFloorButtons[0].setText(R.string.eg);
        mFloorButtons[1].setText(R.string.og1);
        mFloorButtons[2].setText(R.string.og2);

        for (int i = mFloorButtons.length - 1; i >= 0; --i)
            mFloorButtonLayout.addView(mFloorButtons[i], 5 - i);
    }

    /**
     * Wechselt das Stockwerk.
     *
     * @param floor Stockwerk
     */
    private void setFloor(int floor) {
        if (mCurrentFloor == floor && mTileView != null) {
            collapseFloorButtons();
            return;
        }

        float scale = 0.125f;
        int xPos = 0, yPos = 0;
        mCurrentFloor = floor;

        if (mTileView != null) {
            scale = mTileView.getScale();
            xPos = mTileView.getScrollX();
            yPos = mTileView.getScrollY();

            final TileView oltTileView = mTileView;
            ObjectAnimator animator = ObjectAnimator.ofFloat(oltTileView, "alpha", 0f)
                    .setDuration(250);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    oltTileView.setVisibility(View.GONE);
                    oltTileView.destroy();
                    oltTileView.removeView(mTileView);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        }

        mTileView = new TileView(getActivity());

        mMapContainer.addView(mTileView);

        mTileView.setBitmapProvider(new RoomPlanProvider());
        mTileView.setTransitionsEnabled(true);

        mTileView.setSize(MAP_WIDTH, MAP_HEIGHT);

        String path = String.format(Locale.US, "tiles/map_%d/", floor);

        mTileView.addDetailLevel(1.000f, path + "1000/%d_%d.jpg");
        mTileView.addDetailLevel(0.500f, path + "500/%d_%d.jpg");
        mTileView.addDetailLevel(0.250f, path + "250/%d_%d.jpg");
        mTileView.addDetailLevel(0.125f, path + "125/%d_%d.jpg");

        final ImageView backgroundImage = new ImageView(getActivity());
        Picasso.with(getActivity())
                .load("file:///android_asset/" + path + "map_s.jpg")
                .noPlaceholder()
                .into(backgroundImage);
        mTileView.addView(backgroundImage, 0);


        mTileView.getMarkerLayout().setMarkerTapListener(markerTapListener);
        mTileView.setShouldScaleToFit(true);
        mTileView.setScaleLimits(0, 2);
        mTileView.setScale(scale);
        mTileView.scrollTo(xPos, yPos);

        setFavoriteMarkers(getDatabase().getFavoritesForFloor(floor));

        mButtonFloorDown.setEnabled(mCurrentFloor != 0);
        mButtonFloorUp.setEnabled(mCurrentFloor != mFloorButtons.length - 1);

        clearSelection();
        collapseFloorButtons();
    }

    /**
     * Schließt die erweiterte Auswahl der Stockwerke.
     */
    private void collapseFloorButtons() {
        mHandler.removeCallbacks(mHideFloorButtons);

        for (int i = 0; i < mFloorButtons.length; ++i) {
            if (i == mCurrentFloor) {
                mFloorButtons[i].setVisibility(View.VISIBLE);
                mFloorButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expandFloorButtons();
                    }
                });
            } else {
                mFloorButtons[i].setVisibility(View.GONE);
            }
        }
    }

    /**
     * Öffnet die erweiterte Auswahl der Stockwerke.
     */
    private void expandFloorButtons() {
        for (int i = 0; i < mFloorButtons.length; ++i) {
            final int finalI = i;
            mFloorButtons[i].setVisibility(View.VISIBLE);
            mFloorButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFloor(finalI);
                }
            });
        }

        mHandler.postDelayed(mHideFloorButtons, 5000); // automatically collapse until 5 sec
    }

    /**
     * Tab Listener der darauf reagiert wenn auf einen Favoriten-Marker getippt wird.
     * Der Raum wird daraf Markiert.
     */
    private MarkerLayout.MarkerTapListener markerTapListener = new MarkerLayout.MarkerTapListener() {

        @Override
        public void onMarkerTap(View view, int x, int y) {
            Room tabedRoom = (Room) view.getTag();
            if (tabedRoom != null)
                markRoom(tabedRoom);
        }
    };

    // endregion

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.map_button_zoom_in:
                mTileView.smoothScaleFromCenter(mTileView.getScale() * 2);
                return;
            case R.id.map_button_zoom_out:
                mTileView.smoothScaleFromCenter(mTileView.getScale() / 2);
                return;
            case R.id.map_button_floor_up:
                setFloor(mCurrentFloor + 1);
                break;
            case R.id.map_button_floor_down:
                setFloor(mCurrentFloor - 1);
                break;
            case  R.id.appbar:
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SEARCH);
                break;
        }
    }

    /**
     * Bitmap-Provider zum Laden der Tiles des Gebäudeplans.
     */
    private static class RoomPlanProvider implements BitmapProvider {

        @Override
        public Bitmap getBitmap(Tile tile, Context context) {
            Object data = tile.getData();
            if (data instanceof String) {
                String unformattedFileName = (String) tile.getData();
                String formattedFileName = String.format(unformattedFileName, tile.getColumn(), tile.getRow());
                try {
                    return Picasso.with(context).load("file:///android_asset/" + formattedFileName)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).get();
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
            return null;
        }
    }

    private RoomDatabase getDatabase() {
        return ((StudyLa)getActivity().getApplication()).getRoomDatabase();
    }

    public void refreshFavorites() {
        setFavoriteMarkers(getDatabase().getFavoritesForFloor(mCurrentFloor));
    }
}
