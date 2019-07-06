package ms.sapientia.ro.gaitrecognitionapp.common;

import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

/**
 * This class contains static methods to create simple animations.
 *
 * @author MilleJanos
 */
public class Animator {

    public static long ANIMATION_DURATION = 300; // milliseconds

    // Directions
    public static int FROM_LEFT = 0;
    public static int FROM_TOP = 1;
    public static int FROM_RIGHT = 2;
    public static int FROM_BOTTOM = 3;
    public static int TO_LEFT = 4;
    public static int TO_TOP = 5;
    public static int TO_RIGHT = 6;
    public static int TO_BOTTOM = 7;


    /**
     * This method adds logo intro animation to view_item.
     * @param view_item to animate
     */
    public static void LogoIntro(View view_item){
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 45,
                MainActivity.sInstance.getResources().getDisplayMetrics()
        );
        // Translate:
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, distanceY, 0);
        translateAnimation.setDuration(ANIMATION_DURATION);

        // Scale
        Animation scaleAnimation = new ScaleAnimation(
                2f, 1f, // Start and end values for the X axis scaling
                2f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        scaleAnimation.setFillAfter(true); // Needed to keep the result of the animation
        scaleAnimation.setDuration(ANIMATION_DURATION);

        // Animation Set: (Translate+Alpha)
        AnimationSet as = new AnimationSet(false);
        as.addAnimation(translateAnimation);
        as.addAnimation(scaleAnimation);

        // Start Animations:
        view_item.setAnimation(as);
    }

    /**
     * This method adds a simple slide animation to the view_item, 'from' and 'to' the given parameter.
     * Animation duration = {@value ANIMATION_DURATION}
     * @param view_item to animate
     * @param fromXDelta from X relative coordinate
     * @param toXDelta to X relative coordinate
     * @param fromYDelta from Y relative coordinate
     * @param toYDelta to Y relative coordinate
     */
    public static void Slide(View view_item, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta ){
         Slide(view_item,  fromXDelta,  toXDelta,  fromYDelta,  toYDelta, ANIMATION_DURATION);
    }

    /**
     * This method adds a simple slide animation to the view_item, 'from' and 'to' the given parameter.
     * Duration of the animation can be set.
     * @param view_item to animate
     * @param fromXDelta from X relative coordinate
     * @param toXDelta to X relative coordinate
     * @param fromYDelta from Y relative coordinate
     * @param toYDelta to Y relative coordinate
     * @param duration animation duration (ms)
     */
    public static void Slide(View view_item, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, long duration){

        // Convert DPI to Pixels
        float fromXDeltaInPixel = DpiToPixelConverter(fromXDelta);
        float toXDeltaInPixel   = DpiToPixelConverter(toXDelta);
        float fromYDeltaInPixel = DpiToPixelConverter(fromYDelta);
        float toYDeltaInPixel   = DpiToPixelConverter(toYDelta);

        // Translate:
        TranslateAnimation translateAnimation = new TranslateAnimation(fromXDeltaInPixel, toXDeltaInPixel, fromYDeltaInPixel, toYDeltaInPixel);
        translateAnimation.setDuration( duration );


        // Animation Set: (Translate+Alpha)
        AnimationSet as = new AnimationSet(false);
        as.addAnimation(translateAnimation);

        // Start Animations:
        view_item.setAnimation(as);
    }

    /**
     * This method converts DPI value to pixels.
     * @param dpi_value in DPI
     * @return DPI value in pixels
     */
    public static float DpiToPixelConverter(float dpi_value){

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpi_value,
                MainActivity.sInstance.getResources().getDisplayMetrics()
        );
    }


}
