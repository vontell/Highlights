package com.caa.yhack;

import android.animation.Animator;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.caa.yhack.spec.HomePageObject;
import com.caa.yhack.util.RevealHelper;
import com.caa.yhack.views.VideoArrayAdapter;
import com.caa.yhack.views.VideoFragment;
import com.caa.yhack.youtube.Video;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView wideList;
    private Context context;
    private VideoFragment videoFragment;
    private float[] lastTouch = new float[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        wideList = (ListView) findViewById(R.id.parallaxListView);

        loadHomeObjects();

        // Attach the video fragment
        videoFragment = new VideoFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.video_screen, videoFragment);
        transaction.commit();

        // Recording touch positions for reveal

        wideList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    lastTouch[0] = event.getX();
                    lastTouch[1] = event.getY();
                }
                return false;
            }
        });


        wideList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                Log.e("CLICKED", "Selected " + pos);

                showVideoScreen(pos, lastTouch[0], lastTouch[1]);

            }
        });

    }

    public void loadHomeObjects() {

        new GetVideos().execute();

    }


    private HomePageObject[] getTestVids() {

        List<HomePageObject> list = new ArrayList<HomePageObject>();
        list.add(new Video("YouTube Collection", "Y_UmWdcTrrc", 0, 1, 5, false));
        list.add(new Video("GMail Tap", "1KhZKNZO8mQ", 0, 1, 5, false));
        list.add(new Video("Chrome Multitask", "UiLSiqyDf4Y", 0, 1, 5, false));
        list.add(new Video("Google Fiber", "re0VRK6ouwI", 0, 1, 5, false));
        list.add(new Video("Autocompleter", "blB_X38YSxQ", 0, 1, 5, false));
        list.add(new Video("GMail Motion", "Bu927_ul_X0", 0, 1, 5, false));
        list.add(new Video("Translate for Animals", "3I24bSteJpw", 0, 1, 5, false));

        return list.toArray(new HomePageObject[0]);

    }

    /**
     * Uses a radial reveal to instantiate and start the video
     */
    public void showVideoScreen(int position, float x, float y) {

        // TODO: Go full screen

        final Animator videoAnimator = RevealHelper.startVideoReveal(this, x, y);
        videoAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // TODO: Start video here!
                videoFragment.videoStart();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    /**
     * Uses a radial animation to hide the video screen
     */
    public void hideVideoScreen(float x, float y) {

        //TODO: Go not full screen

        final Animator recorderAnimator = RevealHelper.startVideoHide(this, x, y);
        recorderAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    private class GetVideos extends AsyncTask<Void, Void, HomePageObject[]> {

        @Override
        protected HomePageObject[] doInBackground(Void ... params) {

            HomePageObject[] homePageObjects = getTestVids();
            return homePageObjects;

        }

        @Override
        protected void onPostExecute(HomePageObject[] result) {
            VideoArrayAdapter adapter = new VideoArrayAdapter(context, result);
            wideList.setAdapter(adapter);
        }

        @Override
        protected void onPreExecute() {}

    }

}
