package de.hawlandshut.studyla.roomfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.StudyLa;
import de.hawlandshut.studyla.fragments.RoomfinderFragment;
import de.hawlandshut.studyla.roomfinder.model.Room;
import de.hawlandshut.studyla.roomfinder.model.RoomDatabase;

/**
 * Activity für die Anzeige einer Liste von Räumen und deren Suche.
 *
 * @author Frederic Schuetze, Max Kaltenhauser
 *         Created: 06.04.2016.
 */
public class SearchActivity extends AppCompatActivity implements TextWatcher {

    /**
     * Adapter für Recyclerview.
     */
    private SearchAdapter mSearchAdapter;

    /**
     * Raumliste.
     */
    private RecyclerView mResultsView;

    /**
     * View zur Anzeige wenn kein Ergebniss.
     */
    private View mEmptyView;

    /**
     * Ladeanzeige.
     */
    private ProgressBar mSpinner;

    /**
     * Sucheingabe.
     */
    private EditText mSearchEditText;

    /**
     * Background Executor.
     */
    private ListeningExecutorService mBackgroundExecutor;

    /**
     * UI Executor.
     */
    private Executor mUiExecutor;

    /**
     * Suchergebniss.
     */
    private ListenableFuture<List<Room>> mSearchFuture;

    /**
     * Bool ob geschucht werden kann.
     */
    private boolean mSearchViewReady;

    /**
     * Bool ob Gui geladen ist.
     */
    private boolean mLoaded = false;

    /**
     * Liste Aller Räume.
     */
    private List<Room> mAllRooms;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mResultsView = (RecyclerView) findViewById(R.id.recycler);
        mEmptyView = findViewById(android.R.id.empty);
        mSpinner = (ProgressBar) findViewById(R.id.progress_bar);

        mSearchAdapter = new SearchAdapter(this);
        mResultsView.setLayoutManager(new LinearLayoutManager(this));
        mResultsView.setItemAnimator(new DefaultItemAnimator());
        mResultsView.setAdapter(mSearchAdapter);

        mBackgroundExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        mUiExecutor = new MainThreadExecutor();

        ListenableFuture load = mBackgroundExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mAllRooms = new ArrayList<>(getDatabase().getRoomList());
            }
        });

        Futures.addCallback(load, new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                mSearchAdapter.setResult("", mAllRooms);
                mResultsView.setVisibility(View.VISIBLE);
                mSpinner.setVisibility(View.GONE);
                mLoaded = true;
                setSearchTextListener();
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, mUiExecutor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItemCompat.expandActionView(searchItem);

        View actionView = MenuItemCompat.getActionView(searchItem);
        mSearchEditText = (EditText) actionView.findViewById(R.id.searchview_text);
        mSearchEditText.setHint(R.string.search_hint);

        mSearchViewReady = true;
        setSearchTextListener();

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                finish();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        mBackgroundExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mResultsView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mSpinner.setVisibility(View.VISIBLE);
        search(s.toString());
    }

    /**
     * Starten der Suche.
     * @param query Suchbegriff.
     */
    private void search(final String query) {
        if (mSearchFuture != null)
            mSearchFuture.cancel(true);

        mSearchFuture = mBackgroundExecutor.submit(new Callable<List<Room>>() {
            @Override
            public List<Room> call() throws Exception {
                RoomDatabase db = getDatabase();
                return db.search(query);
            }
        });

        Futures.addCallback(mSearchFuture, new FutureCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> result) {
                mSearchAdapter.setResult(query, result);
                mSpinner.setVisibility(View.GONE);
                if (result.isEmpty())
                    mEmptyView.setVisibility(View.VISIBLE);
                else
                    mResultsView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, mUiExecutor);

    }

    /**
     * Initialisiert Suchfeld.
     */
    private void setSearchTextListener() {
        if (!mLoaded || !mSearchViewReady) {
            return;
        }

        final InputMethodManager inputManager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        mSearchEditText.requestFocus();
        inputManager.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Adapter für Recyclerview.
     */
    private class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

        /**
         * Alle zu anzeigenden Räume.
         */
        private List<Room> mRoomItems = new ArrayList<>();

        /**
         * Aktueller Suchbegriff.
         */
        private String mQuery = "";

        /**
         * Activity.
         */
        private final SearchActivity mParent;

        /**
         * Ctor.
         * @param rootActivity Activity des Recyclerviews
         */
        public SearchAdapter(SearchActivity rootActivity) {
            mParent = rootActivity;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_room, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Room room = mRoomItems.get(position);
            setTextWithColor(holder.mTitle, room.getName(), mQuery,
                    ContextCompat.getColor(SearchActivity.this, R.color.red));
            setTextWithColor(holder.mDescription, room.getDescriptionLocalized(), mQuery,
                    ContextCompat.getColor(SearchActivity.this, R.color.red));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RoomfinderFragment.KEY_MARKED_ROOM, room);
                    mParent.setResult(Activity.RESULT_OK, resultIntent);
                    mParent.finish();
                }
            });

            holder.mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    room.setFavorite(!room.isFavorite());
                    getDatabase().setFavorite(room, room.isFavorite());

                    Drawable favIcon = ((ImageButton) v).getDrawable();
                    int colorOn = ContextCompat.getColor(SearchActivity.this, R.color.colorPrimary);
                    int colorOff = ContextCompat.getColor(SearchActivity.this, R.color.darkgrey);
                    favIcon.mutate().setColorFilter(room.isFavorite() ? colorOn : colorOff
                            , PorterDuff.Mode.SRC_ATOP);
                }
            });

            int colorOn = ContextCompat.getColor(SearchActivity.this, R.color.colorPrimary);
            int colorOff = ContextCompat.getColor(SearchActivity.this, R.color.darkgrey);
            Drawable favIcon = holder.mFavoriteButton.getDrawable();
            favIcon.mutate().setColorFilter(mRoomItems.get(position).isFavorite() ? colorOn : colorOff
                    , PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public int getItemCount() {
            return mRoomItems.size();
        }

        /**
         * Setzt eine neue Raumliste.
         * @param query Suchbegriff
         * @param result Ergebnissliste
         */
        public void setResult(String query, List<Room> result) {
            mRoomItems = result;
            mQuery = query;
            notifyDataSetChanged();
        }

        /**
         * Färbt einen Begriff innerhalb eines Textes in einer bestimmten Farbe.
         * @param view TextView
         * @param fulltext Ganzer Text
         * @param subtext zu färbender Begriff
         * @param color Farbe
         */
        private void setTextWithColor(TextView view, String fulltext,
                                      String subtext, int color) {
            if (fulltext == null)
                fulltext = "";

            // assign text to text view
            view.setText(fulltext, TextView.BufferType.SPANNABLE);

            if (subtext.length() == 0)
                return;

            // search for the substring
            int i = fulltext.toLowerCase(Locale.GERMAN).indexOf(
                    subtext.toLowerCase(Locale.GERMAN));
            if (i == -1)
                return;

            // substring was found, set the color
            Spannable str = (Spannable) view.getText();
            str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        /**
         * ViewHolder für Adapter.
         */
        protected class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTitle, mDescription;
            public ImageButton mFavoriteButton;
            public View mView;

            public ViewHolder(View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.item_title);
                mDescription = (TextView) itemView.findViewById(R.id.item_description);
                mView = itemView.findViewById(R.id.item_view);
                mFavoriteButton = (ImageButton) itemView.findViewById(R.id.item_button);
            }
        }
    }

    /**
     * Executor zum ausführen auf dem Main-Thread.
     */
    static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable r) {
            handler.post(r);
        }
    }

    private RoomDatabase getDatabase() {
        return ((StudyLa)getApplication()).getRoomDatabase();
    }
}
