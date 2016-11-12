package com.caa.yhack;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.caa.yhack.spec.HomePageObject;
import com.caa.yhack.util.RevealHelper;
import com.caa.yhack.views.VideoArrayAdapter;
import com.caa.yhack.youtube.Video;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    private ListView wideList;
    private View videoView;
    private Context context;
    private float[] lastTouch = new float[2];
    private Video[] currentSelection;
    private YouTubePlayer player;
    YouTubePlayerFragment youTubePlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        wideList = (ListView) findViewById(R.id.parallaxListView);
        videoView = findViewById(R.id.video_screen);

        loadHomeObjects();

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

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    lastTouch[0] = event.getX();
                    lastTouch[1] = event.getY();
                }
                return false;
            }
        });

        youTubePlayerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(getString(R.string.API_KEY), this);

        setWideListListener();
        removeVideoScreenListener();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_login) {
            Intent intent = new Intent(this, WebAuthActivity.class);
            startActivity(intent);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadHomeObjects() {

        new GetVideos().execute();

    }

    private Video[] getTestVids() {

        List<Video> list = new ArrayList<Video>();
        list.add(new Video("YouTube Collection", "Y_UmWdcTrrc", 0, 1, 5, false));
        list.add(new Video("GMail Tap", "1KhZKNZO8mQ", 0, 1, 5, false));
        list.add(new Video("Chrome Multitask", "UiLSiqyDf4Y", 0, 1, 5, false));
        list.add(new Video("Google Fiber", "re0VRK6ouwI", 0, 1, 5, false));
        list.add(new Video("Autocompleter", "blB_X38YSxQ", 0, 1, 5, false));
        list.add(new Video("GMail Motion", "Bu927_ul_X0", 0, 1, 5, false));
        list.add(new Video("Translate for Animals", "3I24bSteJpw", 0, 1, 5, false));

        return list.toArray(new Video[0]);

    }

    /**
     * Uses a radial reveal to instantiate and start the video
     */
    public void showVideoScreen(int position, float x, float y) {

        player.cueVideo(currentSelection[position].getVideoId());
        new PlayWaitTask().execute();

        Log.e("VIDEO", "PLAYING VIDEO");

        final Animator videoAnimator = RevealHelper.startVideoReveal(this, x, y);
        videoAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // TODO: Start video here!
                removeWideListListener();
                setVideoScreenListener();
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
                setWideListListener();
                removeVideoScreenListener();
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

            Video[] homePageObjects = getTestVids();
            currentSelection = homePageObjects;
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

    private void setWideListListener() {
        wideList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                showVideoScreen(pos, lastTouch[0], lastTouch[1]);

            }
        });
    }

    private void removeWideListListener() {
        wideList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            }
        });
    }

    private void setVideoScreenListener() {
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("VIDEO", "Video is clicked");
                hideVideoScreen(lastTouch[0], lastTouch[1]);
            }
        });
    }

    private void removeVideoScreenListener() {
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        this.player = player;
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    private class PlayWaitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            player.play();
        }
    }

}
