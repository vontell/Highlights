package com.caa.yhack.net;

import android.content.Context;
import android.provider.Settings;

import com.caa.yhack.R;
import com.caa.yhack.spec.HomePageObject;
import com.squareup.picasso.Picasso;
import com.yayandroid.parallaxlistview.ParallaxImageView;

/**
 * A class that contains methods for downloading content from the internet
 * @author Aaron Vontell
 * @version 0.1
 */

public class Downloader {

    private static final String THUMBNAIL_URL = "https://img.youtube.com/vi/%ID%/0.jpg";

    /**
     * Attaches thumbnails to each object that belongs on the homepage
     * @param objects
     */
    public static void attachThumbnails(Context context, HomePageObject[] objects) {

        for(HomePageObject object : objects) {

            String url = THUMBNAIL_URL.replace("%ID%", object.getVideoId());
            ParallaxImageView imageView = new ParallaxImageView(context);
            Picasso.with(context)
                    .load(url)
                    .resize(300, 300)
                    .centerCrop()
                    .placeholder(R.drawable.thumbnail_default)
                    .error(R.drawable.thumbnail_default)
                    .into(imageView);

            object.setBackground(imageView);

        }

    }

}
