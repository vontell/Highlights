package com.caa.yhack.views;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.caa.yhack.MainActivity;
import com.caa.yhack.R;
import com.caa.yhack.youtube.Video;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * A fragment that will hold and play the YouTube videos
 * @author Aaron Vontell
 * @version 0.1
 */

public class VideoFragment extends Fragment {

    private float[] lastTouch = new float[2];
    private Video[] videos = null;
    private int videoIndex = 0;
    private YouTubePlayerFragment youTubePlayerFragment;
    private YouTubePlayer yplayer;

    /**
     * Creates a new video fragment
     */
    public VideoFragment() {

    }

    /**
     * Loads the video view from the layout 'fragment_video'
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return The video screen
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout videoScreen = (RelativeLayout)
                inflater.inflate(R.layout.video_fragment, null);

        videoScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    lastTouch[0] = event.getX();
                    lastTouch[1] = event.getY();
                }
                return false;
            }
        });

        videoScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).hideVideoScreen(lastTouch[0], lastTouch[1]);
            }
        });

        return videoScreen;

    }

    public void loadVideos(Video[] videos) {
        this.videos = videos;
        youTubePlayerFragment =
                (YouTubePlayerFragment) getChildFragmentManager().findFragmentById(R.id.video_container);
        youTubePlayerFragment.initialize(getString(R.string.API_KEY), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                yplayer = youTubePlayer;
                nextVideo();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
    }

    /**
     * Begins the process to start a video
     */
    public void videoStart( ) {


    }

    public void nextVideo() {

        //player.release();

        if(videoIndex >= videos.length) {
            ((MainActivity) getActivity()).hideVideoScreen(lastTouch[0], lastTouch[1]);
        }

        Video nextVid = videos[videoIndex];
        videoIndex += 1;
        Log.e("VIDEO", "Cueing next video");
        yplayer.cueVideo(nextVid.getVideoId());

        Log.e("VIDEO", "Playing next video");
        yplayer.play();

    }

}
