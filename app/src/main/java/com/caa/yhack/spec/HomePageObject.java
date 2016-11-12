package com.caa.yhack.spec;

import android.widget.ImageView;

/**
 * An interface that defines methods for getting information
 * about a view on the homepage
 * @author Aaron Vontell
 * @version 0.1
 */
public interface HomePageObject {

    ImageView getBackgroundImage();
    void setBackground(ImageView image);
    String getTitle();
    String getTidbit();
    String getVideoId();

}
