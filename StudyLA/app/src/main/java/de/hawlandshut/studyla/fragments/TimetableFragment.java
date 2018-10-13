package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * @Fragment: TimetableFragment
 * Fragt www.haw-landshut.de/fakultaeten nach aktuellen Vorlesungsplänen nach Fakultät ab
 * Speicher diese in eine zweiteilige Liste welche an den listAdapter übergeben wird um
 * eine ExpandableListView darzustellen.
 * listDataHeader stellt die übergeordneten Gruppen der ExpandableListView dar
 * listDataChild stellt die untergeordneten Menüs dar, geordnet nach Header
 * listDataLinks beinhaltet alle zugehörigen Links zu den einzelnen Vorlesungsplänen
 * @Activity: MainActivity
 * @Author: Max Kaltenhauser
 *******************************************************************************************/

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ExpandableListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.MainActivity;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.links.WebViewController;
import de.hawlandshut.studyla.timetable.ExpandableListAdapter;

public class TimetableFragment extends Fragment implements MainActivity.OnBackPressedListener{

    @BindView(R.id.timetable_webview) WebView mWebView;
    ExpandableListAdapter listAdapter;
    @BindView(R.id.lvExp) ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private static final int TIMEOUT_IN_MS = 5000;
    HashMap<String, List<String>> listDataLinks;
    String[] myStrings;
    boolean webViewVisible;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);
        ButterKnife.bind(this, view);
        ((MainActivity)getActivity()).setActionBarTitle("Vorlesungsplan");

        //Liste mit Link zu "Infos zum laufendem Studienbetrieb" für die einzelnen Fakultäten
        //Stand 2017: Informatik, Maschinenbau, eTechnik, Soziale Arbeit, BWL, Interdisziplinäre Studien
        myStrings = new String[] {
                "https://www.haw-landshut.de/hochschule/fakultaeten/informatik/infos-zum-laufenden-studienbetrieb.html",
                "https://www.haw-landshut.de/hochschule/fakultaeten/maschinenbau/infos-zum-laufenden-studienbetrieb.html",
                "https://www.haw-landshut.de/hochschule/fakultaeten/elektrotechnik-und-wirtschaftsingenieurwesen/infos-zum-laufenden-studienbetrieb.html",
                "https://www.haw-landshut.de/hochschule/fakultaeten/soziale-arbeit/infos-zum-laufenden-studienbetrieb.html",
                "https://www.haw-landshut.de/hochschule/fakultaeten/betriebswirtschaft/infos-zum-laufenden-studienbetrieb.html",
                "https://www.haw-landshut.de/hochschule/fakultaeten/interdisziplinaere-studien/infos-zum-laufenden-studienbetrieb.html"
        };

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebViewClient(new WebViewController());

        prepareListData();
        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                expListView.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadUrl("https://docs.google.com/gview?embedded=true&url=https://www.haw-landshut.de/" + listDataLinks.get(listDataHeader.get(groupPosition)).get(
                        childPosition));
                webViewVisible = true;

                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).setOnBackPressedListener(this);
    }

    /*
    * Preparing the list data
    */
    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listDataLinks = new HashMap<>();

        // Einfügen der einzelnen Header
        // Übergeordnete Ansicht der ListView
        listDataHeader.add("Informatik");
        listDataHeader.add("Maschinenbau");
        listDataHeader.add("Elektrotechnik");
        listDataHeader.add("Soziale Arbeit");
        listDataHeader.add("Betriebswirtschaft");
        listDataHeader.add("Interdisziplinaere Studien");

        // Anzeigen von "Loading Data" falls getHomepageEvents noch nicht fertig ist
        // Verhindert NullpointerException
        List<String> tempChildList = new ArrayList<>();
        tempChildList.add("Loading Data");

        // Befüllen der Child Elemente mit Platzhaltern
        listDataChild.put(listDataHeader.get(0), tempChildList);
        listDataChild.put(listDataHeader.get(1), tempChildList);
        listDataChild.put(listDataHeader.get(2), tempChildList);
        listDataChild.put(listDataHeader.get(3), tempChildList);
        listDataChild.put(listDataHeader.get(4), tempChildList);
        listDataChild.put(listDataHeader.get(5), tempChildList);

        new getHomepageEvents(myStrings).execute();
    }

    @Override
    public void doBack() {
        //BackPressed in activity will call this
        if(webViewVisible){
            mWebView.setVisibility(View.GONE);
            expListView.setVisibility(View.VISIBLE);
            webViewVisible = false;
        }

    }


    private class getHomepageEvents extends AsyncTask<Void, String, Integer> {

        String[] url_array;
        public getHomepageEvents(String[] url_array){
            this.url_array = url_array;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int i = 0;

            for(String url : url_array){
                try {
                    enableSSLSocket();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                //JSOUP WEBSCRAPING
                //Alle Elemente der News-Seite wiedergeben
                try {
                    Document doc = Jsoup.parse(new URL(url), TIMEOUT_IN_MS);

                    Elements links = doc.select("a[href*=Vorlesungspl]"); //Suche nach allen Links welche "Vorlesungspl" enthalten

                    List<String> tempChildList = new ArrayList<>();
                    List<String> tempLinkList = new ArrayList<>();
                    for (Element element : links) {
                        tempLinkList.add(element.attr("href")); //Link in tempLinkList einfügen
                        tempChildList.add(element.text()); //Beschreibung des Links in tempChildList einfügen
                    }
                    listDataChild.put(listDataHeader.get(i), tempChildList);
                    listDataLinks.put(listDataHeader.get(i), tempLinkList);
                    publishProgress();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
            }
            return null;
        }

        public void enableSSLSocket() throws KeyManagementException, NoSuchAlgorithmException {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //Every time progress is published
            listAdapter.notifyDataSetChanged();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer result) {
            //AFTER TASK IS DONE
            listAdapter.notifyDataSetChanged();
        }

    }

}
