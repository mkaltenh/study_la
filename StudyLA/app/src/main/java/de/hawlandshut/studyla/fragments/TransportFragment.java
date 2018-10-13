package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * @Fragment: TransportFragment
 * Fragt bayern-fahrplan.de nach aktuellen Buslinien welche von der HAW Landshut abgehen ab
 * Speicher diese in eine Liste busList und übergibt diese zur Darstellung an den
 * RecyclerViewAdapter bAdapter welcher die Anzeige regelt
 * @Activity: MainActivity
 * @Author: Max Kaltenhauser
 *******************************************************************************************/

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.MainActivity;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.transport.BusAdapter;
import de.hawlandshut.studyla.transport.BusLine;

public class TransportFragment extends Fragment{

    ArrayList<BusLine> busList = new ArrayList<>();
    private BusAdapter bAdapter;
    String itdDateTimeDepArr;

    @BindView(R.id.recycler_view_bus) RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar_bus) ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).setActionBarTitle("Abfahrt HAW Landshut");
        View view = inflater.inflate(R.layout.fragment_transport, container, false);
        ButterKnife.bind(this, view);

        Bundle bundle = this.getArguments();

        if(bundle.get("DepArr") != null){
            itdDateTimeDepArr = bundle.getString("DepArr"); //Dep = Departing; Arr = Arriving
        }

        new getTransportInformation().execute();

        //Set up RecyclerView
        bAdapter = new BusAdapter(busList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(bAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //AsyncTask paseHTML
    //Fragt Bayern-Fahrplan ab und schickt GET Request
    //Speichert Antwort in busList ab
    private class getTransportInformation extends AsyncTask<Void, String, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            //JSOUP WEBSCRAPING
            //Alle Buszeiten wiedergeben
            try {
                //GET CURRENT TIME AND DATE
                LocalTime localTime = new LocalTime();
                LocalDate localDate = new LocalDate();
                String itdTime = localTime.getHourOfDay() + ":" + localTime.getMinuteOfHour();
                String itdDateDayMonthYear = localDate.toString("dd.MM.yyyy");

                Connection.Response form = Jsoup.connect("http://www.bayern-fahrplan.de/de/abfahrt-ankunft").method(Connection.Method.GET).execute();

                //POST REQUEST
                //GET DOCUMENT FROM FORM
                Document doc = Jsoup.connect("http://www.bayern-fahrplan.de/de/abfahrt-ankunft")
                        .data("name_dm", "Landshut Am Lurzenhof")
                        .data("itdDateDayMonthYear", itdDateDayMonthYear)
                        .data("itdTime", itdTime)
                        .data("itdDateTimeDepArr", itdDateTimeDepArr) //arr = AN HAW Landshut dep = VON HAW Landshut
                        .cookies(form.cookies())
                        .post();

                //REMOVE UNNECESSARY ELEMENTS
                doc.select("label.checkbox").remove();

                //ANALYSE DOCUMENT WITH JSOUP
                for (Element table : doc.select("table.trip")) {
                    for (Element row : table.select("tr")) {
                        Elements data = row.select("td");
                        Elements iconData = row.select("td.icon").select("span.mot-icon");

                            if(data.get(0).hasText() && data.get(2).hasText()){
                                BusLine busline = new BusLine();

                                busline.setTime(data.get(0).text());
                                busline.setDestination(data.get(2).text());
                                busline.setLine(data.get(1).select("span").text().replaceAll("\\s+",""));

                                //CHECK WHICH TYPE OF BUS IT IS
                                for(int mot = 0; mot<16; mot++){
                                    String motType = "span[class=icon mot mot" + String.valueOf(mot) + "]";
                                    if(!iconData.select(motType).isEmpty()){
                                        busline.setType(iconData.select(motType).attr("title"));
                                    }
                                }
                                busList.add(busline);
                                publishProgress();
                            }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //Every time progress is published
            bAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer result) {
            //AFTER TASK IS DONE
            bAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

    }

}
