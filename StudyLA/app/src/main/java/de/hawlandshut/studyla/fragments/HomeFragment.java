package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * @Fragment: HomeFragment
 * StartScreen der App, zeigt den aktuellsten Termin der HAW Landshut FB Page an.
 * Zeigt außerdem die aktuellste News der HAW Landshut Homepage an. Für Funktionalität siehe
 * EventFragment bzw. NewsFragment
 * @Activity: MainActivity
 * @Author: Max Kaltenhauser
 *******************************************************************************************/

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.MainActivity;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.events.DividerItemDecoration;
import de.hawlandshut.studyla.events.Event;
import de.hawlandshut.studyla.events.EventAdapter;
import de.hawlandshut.studyla.home.CustomPagerAdapter;
import de.hawlandshut.studyla.home.NewsAdapterHome;
import de.hawlandshut.studyla.news.News;

public class HomeFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.viewpager) ViewPager pager;
    @BindView(R.id.tabDots) TabLayout tabLayout;
    @BindView(R.id.recycler_view_start1) RecyclerView mRecyclerView;
    @BindView(R.id.recycler_view_start2) RecyclerView mRecyclerViewNews;
    @BindView(R.id.progress_text) TextView mProgressText;
    @BindView(R.id.progress_text2) TextView mProgressText2;
    ArrayList<Event> eventList = new ArrayList<>();
    private EventAdapter eAdapter;
    private static final int TIMEOUT_IN_MS = 5000;
    ArrayList<News> newsList = new ArrayList<>();
    private NewsAdapterHome nAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_home, container, false);
        ((MainActivity) getActivity()).setActionBarTitle("StudyLA");

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.fragment_start, container, false);

        ButterKnife.bind(this, view);

        ImageButton studiumButton = (ImageButton) view.findViewById(R.id.imageButton_Studium);
        ImageButton mensaButton = (ImageButton) view.findViewById(R.id.imageButton_Mensa);
        ImageButton orientationButton = (ImageButton) view.findViewById(R.id.imageButton_Orientierung);
        //ImageButton transportButton = (ImageButton) v.findViewById(R.id.imageButton_Bus);
        ImageButton vorlesungsButton = (ImageButton) view.findViewById(R.id.imageButton_Vorlesungsplan);
        ImageButton newsButton = (ImageButton) view.findViewById(R.id.imageButton_Aktuelles);

        studiumButton.setOnClickListener(this);
        mensaButton.setOnClickListener(this);
        orientationButton.setOnClickListener(this);
        //transportButton.setOnClickListener(this);
        vorlesungsButton.setOnClickListener(this);
        newsButton.setOnClickListener(this);



        pager.setAdapter(new CustomPagerAdapter(getActivity()));
        tabLayout.setupWithViewPager(pager, true);


        //Set up RecyclerView
        eAdapter = new EventAdapter(eventList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(eAdapter);

        nAdapter = new NewsAdapterHome(newsList);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerViewNews.setHasFixedSize(true);
        mRecyclerViewNews.setLayoutManager(mLinearLayoutManager);
        mRecyclerViewNews.setAdapter(nAdapter);

        new getFacebookEvents().execute();
        new getHomepageNews().execute();

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {

        Fragment newFragment = null;
        Bundle bundle = new Bundle();
        int id = 0;

        switch (view.getId()) {
            case R.id.imageButton_Studium:
                newFragment = new NewsFragment();
                id = 1;
                break;
            case R.id.imageButton_Mensa:
                newFragment = new CanteenFragment();
                id = 2;
                break;
            case R.id.imageButton_Orientierung:
                newFragment = new RoomfinderFragment();
                id = 3;
                break;
            case R.id.imageButton_Bus:
                bundle.putString("DepArr", "arr");
                newFragment = new TransportFragment();
                newFragment.setArguments(bundle);
                id = 4;
                break;
            case R.id.imageButton_Vorlesungsplan:
                newFragment = new TimetableFragment();
                id = 5;
                break;
            case R.id.imageButton_Aktuelles:
                newFragment = new EventFragment();
                id = 6;
                break;
        }

        ((MainActivity) getActivity()).onHomeScreenItemSelected(newFragment, id);
    }

    @Override
    public void onDestroy(){
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        super.onDestroy();
    }

    /*
    * AsyncTask getFacebookEvents
    * GET Abfrage um Access Token zu erhalten
    * Verwendet Access Token für Graph API Abfrage
    * Abfragen aller relevanten Facebook Seiten nach den aktuellsten Veranstaltungen
    */
    private class getFacebookEvents extends AsyncTask<Void, String, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            //FACEBOOK
            String appID = "125306974573504";
            String appSecret = "79b8c9a1b3ec6254a2cc254713b8da55"; //Hide in future versions ?
            String token;

            //GET ACCESS_TOKEN
            Bundle bundle = new Bundle();
            bundle.putString("client_id", appID);
            bundle.putString("client_secret", appSecret);
            bundle.putString("grant_type", "client_credentials");

            GraphRequest request = new GraphRequest(null, "oauth/access_token", bundle, HttpMethod.GET);
            final GraphResponse resp = request.executeAndWait();

            try {
                token = resp.getJSONObject().get("access_token").toString();

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                return null;
            }

            //ACCESS TOKEN
            AccessToken access_token = new AccessToken(token, appSecret, appID, null, null, null, null, null);

            //GET GROUP EVENTS
            //Abfrage der einzelnen Facebook Gruppen nach Events innerhalb des nächsten Monats
            getGroupEvents(access_token, "/202731836576420"); //HAW Landshut

            return null;
        }

        public void getGroupEvents(AccessToken access_token, String groupID){
            //EVENT ABFRAGE
            GraphRequest event_request = GraphRequest.newGraphPathRequest(
                    access_token,
                    groupID,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            // Get data from response
                            try {
                                JSONObject responseObject = response.getJSONObject();

                                String picUrl = null;
                                if (responseObject.has("picture")) {
                                    JSONObject jsonPictureData = responseObject.getJSONObject("picture").getJSONObject("data");
                                    picUrl = jsonPictureData.getString("url");
                                }

                                if (responseObject.has("events")){
                                    JSONArray jsonEventData = responseObject.getJSONObject("events").getJSONArray("data");

                                    for (int i = 0; i < jsonEventData.length(); i++) {
                                        JSONObject jsonEventObject = jsonEventData.getJSONObject(i);

                                        String event_name = jsonEventObject.getString("name");
                                        String event_url = "https://www.facebook.com/events/" + jsonEventObject.getString("id");

                                        //Set 01/01/2000 as endDate incase endDate is null
                                        //Will be used later to determine if endDate.after(today); returns false for this default value
                                        Date event_time = null; //All Events have start_time therefore no default value required
                                        Date end_time = null;
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
                                        try {
                                            end_time = sdf.parse("01/01/2000");
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        //Convert Facebook Date Format to same format as HAW-Landshut website
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.US);
                                        try {
                                            event_time = df.parse(jsonEventObject.getString("start_time"));
                                            if (jsonEventObject.has("end_time"))
                                                end_time = df.parse(jsonEventObject.getString("end_time"));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        //Only save events that are relevant into eventList
                                        Date today = new Date();
                                        try {
                                            if (event_time.after(today) || event_time.equals(today) || (event_time.before(today) && end_time.after(today))) {
                                                //Save data in eventList
                                                Event event = new Event();

                                                event.setName(event_name);
                                                event.setDate(event_time);
                                                if (end_time != null) {
                                                    event.setEndDate(end_time);
                                                }
                                                event.setUrl(event_url);
                                                event.setPicUrl(picUrl);

                                                eventList.add(eventList.size(), event);
                                            }
                                        }
                                        catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                                return;
                            }
                            sortEventList();

                            Event obj = eventList.get(0); // remember first item
                            eventList.clear(); // clear complete list
                            eventList.add(0, obj); // add first item

                            eAdapter.notifyDataSetChanged();
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mProgressText.setVisibility(View.GONE);
                        }
                    });

            String timestamp = String.valueOf(((System.currentTimeMillis()/1000) - 60 * 60 * 24 * 30)); //Get all events within the last 30 days

            Bundle parameters = new Bundle();
            parameters.putString("fields", "picture.type(large),events.since(" + timestamp + "){start_time,end_time,name}");
            event_request.setParameters(parameters);
            event_request.executeAsync();
        }
    }

    //Liste nach dem Datum sortieren
    public void sortEventList(){
        Collections.sort(eventList, new Comparator<Event>() {
            public int compare(Event event1, Event event2) {
                return event1.getDate().compareTo(event2.getDate());
            }
        });
    }


    //AsyncTask paseHTML
    //Fragt HAW-Landshut Website nach aktuellen Events ab
    //Speichert Events mit Namen, Datum und URL in eventList
    private class getHomepageNews extends AsyncTask<Void, String, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

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
                Document doc = Jsoup.parse(new URL("https://www.haw-landshut.de/aktuelles/news.html"), TIMEOUT_IN_MS);

                doc.select("span.news-list-morelink").remove();

                Elements names = doc.select("div.list_content a[href]");
                Elements images = doc.select("div.list_image img");
                Elements descriptions = doc.select("div.list_subheader p");


                for (int i = 0; i<1; i++){

                    News news = new News();
                    if(names.get(i).hasText()){
                        news.setName(names.get(i).text());
                        news.setUrl(names.get(i).attr("abs:href"));
                        news.setImageSRC("https://www.haw-landshut.de/" + images.get(i).attr("src"));
                        news.setDescription(descriptions.get(i).text());
                    }
                    newsList.add(i, news);
                }


            } catch (IOException e) {
                e.printStackTrace();
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
        protected void onPostExecute(Integer result) {
            //AFTER TASK IS DONE
            nAdapter.notifyDataSetChanged();
            mRecyclerViewNews.setVisibility(View.VISIBLE);
            mProgressText2.setVisibility(View.GONE);
        }

    }
}