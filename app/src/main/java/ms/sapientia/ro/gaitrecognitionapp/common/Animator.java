package ms.sapientia.ro.gaitrecognitionapp.common;

import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

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


    public static void Slide(View view_item, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta ){
         Slide(view_item,  fromXDelta,  toXDelta,  fromYDelta,  toYDelta, ANIMATION_DURATION);
    }

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


    public static float DpiToPixelConverter(float value){

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value,
                MainActivity.sInstance.getResources().getDisplayMetrics()
        );
    }


}
