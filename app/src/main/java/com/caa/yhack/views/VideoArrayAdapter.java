package com.caa.yhack.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.caa.yhack.R;
import com.caa.yhack.net.Downloader;
import com.caa.yhack.spec.HomePageObject;
import com.caa.yhack.youtube.Video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * An array adapter for our videos from youtube
 * @author Aaron Vontell
 * @version 0.1
 */
public class VideoArrayAdapter extends ArrayAdapter<HomePageObject> {

    private final Context context;
    private final HomePageObject[] listItems;
    private final HashMap<String, ArrayList<HomePageObject>> cats;
    private final Object[] titles;

    public VideoArrayAdapter(Context context, HomePageObject[] listItems) {
        super(context, -1, listItems);
        this.context = context;
        this.listItems = listItems;
        this.cats = new HashMap<>();


        // Filter the list of highlights by video
        for(HomePageObject obj : listItems) {

            String title = obj.getTitle();
            if(cats.containsKey(title)){
                ArrayList<HomePageObject> array = cats.get(title);
                array.add(obj);
            } else {
                ArrayList<HomePageObject> newList = new ArrayList<>();
                newList.add(obj);
                cats.put(title, newList);
            }

        }

        titles = cats.keySet().toArray();

        for(Object title : titles) {

            for(HomePageObject vid : cats.get((String) title)) {

                Log.e("VIDEO***", vid.toString());

            }

        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder(context, parent, R.layout.home_wide);

        String title = (String) titles[position];
        ArrayList<HomePageObject> highlights = cats.get(title);

        HomePageObject homeObject = highlights.get(0);

        viewHolder.setBackground(context, homeObject.getVideoId());
        viewHolder.setTitle(homeObject.getTitle());
        viewHolder.setTidbit("" + highlights.size() + " highlights");
        viewHolder.setNew(Math.random() < 0.18);

        View view = viewHolder.getView();

        return view;

    }

    public int getRealPosition(int position) {

        int actualPos = 0;
        for(int i = 0; i < position; i++) {

            String title = (String) titles[i];
            actualPos += cats.get(title).size();

        }

        return actualPos;

    }

    public Video[] sortedByCategory() {

        Video[] resultsSorted = new Video[listItems.length];

        int i = 0;
        for(Object title : titles) {

            ArrayList<HomePageObject> cat = cats.get(title);
            for(HomePageObject obj : cat) {
                resultsSorted[i] = (Video) obj;
                i++;
            }

        }

        return resultsSorted;

    }

    @Override
    public int getCount() {
        return cats.size();
    }

    @Override
    public HomePageObject getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
