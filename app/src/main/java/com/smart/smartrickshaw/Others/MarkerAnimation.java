package com.smart.smartrickshaw.Others;

import android.animation.ValueAnimator;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerAnimation {
    private static GoogleMap mMap;
    private static final String TAG = "MarkerAnimation";
    private static float getBearings(LatLng startLocation, LatLng newPos) {
        double lat = Math.abs(startLocation.latitude - newPos.latitude);
        double lng = Math.abs(startLocation.longitude - newPos.longitude);

        if (startLocation.latitude < newPos.latitude && startLocation.longitude < newPos.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (startLocation.latitude >= newPos.latitude && startLocation.longitude < newPos.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (startLocation.latitude >= newPos.latitude && startLocation.longitude >= newPos.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (startLocation.latitude < newPos.latitude && startLocation.longitude >= newPos.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }
    public static void animateMarkerToGB(final GoogleMap mMap, final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        MarkerAnimation.mMap = mMap;
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 2000;
        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;
            @Override
            public void run() {
                // Calculate progress using interpolator

//                if(!startPosition.equals(finalPosition)) {
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);
//                marker.setAnchor(0.5f, 0.5f);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));


                    // Repeat till progress is complete.
                    if (t < 1) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
              //  }
            }
        });
    }
}
