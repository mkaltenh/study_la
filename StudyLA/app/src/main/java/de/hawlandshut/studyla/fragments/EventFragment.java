package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * FragmentKlasse: EventFragment
 * Fragt diverse Facebook-Gruppen nach anstehenden Events bis 30 Tage in die Zukunft an
 * Gruppen: HAW Landshut, WingLA, StuV, HSG, SELAM, eRacing, VDE, Unicon, CampusLA, Unicef
 * Fragt diese mithilfe einer FacebookGraphAPI Anfrage ab und erstellt mithilfe der, im JSON
 * Format vorliegenden Antwort eine Liste der Events welche anschließend an den eAdapter
 * zur Erstellung der RecyclerView übergeben wird.
 * @OLD Alte Version dieses Fragments verwendete JSOUP um Events der HAW Landshut von der
 * Website zu scrapen, wurde aus Performance und Wartungsgründen auf Facebook umgestellt
 * @Activity: MainActivity
 * @Author: Max Kaltenhauser
 *******************************************************************************************/

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.MainActivity;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.events.DividerItemDecoration;
import de.hawlandshut.studyla.events.Event;
import de.hawlandshut.studyla.events.EventAdapter;

public class EventFragment extends Fragment{

    private static final int TIMEOUT_IN_MS = 5000;
    ArrayList<Event> eventList = new ArrayList<>();
    private EventAdapter eAdapter;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).setActionBarTitle("Veranstaltungen");
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, view);

        //Get Event data from Homepage and Facebook Groups
        //new getHomepageEvents().execute();
        new getFacebookEvents().execute();

        //Set up RecyclerView
        //Setzen der Orientierung, des Adapters, der zugehörigen ArrayList sowie den Dividern zw. den Elementen
        eAdapter = new EventAdapter(eventList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(eAdapter);

        //Handle Click on Items in RecyclerView
        //Verknüpfung des OnItemTouchListeners mit der RecyclerView mRecyclerView
        //Beim Klick auf eines der Elemente wird die zugehörige URL per Intent im Browser aufgerufen
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new MainActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Event event = eventList.get(position);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getUrl()));
                startActivity(browserIntent);
            }
            @Override
            public void onLongClick(View view, int position) {
                //Required for ClickListener() Class
            }
        }));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //Handles Clicks on Item in RecyclerView
    //Implementierung des OnItemTouchListeners
    @SuppressWarnings("deprecation")
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    /**
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
            //Wird für folgende Graph API Abfragen verwendet
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
            getGroupEvents(access_token, "/122557234421163"); //WingLA
            getGroupEvents(access_token, "/308869473753"); //StuV
            getGroupEvents(access_token, "/463094080439333"); //Unicon
            getGroupEvents(access_token, "/410011599077843"); //Campus Landshut
            getGroupEvents(access_token, "/167640983256062"); //eRacing
            getGroupEvents(access_token, "/383688201667153"); //Unicef
            getGroupEvents(access_token, "/296463403840444"); //SELAM
            getGroupEvents(access_token, "/137374916448701"); //HSG
            getGroupEvents(access_token, "/226597987534627"); //VDE
            getGroupEvents(access_token, "/202731836576420"); //HAW Landshut

            return null;
        }

        /**
        * getGroupEvents stellt eine Facebook Graph-API Anfrage um aktuelle Events zu erhalten
        * @param access_token Access-Token welcher zum Nachweis der Berechtigung benötigt wird
        * @param groupID Eindeutige ID der Facebook Gruppe
        *
         */
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
                            eAdapter.notifyDataSetChanged();
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);
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



    /**
    * AsyncTask getHomepageEvents
    * Fragt HAW-Landshut Website nach aktuellen Events ab
    * Speichert Events mit Namen, Datum und URL in eventList
    * In der aktuellen Version nichtmehr benutzt da HomepageEvents von der HAW Landshut FB Seite gezogen werden
    */
    @SuppressWarnings("unused")
    private class getHomepageEvents extends AsyncTask<Void, String, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            //JSOUP WEBSCRAPING
            //Alle Elemente der Veranstaltungs-Seite wiedergeben
            try {
                Document doc = Jsoup.parse(new URL("https://www.haw-landshut.de/kooperationen/veranstaltungen.html"), TIMEOUT_IN_MS);

                Elements names = doc.select("h2").select("a[href].url");
                Elements links = doc.select("h2").select("a.url");
                Elements dates = doc.select("div.cal_date");

                for (int i = 0; i<names.size(); i++){
                    Event event = new Event();

                    if(names.get(i).hasText()){
                        event.setName(names.get(i).text());
                        String time = dates.get(i).text();
                        String startDate;
                        String endDate = null;

                        if(time.contains(" - ")){
                            startDate = time.substring(4,time.indexOf("-") - 1);
                            endDate = time.substring(time.indexOf("-")+5);
                        } else{
                            startDate = time.substring(4);
                        }

                        SimpleDateFormat format = new SimpleDateFormat( "dd. MMMM yyyy", Locale.GERMAN );
                        try{
                            event.setDate(format.parse(startDate));
                            if(endDate != null) event.setEndDate(format.parse(endDate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        event.setUrl(links.get(i).attr("abs:href"));
                    }
                    eventList.add(i, event);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            sortEventList();
            System.out.println("SIZE: " + eventList.size());
            eAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

    }
}