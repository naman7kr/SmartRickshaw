package com.smart.smartrickshaw.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.smart.smartrickshaw.Others.DirectionsJSONParser;
import com.smart.smartrickshaw.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FindRidesFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker marker;
    private Location currentLocation = null,startLocation = null;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private BottomSheetBehavior bottomSheetBehavior;
    private Button bookRideBtn;
    //constants

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String TAG = "FindRidesFragment";
    private static final float zoom = 15f;

    //vars
    private boolean mPermissionGranted = false;
    private boolean firstTimeFlag=true;
    private boolean isToggleSlider = false;

    //widgets



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_rides, container, false);
        init(view);
        initMap();
        setSliderCallback();
        onBookRide();
        return view;
    }
    void init(View v){
        bottomSheetBehavior = BottomSheetBehavior.from(v.findViewById(R.id.bottom_sheet_layout));
        bookRideBtn = v.findViewById(R.id.book_ride_button);
    }
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void onBookRide(){
        bookRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: get data
                //TODO: if data found set isToggleSlider = true
                isToggleSlider = true;
            }
        });
    }
    private void setSliderCallback(){
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_DRAGGING:
                        if(isToggleSlider){

                        }else{
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


//



    private void moveCamera(LatLng latLng, float zoom, String title){
       // Log.e(TAG, "moveCamera: moving the camera to lat:"+latLng.latitude+" long: "+latLng.longitude);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
       // mMap.addMarker(options);


    }


    @Override
    public void onResume() {
        super.onResume();
        if(isGoogleServiceAvailable()){
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            //TODO: get location update from server
            startCurrentLocationUpdate();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getLocationPermission();

        //Download path data
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute("https://maps.googleapis.com/maps/api/directions/json?origin=22.776928,86.145042&destination=22.779281,86.163003&key=AIzaSyBWVqdc-u4AMuF8JPxMIab4titcOeVE9WY");
    }
    class DownloadTask extends AsyncTask<String, Void, String>{
        private static final String TAG = "DownloadTask";
        @Override
        protected String doInBackground(String... url) {
            String data="";
            try{
                data = downloadUrl(url[0]);
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while((line = br.readLine()) != null){
                    sb.append(line);
                }
                data = sb.toString();

                br.close();
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap point = path.get(j);

                    double lat = Double.parseDouble((String) point.get("lat"));
                    double lng = Double.parseDouble((String) point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }
// Drawing polyline in the Google Map for the i-th route
            if(lineOptions!=null) {
                mMap.addPolyline(lineOptions);
                moveCamera(points.get(0),zoom,"Marked Path");
            }
            else{
                Log.e(TAG,result.toString());
            }
        }
    }

    void startCurrentLocationUpdate(){
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(3000);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
        mFusedLocationProviderClient.requestLocationUpdates(request,mLocationCallback,Looper.myLooper());
    }
    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.e(TAG, String.valueOf(currentLocation));
            currentLocation = locationResult.getLocations().get(0);
            if (locationResult == null)
                return;

            LatLng latLng= new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
            if (firstTimeFlag && mMap != null) {
                firstTimeFlag = false;
              //  moveCamera(latLng,zoom,"Current Location");
            }
            showMarker(latLng);
        }
    };
    void showMarker(@NonNull LatLng latLng){

        if(marker==null){
            marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));
        }else{
            marker.setPosition(latLng);
            //MarkerAnimation.animateMarkerToGB(mMap,marker,latLng, new LatLngInterpolator.Spherical());
        }
    }
    public void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COURSE_LOCATION};
        if (ContextCompat.checkSelfPermission(getContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0){
                    for(int i = 0;i<grantResults.length;i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mPermissionGranted = false;
                            return;
                        }
                    }
                    mPermissionGranted = true;
                    //init
                    initMap();
                }
        }

    }
    private boolean isGoogleServiceAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(getContext());
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(getContext(), "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
//start = 22.776928,86.145042
//end = 22.779281,86.163003