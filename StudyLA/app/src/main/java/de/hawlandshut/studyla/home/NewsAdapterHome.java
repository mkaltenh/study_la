package de.hawlandshut.studyla.home;

/**
* @AdapterKlasse: NewsAdapterHome
* Wird zur Darstellung der Recyclerview verwendet
* Regelt die Darstellung der einzelnen News-Elemente
* Angepasste Version des NewsAdapters für die Startseite
* @Fragment: HomeFragment
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.news.News;



import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.R;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.R;

public class NewsAdapterHome extends RecyclerView.Adapter<NewsAdapterHome.MyViewHolder> {

    private ArrayList<News> newsList;
    OkHttpClient okHttpClient;
    OkHttpDownloader okHttpDownloader;
    Picasso.Builder builder;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.news_headline)
        TextView mHeadline;
        @BindView(R.id.news_description) TextView mDescription;
        @BindView(R.id.news_previewpicture)
        ImageView mPicture;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public NewsAdapterHome(ArrayList<News> newsList) {
        this.newsList = newsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_list_row_home, parent, false);

        okHttpClient = new OkHttpClient();

        try {
            enableSSLSocket();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        okHttpDownloader = new OkHttpDownloader(okHttpClient);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.mHeadline.setText(news.getName());
        holder.mDescription.setText(news.getDescription());

        //Set Up Picasso with OkHTTPDownloader
        builder =  new Picasso.Builder(holder.mPicture.getContext());
        builder.downloader(okHttpDownloader);
        Picasso picasso = builder.build();

        //Set Preview Picture of News Headline
        if(news.getImageSRC() != null){
            //Context context = holder.mPicture.getContext();
            picasso.load(news.getImageSRC()).into(holder.mPicture);
            //Picasso.with(context).load(news.getImageSRC()).into(holder.mPicture);
            }
        else{
            holder.mPicture.setImageResource(R.drawable.logo_hawlandshut);
        }
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

        okHttpClient.setSslSocketFactory(context.getSocketFactory());
        //HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

}
