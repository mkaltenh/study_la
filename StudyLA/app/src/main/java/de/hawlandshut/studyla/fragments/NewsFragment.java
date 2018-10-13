package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * @Fragment: NewsFragment
 * Fragt www.haw-landshut.de/aktuelles/news nach aktuellen News ab
 * Speicher diese in eine Liste newsList und übergibt diese zur Darstellung an den
 * RecyclerViewAdapter nAdapter welcher die Anzeige regelt
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.MainActivity;
import de.hawlandshut.studyla.R;

import android.widget.ProgressBar;

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
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import de.hawlandshut.studyla.news.News;
import de.hawlandshut.studyla.news.NewsAdapter;

public class NewsFragment extends Fragment {

    private static final int TIMEOUT_IN_MS = 5000;
    ArrayList<News> newsList = new ArrayList<>();
    private NewsAdapter nAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    @BindView(R.id.recycler_view_news) RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar_news) ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nAdapter = new NewsAdapter(newsList);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).setActionBarTitle("News");
        View view = inflater.inflate(R.layout.fragment_study, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(nAdapter);

        //Handle Click on Items in RecyclerView
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new MainActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                News news = newsList.get(position);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                startActivity(browserIntent);
            }
            @Override
            public void onLongClick(View view, int position) {
                //Required for ClickListener() Class
            }
        }));

        new getHomepageNews().execute();
    }


    /**
    * RecyclerTouchlistener und TouchEvents
    * Handlen verschiedener onTouchEvents Abfragen der Child-Position
     */
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
    * AsyncTask paseHTML
    * Fragt HAW-Landshut Website nach aktuellen News ab
    * Speichert News mit Namen, Datum und URL in eventList
     */
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


                for (int i = 0; i<names.size(); i++){

                    News news = new News();
                        if(names.get(i).hasText()){
                            news.setName(names.get(i).text());
                            news.setUrl(names.get(i).attr("abs:href"));
                            news.setImageSRC("https://www.haw-landshut.de/" + images.get(i).attr("src"));
                            news.setDescription(descriptions.get(i).text());
                        }
                        newsList.add(i, news);
                        publishProgress();
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
        protected void onProgressUpdate(String... values) {
            //Every time progress is published
            nAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer result) {
            //AFTER TASK IS DONE
            nAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

    }

}

