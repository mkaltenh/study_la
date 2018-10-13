package de.hawlandshut.studyla.links;

/**
* WebViewClient: WebViewController
* Wird für das erstellen der WebViews verwendet
* Regelt das laden der URLs
* @Fragment: WebViewFragment, TimetableFragment
 */

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewController extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}