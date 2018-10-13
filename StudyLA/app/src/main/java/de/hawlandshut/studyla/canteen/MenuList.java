package de.hawlandshut.studyla.canteen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import static de.hawlandshut.studyla.canteen.Menu.parseGroupfromString;
import static de.hawlandshut.studyla.canteen.Menu.parseTagsfromString;

public class MenuList extends ArrayList<Menu> {

    public static MenuList fromCsv(InputStream stream) throws IOException, ParseException {
        CsvReader reader = new CsvReader(new InputStreamReader(stream, Charset.forName("ISO-8859-1")));
        MenuList menus = new MenuList();
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        reader.readNext();
        String[] line = reader.readNext();
        while (line != null) {
            try{
            menus.add(new Menu(
                    df.parse(line[0]),
                    parseGroupfromString(line[2]),
                    Menu.parseName(line[3]),
                    parseTagsfromString(line[4]),
                    Float.parseFloat(line[6].replace(',', '.')), //Aktuell Fehler in 5.csv Line 35 da .csv fehlerhaft
                    Float.parseFloat(line[7].replace(',', '.')),
                    Float.parseFloat(line[8].replace(',', '.'))
            ));}
            catch (NumberFormatException e) {
                e.printStackTrace(); //prints error
            }
            line = reader.readNext();
        }
        reader.close();
        return menus;
    }

    public MenuList menusForDay(Date day) {
        MenuList dayList = new MenuList();
        Calendar calDay = Calendar.getInstance();
        calDay.setTime(day);

        for (Menu menu : this) {
            Calendar calEntity = Calendar.getInstance();
            calEntity.setTime(menu.day);
            if (calDay.get(Calendar.YEAR) == calEntity.get(Calendar.YEAR) &&
                    calDay.get(Calendar.DAY_OF_YEAR) == calEntity.get(Calendar.DAY_OF_YEAR))
                dayList.add(menu);
        }

        return dayList;
    }

    public MenuList menusForGroup(Menu.Group group) {
        MenuList groupList = new MenuList();

        for (Menu menu : this)
            if (menu.group == group)
                groupList.add(menu);

        return groupList;
    }

    public List<Date> listDays() {
        List<Date> days = new ArrayList<>();
        for (Menu menu : this) {
            if (!days.contains(menu.day)) {
                days.add(menu.day);
            }
        }
        return days;
    }

    public int getPositionOfDay(Date day){
        List<Date> mDays = listDays();

        for (int position = 0; position<mDays.size(); position++){
                DateTime today = new DateTime(day);
                DateTime listTime = new DateTime(mDays.get(position));
                DateTimeComparator.getDateOnlyInstance().compare(mDays.get(position), day);

                if(listTime.withTimeAtStartOfDay().isEqual(today.withTimeAtStartOfDay())){
                    return position;
                }
                else if(listTime.withTimeAtStartOfDay().isAfter(today.withTimeAtStartOfDay())){
                    return position;
                }

            }
        return 0;
    }
}
