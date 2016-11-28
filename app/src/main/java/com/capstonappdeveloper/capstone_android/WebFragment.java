package com.capstonappdeveloper.capstone_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by james on 2016-11-28.
 */

public class WebFragment extends Fragment {

    private String URL;

    public void init(String url) {
        URL = url;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.webview_layout, container, false);

        if (URL != null) {
            WebView webview = (WebView) view.findViewById(R.id.web);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.setWebViewClient(new WebViewClient());
            webview.loadUrl(URL);
        }
        return view;
    }

    public void updateUrl(String url) {
        URL = url;
        WebView webview = (WebView) getView().findViewById(R.id.web);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(url);
    }
}