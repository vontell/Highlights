package com.caa.yhack.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caa.yhack.R;
import com.yayandroid.parallaxlistview.ParallaxImageView;
import com.yayandroid.parallaxlistview.ParallaxViewHolder;

/**
 * A view holder which creates a parallax affect within the background image
 * @author Aaron Vontell
 * @version 0.1
 */
public class MainParallaxViewHolder extends ParallaxViewHolder {

    private View mainView;

    public MainParallaxViewHolder(Context context, ViewGroup parent, int layoutType){

        super();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(layoutType, parent, false);

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

    /**
     * Sets the parallax image background for this view
     * @param image The parallax background
     */
    public void setParallaxBackground(ParallaxImageView image) {

        this.setBackgroundImage(image);

    }

}
