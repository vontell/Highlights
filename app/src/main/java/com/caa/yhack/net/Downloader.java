package com.caa.yhack.net;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.caa.yhack.R;
import com.caa.yhack.spec.HomePageObject;
import com.caa.yhack.views.VideoArrayAdapter;
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
     * Attaches thumbnails to the home page object that belongs on the homepage
     * @param context
     * @param object
     * @param imageView
     */
    public static void attachThumbnail(Context context, HomePageObject object, ImageView imageView) {

        String url = THUMBNAIL_URL.replace("%ID%", object.getVideoId());
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.thumbnail_default)
                .error(R.drawable.thumbnail_default)
                .into(imageView);

    }

    public static String getThumbnailUrl(String videoId) {
        return THUMBNAIL_URL.replace("%ID%", videoId);
    }

}
