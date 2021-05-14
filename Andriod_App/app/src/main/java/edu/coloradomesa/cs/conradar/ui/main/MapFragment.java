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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;

import edu.coloradomesa.cs.conradar.FirebaseDBHelper;
import edu.coloradomesa.cs.conradar.MainActivity;
import edu.coloradomesa.cs.conradar.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class MapFragment extends Fragment implements GoogleMap.OnMarkerDragListener {

    String TAG = MapFragment.class.getSimpleName();
    private MapView mMapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    FirebaseDBHelper dbHelper = new FirebaseDBHelper();
    FirebaseDatabase db = dbHelper.getDB();

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
    //Since variable is accessed within inner class, global variable is easiest fix :/
    private int geofenceRadius = 0;
    private LatLng geofenceLatLng;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //setup for googlemaps view
        final View view = inflater.inflate(R.layout.fragment_map, null, false);
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
                        final LatLng currentpos = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentpos).zoom(11).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        final DatabaseReference geofenceLat = db.getReference(dbHelper.getRootSTR() + "GeofenceLat");
                        final DatabaseReference geofenceLng = db.getReference(dbHelper.getRootSTR() + "GeofenceLng");
                        final DatabaseReference geofenceRad = db.getReference(dbHelper.getRootSTR() + "GeofenceRad");
                        DatabaseReference rootRf = db.getReference(dbHelper.getRootSTR());

                        rootRf.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.d(TAG, "DATA CHANGE");
                                Log.d(TAG, "geofencelat: " + String.valueOf(snapshot.hasChild("GeofenceLat")));
                                Log.d(TAG, "GeofenceLang: " + String.valueOf(snapshot.hasChild("GeofenceLng")));
                                Log.d(TAG, "GeofenceRad: " + String.valueOf(snapshot.hasChild("GeofenceRad")));

                                if (snapshot.hasChild("GeofenceLat") &&
                                snapshot.hasChild("GeofenceLng") &&
                                snapshot.hasChild("GeofenceRad")){
                                    double lat = snapshot.child("GeofenceLat").getValue(double.class);
                                    double lng  = snapshot.child("GeofenceLng").getValue(double.class);
                                    int georadius =  snapshot.child("GeofenceRad").getValue(Integer.class);
                                    geofenceLatLng = new LatLng(lat,lng);
                                    geofenceRadius = georadius;
                                    addMarker(geofenceLatLng, geofenceRadius);
                                    setSeekBarProgress(georadius / 100, view);
                                }else{
                                    geofenceLat.setValue(currentpos.latitude);
                                    geofenceLng.setValue(currentpos.longitude);
                                    geofenceRad.setValue(1000);
                                    addMarker(currentpos, 1000);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        Log.d(TAG, "latlng: " + geofenceLatLng + "geoRad: "+ geofenceRadius);
                        if(geofenceLatLng != null && geofenceRadius != 0) {
                            ((MainActivity)getActivity()).addGeoFence(geofenceLatLng, geofenceRadius);
                        }else{
                            //addMarker(currentpos,1000);
                        }

                    }
                });

            }
        });

        DatabaseReference geofenceRad = db.getReference(dbHelper.getRootSTR() + "GeofenceRad");
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
                setRadius((seekBar.getProgress() +1)*100);
                DatabaseReference radref = db.getReference(dbHelper.getRootSTR() + "GeofenceRad");
                radref.setValue((seekBar.getProgress() +1) * 100);
                ((MainActivity)getActivity()).addGeoFence(oldCircle.getCenter(), (seekBar.getProgress() +1)*100);


            }
        });
        return view;
    }
    public void setSeekBarProgress(int progress, View view){
        SeekBar radiusBar = (SeekBar)view.findViewById(R.id.seekBar);
        radiusBar.setProgress(progress);
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


        DatabaseReference geofenceLat = db.getReference(dbHelper.getRootSTR() + "GeofenceLat");
        DatabaseReference geofenceLng = db.getReference(dbHelper.getRootSTR() + "GeofenceLng");

        DatabaseReference geofenceRad = db.getReference(dbHelper.getRootSTR() + "GeofenceRad");

        geofenceLat.setValue(latLng.latitude);
        geofenceLng.setValue(latLng.longitude);
        geofenceRad.setValue(radius);


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



        ((MainActivity)getActivity()).addGeoFence(marker.getPosition(), radius);

    }

}