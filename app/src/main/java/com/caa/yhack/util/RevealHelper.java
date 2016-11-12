package com.caa.yhack.util;

import android.animation.Animator;
import android.app.Activity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.caa.yhack.R;

/**
 * Static methods for doing reveal animations on views
 * @author Aaron Vontell
 * @version 0.1
 */
public class RevealHelper {

    /**
     * Starts the animation for reveal the video screen
     * @param activity The calling activity
     * @return the animator for the video screen, for later listener use
     */
    public static Animator startVideoReveal(Activity activity, float x, float y) {

        System.out.println("VIDEO REVEAL STARTED AT POST " + x + "," + y);

        // Get animation info for the video view -----------------------------------------------

        // previously invisible view
        View screen = activity.findViewById(R.id.video_screen);

        // get the final radius for the clipping circle
        int cFinalRadius = (int) Math.hypot(screen.getWidth(), screen.getHeight());
        int cStartRadius = 0;

        // create the animator for this view
        Animator circleAnim =
                ViewAnimationUtils.createCircularReveal(screen, (int) x, (int) y, cStartRadius, cFinalRadius);

        // Start the animations! -------------------------------------------------------------------
        // make the views visible and start the animation
        screen.setVisibility(View.VISIBLE);
        circleAnim.start();

        return circleAnim;

    }

    /**
     * Starts the animation for hiding the video screen
     * @param activity The calling activity
     * @return the animator for the video screen, for later listener use
     */
    public static Animator startVideoHide(Activity activity, float x, float y) {

        // Get animation info for the recording view -----------------------------------------------

        // previously invisible view
        final View screen = activity.findViewById(R.id.video_screen);

        // get the final radius for the clipping circle
        int cStartRadius = (int) Math.hypot(screen.getWidth(), screen.getHeight());
        int cFinalRadius = 0;

        // create the animator for this view
        Animator circleAnim =
                ViewAnimationUtils.createCircularReveal(screen, (int) x, (int) y, cStartRadius, cFinalRadius);

        circleAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                screen.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        // Start the animations! -------------------------------------------------------------------
        // make the views visible and start the animation
        screen.setVisibility(View.VISIBLE);
        circleAnim.start();

        return circleAnim;

    }

}
