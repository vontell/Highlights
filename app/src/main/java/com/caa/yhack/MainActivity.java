package com.caa.yhack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.caa.yhack.spec.HomePageObject;
import com.caa.yhack.views.VideoArrayAdapter;
import com.caa.yhack.youtube.Video;
import com.yayandroid.parallaxlistview.ParallaxListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ParallaxListView wideList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wideList = (ParallaxListView) findViewById(R.id.parallaxListView);
        wideList.setDividerHeight(5);

    }

    public void loadHomeObjects() {

        // TODO: Replace this with actual call
        HomePageObject[] objects = getTestVids();
        VideoArrayAdapter adapter = new VideoArrayAdapter(this, objects);

    }


    private HomePageObject[] getTestVids() {

        List<HomePageObject> list = new ArrayList<HomePageObject>();
        list.add(new Video("YouTube Collection", "Y_UmWdcTrrc", 0, 1, 5, false));
        list.add(new Video("GMail Tap", "1KhZKNZO8mQ", 0, 1, 5, false));
        list.add(new Video("Chrome Multitask", "UiLSiqyDf4Y", 0, 1, 5, false));
        list.add(new Video("Google Fiber", "re0VRK6ouwI", 0, 1, 5, false));
        list.add(new Video("Autocompleter", "blB_X38YSxQ", 0, 1, 5, false));
        list.add(new Video("GMail Motion", "Bu927_ul_X0", 0, 1, 5, false));
        list.add(new Video("Translate for Animals", "3I24bSteJpw", 0, 1, 5, false));

        return (HomePageObject[]) list.toArray();
    }

}
