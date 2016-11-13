package com.caa.yhack;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.caa.yhack.net.Downloader;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    private ListView wideList;
    private View videoView;
    private Context context;
    private float[] lastTouch = new float[2];
    private Video[] currentSelection;
    private YouTubePlayer player;
    YouTubePlayerFragment youTubePlayerFragment;
    private TextView countdownTimer;
    private int noNewCount = 0;
    private boolean shouldCount;

    // FOR USE WITH PLAYBACK
    ArrayList<Integer> starts;
    ArrayList<Integer> lengths;
    ArrayList<String> videos;
    int currentTrack = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        startTimer();

        wideList = (ListView) findViewById(R.id.parallaxListView);
        videoView = findViewById(R.id.video_screen);
        countdownTimer = (TextView) findViewById(R.id.countdown);

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
        } else if (id == R.id.action_refresh) {
            loadHomeObjects();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadHomeObjects() {

        new GetVideosTask().execute();

    }

    private Video[] getTestVids() {

        List<Video> list = new ArrayList<Video>();
        list.add(new Video("YouTube Collection", "Y_UmWdcTrrc", 5000, 10000, 5, false));
        list.add(new Video("GMail Tap", "1KhZKNZO8mQ", 5000, 10000, 5, false));
        list.add(new Video("Chrome Multitask", "UiLSiqyDf4Y", 5000, 10000, 5, false));
        list.add(new Video("Google Fiber", "re0VRK6ouwI", 5000, 10000, 5, false));
        list.add(new Video("Autocompleter", "blB_X38YSxQ", 5000, 10000, 5, false));
        list.add(new Video("GMail Motion", "Bu927_ul_X0", 5000, 10000, 5, false));
        list.add(new Video("Translate for Animals", "3I24bSteJpw", 5000, 10000, 5, false));

        return list.toArray(new Video[0]);

    }

    /**
     * Uses a radial reveal to instantiate and start the video
     */
    public void showVideoScreen(int position, float x, float y) {

        position = ((VideoArrayAdapter) wideList.getAdapter()).getRealPosition(position);
        currentTrack = position;

        // Construct the queue of videos
        videos = new ArrayList<>();
        starts = new ArrayList<>();
        lengths = new ArrayList<>();
        for (int i = position; i < currentSelection.length; i++) {
            videos.add(currentSelection[i].getVideoId());
            Log.e("ID", currentSelection[i].getVideoId());
            starts.add(currentSelection[i].getStartSeek());
            lengths.add(currentSelection[i].getEndSeek() - currentSelection[i].getStartSeek());
        }

        player.loadVideos(videos);
        player.cueVideos(videos);

        queueVideos();

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
        findViewById(R.id.interceptor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideVideoScreen(lastTouch[0], lastTouch[1]);
                player.pause();
            }
        });

    }

    private void removeVideoScreenListener() {
        findViewById(R.id.interceptor).setOnClickListener(new View.OnClickListener() {
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
            playNext(true);
        }
    }

    public void queueVideos() {

        Log.e("QUEUE", "Cueing the vids");

        /*
        int sum = 0;
        for(int i = 0; i < lengths.size(); i++) {

            Integer[] time = {lengths.get(i), starts.get(i)};
            new NextCoordTask().execute(time);
            sum += lengths.get(i) + 700;

        }
        */

        new PlayWaitTask().execute();

    }

    /**
     * Play the next video, unless this is the first video (then play the first video)
     * @param first True if this is the first video
     */
    public void playNext(boolean first){

        Log.e("PLAYING", "Play next, where track is " + currentTrack + " and next is " + player.hasNext());
        if(player.hasNext()) {

            Log.e("PLAY", "WE HAVE NEXT");

            // Pick the track
            if(!first) {
                player.next();
                currentTrack++;
            }

            // Seek to the desired position
            Log.e("TRACK START", "" + starts.get(currentTrack));
            Log.e("TRACK LENGTH", "" + (lengths.get(currentTrack) / 1000.0));

            // Start playing, and set countdown timer
            Log.e("PLAY", "EXECUTING NEXT VID");
            new PlayNextVidStartTask().execute();

            player.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                @Override
                public void onPlaying() {
                    Log.e("ON_PLAYING", "");
                }

                @Override
                public void onPaused() {
                    Log.e("ON_PAUSE", "");
                }

                @Override
                public void onStopped() {
                    Log.e("ON_STOPPED", "");
                }

                @Override
                public void onBuffering(boolean b) {
                    Log.e("ON_BUFFERING", "" + b);
                    shouldCount = !b;
                    if(!b) {
                        new CueNextTrackTask().execute();
                    }

                }

                @Override
                public void onSeekTo(int i) {
                    Log.e("ON_SEEK_TO", "" + i);
                }
            });


        } else {

            player.pause();
            hideVideoScreen(lastTouch[0], lastTouch[1]);

        }

    }

    /**
     * Waits for 700 milliseconds and then plays the video, starting a task to move on afterwards
     */
    private class PlayNextVidStartTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Start playing, and set countdown timer
            Log.e("PLAY", "POST ON PLAY NEXT");
            setCountdown(lengths.get(currentTrack) / 1000);
            player.play();
            player.seekToMillis(starts.get(currentTrack));
        }
    }

    private class CueNextTrackTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(lengths.get(currentTrack));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("PLAY", "POST ON CUE");
            playNext(false);

        }

    }

    public void startTimer() {

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                if(shouldCount) {
                    setCountdown(Math.max(0, Integer.parseInt("" + countdownTimer.getText()) - 1));
                }
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(r, 1000);

    }

    public void setCountdown(int seconds) {

        countdownTimer.setText("" + seconds);

    }

    private class GetVideosTask extends AsyncTask<Void, Void, Void> {

        Video[] videos = null;

        @Override
        protected Void doInBackground(Void... voids) {
            Log.e("BACKEND", "GETTING VIDEOS");
            videos = Downloader.getVideos();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(videos.length != 0) {
                noNewCount = 0;
                if(currentSelection == null) {
                    currentSelection = new Video[0];
                }
                currentSelection = concat(currentSelection, videos);
                if(wideList.getAdapter() == null) {
                    VideoArrayAdapter adapter = new VideoArrayAdapter(context, currentSelection);
                    wideList.setAdapter(adapter);
                } else {
                    ((VideoArrayAdapter) wideList.getAdapter()).notifyDataSetChanged();
                }
                currentSelection = ((VideoArrayAdapter) wideList.getAdapter()).sortedByCategory();
            } else {
                noNewCount++;
                if (noNewCount < 3) {
                    //new GrabFromQueueTask().execute();
                }
            }
        }
    }

    private class GrabFromQueueTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new GetVideosTask().execute();
        }
    }

    public static <T> T[] concat(T[] first, T[] second) {

        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}