package de.hawlandshut.studyla.roomfinder.model;

import android.database.Cursor;

import java.io.Serializable;
import java.util.Locale;

/**
 * Klasse für die Eigenschaften eines Raumes.
 *
 * @author Frederic Schuetze
 *         Created: 03.04.2016.
 */
public class Room implements Serializable {

    /**
     * Id des Raumes für die Datenbank.
     */
    @com.google.gson.annotations.SerializedName("Id")
    private String mId;

    /**
     * Name des Raumes (z.B. 'HS104').
     */
    @com.google.gson.annotations.SerializedName("Name")
    private String mName = "";

    /**
     * Horizontale Position des Raumes.
     */
    @com.google.gson.annotations.SerializedName("PositionX")
    private int mPositionX;

    /**
     * Vertikale Position des Raumes.
     */
    @com.google.gson.annotations.SerializedName("PositionY")
    private int mPositionY;

    /**
     * Stockwerk des Raumes (0 = EG, 1 = OG1, ...).
     */
    @com.google.gson.annotations.SerializedName("Floor")
    private int mFloor;

    /**
     * Beschreibung des Raumes auf Deutsch (z.B. 'Mensa').
     */
    @com.google.gson.annotations.SerializedName("Description")
    private String mDescription = "";

    /**
     * Bechreibung des Raumes auf Englisch (z.B. 'Laboratory').
     */
    @com.google.gson.annotations.SerializedName("DescriptionEnglish")
    private String mDescriptionEnglish = "";

    /**
     * Boolean ob der Raum ein Favorit ist.
     */
    private transient boolean mIsFavorite = false;

    /**
     * Erstellt ein neues Objekt vom Typ Raum von einen gegebenen Cursor.
     * Dieser Cursor muss die Spalte 'id', 'name', 'floor', 'description',
     * 'descriptionenglish', 'positionx' und 'positiony' enthalten.
     *
     * @param c gegebene Cursor
     * @return der aus dem Cursor erstellte Raum
     */
    public static Room fromCursor(Cursor c) {
        Room room = new Room(c.getString(c.getColumnIndex("id")));
        room.mName = c.getString(c.getColumnIndex("name"));
        room.mFloor = c.getInt(c.getColumnIndex("floor"));
        room.mDescription = c.getString(c.getColumnIndex("description"));
        room.mDescriptionEnglish = c.getString(c.getColumnIndex("descriptionenglish"));
        room.mPositionX = c.getInt(c.getColumnIndex("positionx"));
        room.mPositionY = c.getInt(c.getColumnIndex("positiony"));
        return room;
    }

    /**
     * Ctor.
     *
     * @param id Id des Raumes
     */
    public Room(String id) {
        mId = id;
    }

    public String getName() {
        return mName == null ? "" : mName;
    }

    public String getDescription() {
        return mDescription == null ? "" : mDescription;
    }

    public int getFloor() {
        return mFloor;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }

    public String getId() {
        return mId;
    }

    public int getPositionX() {
        return mPositionX;
    }

    public int getPositionY() {
        return mPositionY;
    }

    public String getDescriptionEnglish() {
        return mDescriptionEnglish == null ? "" : mDescriptionEnglish;
    }

    /**
     * Gibt die Beschreibung eines Raumes in der Sprache zurück in der das Gerät eingestellt ist.
     *
     * @return Beschreibung des Raumes
     */
    public String getDescriptionLocalized() {
        if ("de".equals(Locale.getDefault().getLanguage()))
            return getDescription().length() == 0 ? getDescriptionEnglish() : getDescription();
        else
            return getDescriptionEnglish().length() == 0 ? getDescription() : getDescriptionEnglish();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        return mId != null ? mId.equals(room.mId) : room.mId == null;
    }

    @Override
    public int hashCode() {
        return mId != null ? mId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return mName;
    }
}
