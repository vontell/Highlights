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

/**
 * A fragment that will hold and play the YouTube videos
 * @author Aaron Vontell
 * @version 0.1
 */

public class VideoFragment extends Fragment {

    private float[] lastTouch = new float[2];

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

    /**
     * Begins the process to start a video
     */
    public void videoStart() {

        Log.d("RECORDING", "Video started");

    }

}
