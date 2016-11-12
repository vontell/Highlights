package com.caa.yhack.youtube;

import com.caa.yhack.spec.HomePageObject;
import com.yayandroid.parallaxlistview.ParallaxImageView;

/**
 * A representation of a video on YouTube
 * @author Aaron Vontell
 * @version 0.1
 */
public class Video implements HomePageObject {

    private String title;
    private String videoId;
    private int startSeek;
    private int endSeek;
    private int views;
    private boolean seen;
    private ParallaxImageView background;

    /**
     * Creates a video representation that we can retrieve from the YouTube API
     * @param title The title of the video
     * @param videoId The videoId of the YouTube video
     * @param startSeek The beginning seek time of this specific highlight
     * @param endSeek The end seek time of this specific highlight
     * @param views The number of views for this video
     * @param seen True if the user has seen this video before
     */
    public Video(String title, String videoId, int startSeek, int endSeek, int views, boolean seen) {
        this.title = title;
        this.videoId = videoId;
        this.startSeek = startSeek;
        this.endSeek = endSeek;
        this.views = views;
        this.seen = seen;
    }

    @Override
    public ParallaxImageView getBackgroundImage() {
        return background;
    }

    /**
     * Returns the title of this video
     * @return the title of this video
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String getTidbit() {
        return views + " views";
    }

    /**
     * Returns the videoId for this video
     * @return the videoId for this video
     */
    public String getUrl() {
        return videoId;
    }

    /**
     * Returns the start seek time for this video
     * @return the start seek time for this video
     */
    public int getStartSeek() {
        return startSeek;
    }

    /**
     * Returns the end seek time for this video
     * @return the end seek time for tihs video
     */
    public int getEndSeek() {
        return endSeek;
    }

    /**
     * Returns the number of views for this video
     * @return the number of views for this video
     */
    public int getViews() {
        return views;
    }

    /**
     * Returns whether this video has been seen
     * @return whether this video has been seen
     */
    public boolean isSeen() {
        return seen;
    }

    /**
     * Returns the videoId
     * @return te videoId
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Sets whether or not this video has been seen
     * @param seen true if this video was seen, or false otherwise
     */
    public void setSeen(boolean seen){
        this.seen = seen;
    }

    public void setBackground(ParallaxImageView image) {
        this.background = image;
    }

}
