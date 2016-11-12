package com.caa.yhack.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.caa.yhack.R;
import com.caa.yhack.spec.HomePageObject;
import com.caa.yhack.youtube.Video;

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

        MainParallaxViewHolder viewHolder = new MainParallaxViewHolder(context, parent, R.layout.home_wide);

        HomePageObject homeObject = listItems[position];
        viewHolder.setParallaxBackground(homeObject.getBackgroundImage());
        viewHolder.setTitle(homeObject.getTitle());
        viewHolder.setTidbit(homeObject.getTidbit());

        viewHolder.getBackgroundImage().reuse();
        convertView.setTag(viewHolder);

        return convertView;

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
