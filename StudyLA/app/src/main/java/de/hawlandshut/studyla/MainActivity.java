package de.hawlandshut.studyla;

/*******************************************************************************************
 * @Activity: MainActivity
 * Hauptklasse der HAW Landshut App, erstellt den NavigationDrawer, regelt das wechseln
 * zwischen den einzelnen FragmentKlassen und Implementiert onBackPressed. Stellt den Context
 * für alle Fragments dar, regelt zudem die Erstellung des SubMenus.
 * @Author: Max Kaltenhauser, Frederic Schütze
 *******************************************************************************************/

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;

import de.hawlandshut.studyla.fragments.CanteenFragment;
import de.hawlandshut.studyla.fragments.EventFragment;
import de.hawlandshut.studyla.fragments.HomeFragment;
import de.hawlandshut.studyla.fragments.ImpressumFragment;
import de.hawlandshut.studyla.fragments.NewsFragment;
import de.hawlandshut.studyla.fragments.TimetableFragment;
import de.hawlandshut.studyla.fragments.TransportFragment;
import de.hawlandshut.studyla.fragments.RoomfinderFragment;
import de.hawlandshut.studyla.fragments.WebViewFragment;
import de.hawlandshut.studyla.roomfinder.model.Room;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;

    boolean submenuOpen;
    ColorStateList tintList;
    private Fragment mCurrentFragment;
    static boolean checkFirstTime = true;
    protected OnBackPressedListener onBackPressedListener;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        tintList = navigationView.getItemIconTintList();

        if(checkFirstTime){
            Fragment newFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, newFragment).commit();
            checkFirstTime = false;
        }
    }

    //Handle Back Presses
    public interface OnBackPressedListener {
        void doBack();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    public void onBackPressed() {
        //When Submenu is open
        if(submenuOpen){
            navigationView.getHeaderView(0).setVisibility(View.VISIBLE);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer);
            navigationView.setItemIconTintList(tintList);
            submenuOpen = false;
        }
        else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);}
        //Can be modified individually for each Fragment
        else if (onBackPressedListener != null)
            onBackPressedListener.doBack();
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        onBackPressedListener = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    //Handle clicks on NavigationView Items
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment newFragment = null;
        Bundle bundle = new Bundle();

        switch(id){
            //Main Menu
            case R.id.nav_studium: newFragment = new NewsFragment(); break;
            case R.id.nav_mensa: setActionBarTitle("Mensa"); newFragment = new CanteenFragment(); break;
            case R.id.nav_orientierung: setActionBarTitle("Room Finder"); newFragment = new RoomfinderFragment(); break;
            case R.id.nav_transport:
                bundle.putString("DepArr" ,"arr");
                newFragment = new TransportFragment();
                newFragment.setArguments(bundle); break;
            case R.id.nav_veranstaltungen: newFragment = new EventFragment(); break;
            case R.id.nav_moodle: setActionBarTitle("Moodle");
                bundle.putString("URL" ,"https://moodle.haw-landshut.de/");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.nav_bibliothek: setActionBarTitle("Bibliothek");
                bundle.putString("URL" ,"https://bibaccess.fh-landshut.de/login?url=https://opac.haw-landshut.de/InfoGuideClient.flasis/start.do?Login=wofla");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.nav_einstellungen:  newFragment = new ImpressumFragment(); break;
            case R.id.nav_email: setActionBarTitle("Webmail");
                bundle.putString("URL" ,"https://webmail.haw-landshut.de/rc/index.php");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.nav_feedback:
                Intent feedbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:studyla@campus-company-landshut.de"));
                startActivity(feedbackIntent); break;
            case R.id.nav_links:openLinkMenu();
                return true;
            case R.id.nav_vorlesungsplan: newFragment = new TimetableFragment(); break;
            case R.id.nav_home: newFragment = new HomeFragment(); break;

            //Submenu
            case R.id.link_back: onBackPressed(); return true;
            case R.id.link_campusla: setActionBarTitle("Campus Landshut");
                bundle.putString("URL", "https://www.campuslandshut.de/");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL" ,"https://www.facebook.com/410011599077843");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_eracing: setActionBarTitle("eRacing");
                bundle.putString("URL", "http://www.la-eracing.de/de");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL" ,"https://www.facebook.com/167640983256062");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_homepage: setActionBarTitle("HAW Landshut");
                bundle.putString("URL" ,"https://www.haw-landshut.de/home.html");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL", "https://www.facebook.com/HAW.Landshut/");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_stuv: setActionBarTitle("StuV");
                bundle.putString("URL" ,"https://www.facebook.com/308869473753");
                bundle.putBoolean("FACEBOOK", false);
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_hsg: setActionBarTitle("Hochschulgemeinde");
                bundle.putString("URL", "http://diehochschulgemeinde.de/");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL" ,"https://www.facebook.com/137374916448701");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_selam: setActionBarTitle("SELAM");
                bundle.putString("URL" ,"https://www.facebook.com/296463403840444");
                bundle.putBoolean("FACEBOOK", false);
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_stadtwerke: setActionBarTitle("Stadtwerke Landshut");
                bundle.putString("URL" ,"http://www.stadtwerke-landshut.de/");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL", "https://www.facebook.com/167638136595006");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_unicef: setActionBarTitle("Unicef");
                bundle.putString("URL" ,"https://www.facebook.com/383688201667153");
                bundle.putBoolean("FACEBOOK", false);
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_unicon: setActionBarTitle("Unicon");
                bundle.putString("URL", "http://www.unicon-landshut.de/");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL" ,"https://www.facebook.com/463094080439333");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_vde: setActionBarTitle("VDE Landshut");
                bundle.putString("URL", "http://www.vde-landshut.de/");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL" ,"https://www.facebook.com/226597987534627");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_wingla: setActionBarTitle("WingLA");
                bundle.putString("URL", "http://www.wingla.de/");
                bundle.putBoolean("FACEBOOK", true); bundle.putString("FURL" ,"https://www.facebook.com/122557234421163");
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_sbportal: setActionBarTitle("SB Portal");
                bundle.putString("URL", "https://service.fh-landshut.de/netreg/do/ask_username");
                bundle.putBoolean("FACEBOOK", false);
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
            case R.id.link_allgemeines: setActionBarTitle("Allgemeine Infos");
                bundle.putString("URL", "https://www.haw-landshut.de/studium.html");
                bundle.putBoolean("FACEBOOK", false);
                newFragment = new WebViewFragment();
                newFragment.setArguments(bundle); break;
        }

        if (newFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, newFragment).commit();
        }

        mCurrentFragment = newFragment;

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Create Submenu when Links is selected
    public void openLinkMenu(){
        navigationView.getHeaderView(0).setVisibility(View.GONE);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.fragment_links_drawer);
        navigationView.setItemIconTintList(null);
        submenuOpen = true;
    }

    //Gets called when ImageButton in HomeFragment is clicked
    public boolean onHomeScreenItemSelected(Fragment fragment,int id) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, fragment).commit();
        }
        navigationView.getMenu().getItem(id).setChecked(true);

        return true;
    }

    //Used to handle clickEvents() in HomeFragment
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentFragment instanceof RoomfinderFragment) {
            RoomfinderFragment fragment = (RoomfinderFragment) mCurrentFragment;

            fragment.refreshFavorites();
            if (data != null && data.hasExtra(RoomfinderFragment.KEY_MARKED_ROOM)) {
                Room room = (Room) data.getSerializableExtra(RoomfinderFragment.KEY_MARKED_ROOM);
                fragment.markRoom(room);
            }
        }
    }

    public void setActionBarTitle(String title){
        toolbar.setTitle(title);
    }

    /* Hash for Facebook App Authorisation
         * Was needed for registration at developer.facebook.com currently not used*/
    @SuppressWarnings("unused")
    public void activateFacebook(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "de.hawlandshut.studyla",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.w("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}

