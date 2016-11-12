package com.caa.yhack.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caa.yhack.R;
import com.caa.yhack.net.Downloader;
import com.squareup.picasso.Picasso;

/**
 * A view holder which creates a parallax affect within the background image
 * @author Aaron Vontell
 * @version 0.1
 */
public class ViewHolder{

    private View mainView;

    public ViewHolder(Context context, ViewGroup parent, int layoutType){

        super();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(layoutType, parent, false);

    }

    public View getView() {
        return mainView;
    }

    /**
     * Sets the title for this view
     * @param title the title to display
     */
    public void setTitle(String title) {

        ((TextView) mainView.findViewById(R.id.title_view)).setText(title);

    }

    /**
     * Sets the tidbit information for this view
     * @param tidbit the tibit for this view
     */
    public void setTidbit(String tidbit) {

        ((TextView) mainView.findViewById(R.id.tidbit_view)).setText(tidbit);

    }

    public void setBackground(Context context, String id) {

        String url = Downloader.getThumbnailUrl(id);
        ImageView background = (ImageView) mainView.findViewById(R.id.thumbnail);
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.thumbnail_default)
                .error(R.drawable.thumbnail_default)
                .into(background);

    }

}
