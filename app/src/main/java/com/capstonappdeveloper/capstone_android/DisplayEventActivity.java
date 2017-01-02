package com.capstonappdeveloper.capstone_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DisplayEventActivity extends AppCompatActivity {

    //dummy array for now
    String[] dummyArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_event);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, dummyArray);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }
}
