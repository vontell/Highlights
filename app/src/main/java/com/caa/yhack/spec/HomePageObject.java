package com.caa.yhack.spec;

import com.yayandroid.parallaxlistview.ParallaxImageView;

/**
 * An interface that defines methods for getting information
 * about a view on the homepage
 * @author Aaron Vontell
 * @version 0.1
 */
public interface HomePageObject {

    ParallaxImageView getBackgroundImage();
    void setBackground(ParallaxImageView image);
    String getTitle();
    String getTidbit();
    String getVideoId();

}
