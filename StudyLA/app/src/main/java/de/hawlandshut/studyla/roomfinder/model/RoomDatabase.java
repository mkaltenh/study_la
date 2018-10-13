package de.hawlandshut.studyla.roomfinder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Klasse zum Ansprechen der lokalen Datenbank und synchronisieren mit der Server-Datenbank.
 *
 * @author Frederic Schuetze
 *         Created: 06.04.2016.
 */
public class RoomDatabase {

    /**
     * Url der Azure-Mobile-App als Schnittstelle zur Server-Datenbank.
     */
    private final static String URL_AZURE_MOBILE_APP = "https://hawla-roomfinder.azurewebsites.net";

    /**
     * Name der Tabelle der Räume.
     */
    private final static String TABLE_ROOM = "Room";

    /**
     * Name der Tabelle der Favoriten.
     */
    private final static String TABLE_FAVORITES = "Favorites";

    /**
     * Azure Mobile Service Client.
     */
    private MobileServiceClient mClient;

    /**
     * Synchronisierte Tabelle für Räume.
     */
    private MobileServiceSyncTable<Room> mRoomTable;

    /**
     * Lolaker SQLite-Datenbank Helper.
     */
    private SQLiteLocalStore mLocalStore;

    /**
     * Ctor.
     *
     * @param context Android-Context
     */
    public RoomDatabase(Context context) {

        try {
            mClient = new MobileServiceClient(
                    URL_AZURE_MOBILE_APP,
                    context);

            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            mRoomTable = mClient.getSyncTable(TABLE_ROOM, Room.class);

            initLocalStore().get();

        } catch (MalformedURLException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asynchrone Methode zur Definition der lokalen Datenbank.
     *
     * @return AsyncTask
     */
    private AsyncTask<Void, Void, Void> initLocalStore() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    mLocalStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    // Definition for Rooms Table
                    Map<String, ColumnDataType> roomTable = new HashMap<>();
                    roomTable.put("Id", ColumnDataType.String);
                    roomTable.put("Name", ColumnDataType.String);
                    roomTable.put("Description", ColumnDataType.String);
                    roomTable.put("DescriptionEnglish", ColumnDataType.String);
                    roomTable.put("Floor", ColumnDataType.Integer);
                    roomTable.put("PositionX", ColumnDataType.Integer);
                    roomTable.put("PositionY", ColumnDataType.Integer);
                    mLocalStore.defineTable(TABLE_ROOM, roomTable);

                    // Definition for Favorite Table
                    Map<String, ColumnDataType> favoriteTable = new HashMap<>();
                    favoriteTable.put("Id", ColumnDataType.String);
                    mLocalStore.defineTable(TABLE_FAVORITES, favoriteTable);

                    SimpleSyncHandler handler = new SimpleSyncHandler();
                    syncContext.initialize(mLocalStore, handler).get();

                } catch (final Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    /**
     * Führt einen AsyncTask ohne Parameter aus.
     *
     * @param task AsyncTask
     * @return AsyncTask
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    /**
     * Synchronisierung.
     * Aktualisiert die lokale Datenbank mit den Daten vom Server.
     *
     * @throws Exception
     */
    public void sync() throws Exception {
        // MobileServiceSyncContext syncContext = mClient.getSyncContext();
        // syncContext.push().get();
        mRoomTable.pull(null).get();
    }

    /**
     * Fügt einen Raum zu den Favoriten hinzu.
     *
     * @param room Raum
     */
    public void addFavorite(final Room room) {
        ContentValues v = new ContentValues();
        v.put("id", room.getId());
        mLocalStore.getWritableDatabase().insertWithOnConflict(TABLE_FAVORITES, null, v,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Löscht einen Raum von den Favoriten.
     *
     * @param room Raum
     */
    public void removeFavorite(final Room room) {
        mLocalStore.getWritableDatabase().delete(TABLE_FAVORITES, "id=?", new String[]{room.getId()});
    }

    /**
     * Setzt oder Löscht einen Favoriten.
     * Ist fav True wird ein Favorit hinzugefügt, bei False wird er entfernt.
     *
     * @param room Raum
     * @param fav  Entscheidungskriterium
     */
    public void setFavorite(final Room room, boolean fav) {
        if (fav)
            addFavorite(room);
        else
            removeFavorite(room);
    }

    /**
     * Gibt alle Räume der lokalen Datenbank zurück.
     *
     * @return Liste mit Räumen
     */
    public List<Room> getRoomList() {
        final String QUERY = "SELECT Room.*, CASE WHEN Favorites.Id IS NULL THEN 0 ELSE 1 END AS favorite " +
                "FROM Room " +
                "LEFT JOIN Favorites " +
                "ON Favorites.Id = Room.Id " +
                "ORDER BY name ASC";

        return queryRooms(QUERY);
    }

    /**
     * Gibt alle favoritisierten Raume für einen gegebenes Stockwert zurück.
     *
     * @param floor Stockwerk
     * @return favoritisierte Räume
     */
    public List<Room> getFavoritesForFloor(int floor) {
        final String QUERY = "SELECT * FROM Room r " +
                "INNER JOIN Favorites f " +
                "ON r.Id=f.Id " +
                "WHERE floor=" + Integer.toString(floor) + " " +
                "ORDER BY name ASC";

        return queryRooms(QUERY);
    }

    /**
     * Sucht Räume nach einem gegebenem Suchbegriff.
     *
     * @param query Suchbegriff
     * @return Liste gefundener Räume
     */
    public List<Room> search(String query) {
        boolean de = "de".equals(Locale.getDefault().getLanguage());

        final String QUERY = "SELECT Room.*, CASE WHEN Favorites.Id IS NULL THEN 0 ELSE 1 END AS favorite " +
                "FROM Room " +
                "LEFT JOIN Favorites " +
                "ON Favorites.Id = Room.Id " +
                "WHERE name LIKE '%" + query + "%' OR " +
                (de ? "description LIKE '%" : "descriptionenglish LIKE '%") +
                query + "%' " +
                "ORDER BY name ASC";


        return queryRooms(QUERY);
    }

    /**
     * Führt eine SQL-Select-Query auf der Datenbank aus und gibt Ergebniss als Raumliste zurück.
     *
     * @param query SQL-Query
     * @return Ergebniss
     */
    private synchronized List<Room> queryRooms(final String query) {
        ArrayList<Room> rooms = new ArrayList<>();

        Cursor c = mLocalStore.getReadableDatabase()
                .rawQuery(query, new String[]{});

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Room room = Room.fromCursor(c);
            if (c.getColumnIndex("favorite") >= 0)
                room.setFavorite(c.getInt(c.getColumnIndex("favorite")) > 0);
            rooms.add(room);
        }

        c.close();

        return rooms;
    }
}
