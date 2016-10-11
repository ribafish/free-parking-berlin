package com.kojek.gasper.freeparkingberlin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.messaging.SendException;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPolygon;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity{
    static final private String TAG = "MapsActivity";
    private GoogleMap mMap;
    MapsActivity instace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instace = this;
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
        final SupportMapFragment mapFragment = new SupportMapFragment();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);

                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException se) {
                    Log.e(TAG, se.getLocalizedMessage());
                }

                // Draw the zones
//                HashMap<Integer, Zone> zones = new Zones().getZones();
//                for (Map.Entry<Integer, Zone> entry : zones.entrySet()) {
//                    Integer key = entry.getKey();
//                    Zone zone = entry.getValue();
//                    mMap.addPolygon(new PolygonOptions()
//                            .addAll(zone.getPoints())
//                            .strokeColor(0xFFffff00)
//                            .fillColor(0x22ffff00)
//                            .strokeWidth(4)
//                    );
//                }
                try {
                    GeoJsonLayer layer = new GeoJsonLayer(mMap, R.raw.berlin_zones, instace);
                    GeoJsonPolygonStyle style = layer.getDefaultPolygonStyle();
                    style.setFillColor(0x22ea6464);
                    style.setStrokeColor(0xFFff0000);
                    style.setStrokeWidth(4);
                    layer.addLayerToMap();
                    layer.setOnFeatureClickListener(new GeoJsonLayer.GeoJsonOnFeatureClickListener() {
                        @Override
                        public void onFeatureClick(GeoJsonFeature feature) {
                            Log.d(TAG, "Zone clicked: " + feature.getProperty("zone"));
                            Toast.makeText(instace,
                                    "Zone clicked: " + feature.getProperty("zone"),
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "GeoJSON file could not be read");
                } catch (JSONException e) {
                    Log.e(TAG, "GeoJSON file could not be converted to a JSONObject");
                }

                // Move the camera to overview of Berlin
                LatLng berlin = new LatLng(52.5172, 13.3887);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(berlin).zoom(11.2f).build();
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

                //Listeners
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        Log.d(TAG, "onMapLongClick, loc: " + latLng);
                    }
                });
                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        Log.d(TAG, "onMyLocationButtonClick");
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);

                        // Move to user and ALWAYS zoom
                        if (mMap.getCameraPosition().zoom < 17) {
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(mMap.getCameraPosition().target).zoom(17).build();  //TODO
                            mMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(cameraPosition));
                        }
                        return true;
                    }
                });
                mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
//                        Log.d(TAG, "onCameraMoveStarted, reason: " + i);
                        if (i == 1){    // if moved by user
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);  //enable go to my loc button
                        }
                    }
                });

            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.map_fragment_container, mapFragment)
                .commit();
    }

}
