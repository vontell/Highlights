package com.caa.yhack.util;

import android.animation.Animator;
import android.app.Activity;
import android.view.ViewAnimationUtils;

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
     *
    public static Animator startVideoReveal(Activity activity) {

        // Get animation info for FAB --------------------------------------------------------------

        // previously invisible view
        final FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);

        // get the center for the fab circle
        int fx = fab.getMeasuredWidth() / 2;
        int fy = fab.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int fStartRadius = Math.max(fab.getWidth(), fab.getHeight()) / 2;
        int fFinalRadius = 0;

        // create the animator for this view
        Animator fabAnim =
                ViewAnimationUtils.createCircularReveal(fab, fx, fy, fStartRadius, fFinalRadius);

        fabAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fab.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        // Get animation info for the recording view -----------------------------------------------

        // previously invisible view
        View screen = activity.findViewById(R.id.recording_screen);

        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        int cx = screen.getWidth() - fx - marginParams.leftMargin;
        int cy = screen.getHeight() - fy - marginParams.bottomMargin;

        // get the final radius for the clipping circle
        int cFinalRadius = (int) Math.hypot(screen.getWidth(), screen.getHeight());
        int cStartRadius = 0;

        // create the animator for this view
        Animator circleAnim =
                ViewAnimationUtils.createCircularReveal(screen, cx, cy, cStartRadius, cFinalRadius);

        // Start the animations! -------------------------------------------------------------------
        // make the views visible and start the animation
        fab.setVisibility(View.VISIBLE);
        screen.setVisibility(View.VISIBLE);
        fabAnim.start();
        circleAnim.start();

        return circleAnim;

    }

    /**
     * Starts the animation for hiding the video screen
     * @param activity The calling activity
     * @return the animator for the video screen, for later listener use
     *
    public static Animator startVideoHide(Activity activity) {

        // Get animation info for FAB --------------------------------------------------------------

        // previously invisible view
        final FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);

        // get the center for the fab circle
        int fx = fab.getMeasuredWidth() / 2;
        int fy = fab.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int fFinalRadius = Math.max(fab.getWidth(), fab.getHeight()) / 2;
        int fStartRadius = 0;

        // create the animator for this view
        final Animator fabAnim =
                ViewAnimationUtils.createCircularReveal(fab, fx, fy, fStartRadius, fFinalRadius);

        fabAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        // Get animation info for the recording view -----------------------------------------------

        // previously invisible view
        final View screen = activity.findViewById(R.id.recording_screen);

        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        int cx = screen.getWidth() - fx - marginParams.rightMargin;
        int cy = screen.getHeight() - fy - marginParams.bottomMargin;

        // get the final radius for the clipping circle
        int cStartRadius = (int) Math.hypot(screen.getWidth(), screen.getHeight());
        int cFinalRadius = 0;

        // create the animator for this view
        Animator circleAnim =
                ViewAnimationUtils.createCircularReveal(screen, cx, cy, cStartRadius, cFinalRadius);

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
        fab.setVisibility(View.VISIBLE);
        circleAnim.start();
        fabAnim.start();

        return circleAnim;

    }
    */

}
