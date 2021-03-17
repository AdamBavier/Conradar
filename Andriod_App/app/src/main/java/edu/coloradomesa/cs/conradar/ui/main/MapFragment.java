package edu.coloradomesa.cs.conradar.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.List;

import edu.coloradomesa.cs.conradar.MainActivity;
import edu.coloradomesa.cs.conradar.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class MapFragment extends Fragment implements GoogleMap.OnMarkerDragListener {


    private MapView mMapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    Circle oldCircle = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Marker crclMarker;
    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("ConRadar","YES");
                    return;
                }
                Log.d("ConRadar", "NO");
                googleMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) getContext(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null){
                            return;
                        }
                        LatLng currentpos = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentpos).zoom(11).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        addMarker(currentpos, 1000);

                    }
                });

            }
        });
        SeekBar radiusBar = (SeekBar)view.findViewById(R.id.seekBar);
        // perform seek bar change listener event used for getting the progress value
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("yes", "it do :" + seekBar.getProgress());
                //addCircle(oldCircle.getCenter(), seekBar.getProgress() * 100);
                setRadius(seekBar.getProgress()*100);
            }
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    public void addMarker(LatLng latLng, int radius){
        crclMarker = googleMap.addMarker(new MarkerOptions()
            .position(latLng)
            .draggable(true)
            .title("geofence")
            .snippet("Lat")
            );
        googleMap.setOnMarkerDragListener(this);
        CircleOptions crcl = new CircleOptions()
                .center(latLng)
                .radius(radius);
       oldCircle = googleMap.addCircle(crcl);
    }
    public Circle addCircle(LatLng latLng, int radius){
        CircleOptions crcl = new CircleOptions()
                .center(latLng)
                .radius(radius);
        Circle circle = googleMap.addCircle(crcl);
        return circle;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }
    public void setRadius(int radius){

        if (oldCircle !=null){
            LatLng center = oldCircle.getCenter();
            oldCircle.remove();
            oldCircle = null;
        }
        oldCircle = addCircle(crclMarker.getPosition(), radius);
    }
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 100002;

    @Override
    public void onMarkerDragEnd(Marker marker) {
        float radius = 1000;
        if (oldCircle !=null){
            radius = (float) oldCircle.getRadius();
            oldCircle.remove();
            oldCircle = null;
        }
        LatLng pos = marker.getPosition();
        oldCircle = addCircle(pos, (int)radius);
        /*
        if(Build.VERSION.SDK_INT >= 29){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }
            else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        }
        */

        ((MainActivity)getActivity()).addGeoFence(marker.getPosition(), radius);

    }
    public void setGeoFence(List<Geofence> geofenceList){
        Log.d("yes", String.valueOf(oldCircle.getCenter().latitude));

        Geofence fence = new Geofence.Builder()
               .setRequestId("idk")
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(oldCircle.getCenter().latitude,
                        oldCircle.getCenter().longitude,
                        (float)oldCircle.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

    }
}