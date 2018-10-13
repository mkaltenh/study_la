package de.hawlandshut.studyla;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hawlandshut.studyla.canteen.Menu;
import de.hawlandshut.studyla.canteen.MenuList;

public class CanteenUnitTest {

    @Test
    public void readWeekMenus() throws IOException, ParseException {
        URL url = new URL("http://www.stwno.de/infomax/daten-extern/csv/HS-LA/26.csv");
        InputStream is = url.openStream();
        MenuList menus = MenuList.fromCsv(is);
        System.out.println(menus);
    }

    @Test
    public void readDayMenus() throws IOException, ParseException {
        URL url = new URL("http://www.stwno.de/infomax/daten-extern/csv/HS-LA/26.csv");
        InputStream is = url.openStream();
        MenuList menus = MenuList.fromCsv(is);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DAY_OF_MONTH, 29);
        System.out.println(menus.menusForDay(cal.getTime()));
    }
}
