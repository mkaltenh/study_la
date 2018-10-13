package de.hawlandshut.studyla.roomfinder.model;

import java.util.Locale;

/**
 * Immmutable-Klasse für einen Punkt auf dem Gebäudeplan.
 *
 * @author Frederic Schuetze
 *         Created: 13.04.2016.
 */
public class Position {

    /**
     * Absolute Position X für die Position in der Breite und
     * Y für die Position in der Höhe.
     * X Werte beginnen auf der Linken Seite und Y Werte beginnen von Oben.
     */
    public final int x, y;

    /**
     * Ctor.
     *
     * @param x horizontale Position
     * @param y vertikale Position
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "{%d,%d}", x, y);
    }
}
