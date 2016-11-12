package com.caa.yhack.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.caa.yhack.R;
import com.caa.yhack.net.Downloader;
import com.caa.yhack.spec.HomePageObject;

/**
 * An array adapter for our videos from youtube
 * @author Aaron Vontell
 * @version 0.1
 */
public class VideoArrayAdapter extends ArrayAdapter<HomePageObject> {

    private final Context context;
    private final HomePageObject[] listItems;

    public VideoArrayAdapter(Context context, HomePageObject[] listItems) {
        super(context, -1, listItems);
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder(context, parent, R.layout.home_wide);

        HomePageObject homeObject = listItems[position];

        viewHolder.setBackground(context, homeObject.getVideoId());
        viewHolder.setTitle(homeObject.getTitle());
        viewHolder.setTidbit(homeObject.getTidbit());

        View view = viewHolder.getView();

        return view;

    }

    @Override
    public int getCount() {
        return listItems.length;
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
