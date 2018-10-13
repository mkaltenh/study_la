package de.hawlandshut.studyla.fragments;

/*******************************************************************************************
 * @Fragment: WebViewFragment
 * Wird zur Anzeige von Websites innerhalb der App verwendet. Liest die, im Bundle übergebenen,
 * Werte und stellt diese je nach Art in einer WebView dar.
 * @Params: String "URL" boolean "FACEBOOK" String "FURL" String "PDF"
 * @Activity: MainActivity
 * @Author: Max Kaltenhauser
 *******************************************************************************************/

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.R;
import de.hawlandshut.studyla.links.WebViewController;

public class WebViewFragment extends Fragment {

    @BindView(R.id.help_webview) WebView mWebView;
    @BindView(R.id.progress_webview) ProgressBar mProgress;
    @BindView(R.id.fab_facebook) FloatingActionButton mFAB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        ButterKnife.bind(this, view);
        Bundle bundle = this.getArguments();
        String mURL = "";

        if(bundle.get("URL") != null && !bundle.getBoolean("FACEBOOK")){
            mURL = bundle.getString("URL");
        }
        else if(bundle.get("URL") != null && bundle.getBoolean("FACEBOOK")){
            mURL = bundle.getString("URL");
            mFAB.setVisibility(View.VISIBLE);

            final String fURL = bundle.getString("FURL");
            mFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fURL));
                    startActivity(browserIntent);
                }
            });
        }
        else if(bundle.get("PDF") != null){
            String pdfURL = "https://drive.google.com/viewerng/viewer?embedded=true&url=";
            mURL = pdfURL + bundle.getString("PDF");
        }

        CookieSyncManager.createInstance(getActivity());
        CookieManager.getInstance();

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebViewClient(new WebViewController());
        mWebView.loadUrl(mURL);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if(progress == 100) {
                    mProgress.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}