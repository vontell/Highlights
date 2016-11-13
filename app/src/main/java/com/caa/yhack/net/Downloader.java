package com.caa.yhack.net;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.caa.yhack.R;
import com.caa.yhack.spec.HomePageObject;
import com.caa.yhack.views.VideoArrayAdapter;
import com.caa.yhack.youtube.Video;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A class that contains methods for downloading content from the internet
 * @author Aaron Vontell
 * @version 0.1
 */

public class Downloader {

    private static final String THUMBNAIL_URL = "https://img.youtube.com/vi/%ID%/0.jpg";
    private static final String VIDEO_URL = "http://66.175.210.39/api/get_ml_data";

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

    /**
     * Retrieve the list of videos that the user should watch
     * @return the missed videos from their subscriptions
     */
    public static Video[] getVideos() {

        try {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(VIDEO_URL)
                    .build();

            Log.e("BACKEND", "About to call server");
            Response response = client.newCall(request).execute();
            Log.e("BACKEND", "Built Call");
            JSONArray result = new JSONArray(response.body().string());
            result = result.getJSONArray(0);
            Log.e("BACKEND", "Got array: " + result.toString());

            Video[] videos = new Video[result.length()];

            for(int i = 0; i < result.length(); i++) {

                JSONObject jVid = result.getJSONObject(i);
                String videoId = jVid.getString("videoId");
                String title = jVid.getString("title");
                int start = jVid.getInt("startSeek");
                int end = jVid.getInt("endSeek");
                int views = 0;
                boolean seen = false;

                Video video = new Video(title, videoId, start, end, views, seen);
                videos[i] = video;

            }

            return videos;

        } catch (Exception e) {

            Log.e("EXCEPTION", e.toString());
            return new Video[0];

        }

    }

}
