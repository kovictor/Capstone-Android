package com.capstonappdeveloper.capstone_android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by james on 2016-11-28.
 */

public class WebFragment extends Fragment {

    private String URL;
    private ProgressBar dialog;
    public void init(String url) {
        URL = url;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected void showSpinner(View v) {
        dialog = (ProgressBar) v.findViewById(R.id.load_spinner);
        dialog.setVisibility(View.VISIBLE);
    }

    protected void hideSpinner() {
        dialog.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View parentView = inflater.inflate(R.layout.webview_layout, container, false);

        if (URL != null) {
            WebView webview = (WebView) parentView.findViewById(R.id.webview);
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    showSpinner(parentView);
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    hideSpinner();
                }
            });
            webview.getSettings().setJavaScriptEnabled(true);
            webview.setVerticalScrollBarEnabled(false);
            webview.setHorizontalScrollBarEnabled(false);
            webview.loadUrl(URL);
        }
        return parentView;
    }

    public void updateUrl(String url) {
        URL = url;
        WebView webview = (WebView) getView().findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(url);
    }
}