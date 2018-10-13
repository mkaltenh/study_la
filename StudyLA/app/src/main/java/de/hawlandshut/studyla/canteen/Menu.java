package de.hawlandshut.studyla.canteen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Menu {

    public final Date day;
    public final Group group;
    public final String name;
    public final List<Tag> tags;
    public final float priceStudent; // ct
    public final float priceStaff; // ct
    public final float priceGuest; // ct

    public Menu(Date day, Group group, String name, List<Tag> tags,
                float priceStudent, float priceStaff, float priceGuest) {
        this.day = day;
        this.group = group;
        this.name = name;
        this.tags = tags;
        this.priceStudent = priceStudent;
        this.priceStaff = priceStaff;
        this.priceGuest = priceGuest;
    }

    @Override
    public String toString() {
        return name;
    }

    protected static String parseName(String name) {
        String pattern = "(\\([^\\(\\)]+\\))|(\\*\\S+)";
        name = name.replaceAll(pattern, "");
        name = name.trim();
        return name;
    }

    protected static Group parseGroupfromString(final String group) {
        if (group.startsWith("Suppe"))
            return Group.Soup;
        else if (group.startsWith("HG"))
            return Group.MainDish;
        else if (group.startsWith("B"))
            return Group.SideDish;
        else
            return Group.Dessert;
    }

    protected static List<Tag> parseTagsfromString(final String tags) {
        final String[] tagArr = tags.split(",");
        List<Tag> tagList = new ArrayList<>();
        List<String> tagss = Arrays.asList("G", "S", "R", "L", "W", "F", "A"
                , "V", "VG", "MV", "B", "AG");
        for (String tag : tagArr)
            if (tagss.indexOf(tag) >= 0)
                tagList.add(Tag.values()[tagss.indexOf(tag)]);
        return tagList;
    }

    public enum Group {
        Soup, // Suppe
        MainDish, // Hauptgericht
        SideDish, // Beilage
        Dessert // Nachtisch
    }

    public enum Tag {
        Poultry, // Geflügel
        Pork, // Schwein
        Beef, // Rind
        Lamb, // Lamm
        Venison, // Wild
        Fish, // Fisch
        Alcohol, // Alkohol
        Vegetarian, // Vegetarisch
        Vegan, // Vegan
        Vital, // MensaVital
        Bio, // DE-ÖKO-006 mit ausschließlich biologisch erzeugten Rohstoffen
        Special // Aktionsgericht
    }
}
