package com.capstonappdeveloper.capstone_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.capstonappdeveloper.capstone_android.Protocol.Map.Event;
import com.capstonappdeveloper.capstone_android.Protocol.Map.EventFetcher;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by james on 2016-11-28.
 */

public class WebFragment extends Fragment {

    private HashMap<String, Event> events;
    private String URL;
    private ProgressBar dialog;
    private ListView listView;
    public void init(String url) {
        URL = url;
    }

    private class EventAdapter extends ArrayAdapter<Event>{

        Context context;
        int layoutResourceId;
        ArrayList<Event> data = null;

        public EventAdapter(Context context, int layoutResourceId, ArrayList<Event> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            EventHolder holder = null;

            if(row == null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new EventHolder();
                holder.icon = (ImageView)row.findViewById(R.id.event_row_icon);
                holder.title = (TextView)row.findViewById(R.id.event_row_title);
                holder.date = (TextView)row.findViewById(R.id.event_time_created);
                holder.numParticipants = (TextView)row.findViewById(R.id.event_num_participants);

                row.setTag(holder);
            }
            else
            {
                holder = (EventHolder) row.getTag();
            }

            Event event = data.get(position);
            holder.icon.setImageBitmap(event.icon);
            holder.title.setText(event.eventName);
            holder.date.setText(event.timeCreated);
            holder.numParticipants.setText(Integer.toString(event.numParticipants));

            return row;
        }

        class EventHolder
        {
            ImageView icon;
            TextView title;
            TextView date;
            TextView numParticipants;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected void showSpinner() { dialog.setVisibility(View.VISIBLE); }

    protected void hideSpinner() {
        dialog.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View parentView = inflater.inflate(R.layout.webview_layout, container, false);
        dialog = (ProgressBar) parentView.findViewById(R.id.load_spinner);
        listView = (ListView) parentView.findViewById(R.id.events_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final Event item = (Event) parent.getItemAtPosition(position);
                updateUrl(StaticResources.HTTP_PREFIX + StaticResources.ProdServer + StaticResources.GET_MESH_MODEL + item.id);
            }

        });

        events = new HashMap<String, Event>();

        WebView webview = (WebView) parentView.findViewById(R.id.webview);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                showSpinner();
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                    hideSpinner();
                }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setVerticalScrollBarEnabled(false);
        webview.setHorizontalScrollBarEnabled(false);
        fetchEvents();
        return parentView;
    }

    public void updateUrl(String url) {
        URL = url;
        WebView webview = (WebView) getView().findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(url);
    }

    public HashMap<String, Event> getEvents() {
        return this.events;
    }

    public ArrayList<Event> getEventsList() {
        ArrayList<Event> eventsList = new ArrayList<Event>();
        for (Event event : events.values()) {
            Log.d("WEB EVENT LIST", event.id + event.icon);
            eventsList.add(event);
        }
        return eventsList;
    }

    public void setEvents(HashMap<String, Event> events) {
        this.events = events;
    }

    public void fetchEvents() {
        showSpinner();
        new EventFetcher(this).execute();
    }

    public void setListViewContents() {
        final EventAdapter adapter = new EventAdapter(getContext(),
                R.layout.event_list_row, getEventsList());
        listView.setAdapter(adapter);
    }
}