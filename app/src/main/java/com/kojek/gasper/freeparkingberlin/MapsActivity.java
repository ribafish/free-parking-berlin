package com.kojek.gasper.freeparkingberlin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.shapes.Shape;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPolygon;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;
import com.kojek.gasper.freeparkingberlin.util.StringSuggestion;

import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class MapsActivity extends AppCompatActivity{
    static final private String TAG = "MapsActivity";
    private GoogleMap mMap;
    private GeoJsonLayer zoneLayer;
    private LatLng touchLatLng;
    private Marker marker;
    MapsActivity instance;
    private MySupportMapFragment mapFragment;

    private BottomSheetBehavior bottomSheetBehavior;
    private TextView bottomSheetTitle;
    private TextView bottomSheetText;
    private FloatingActionButton maps;
    private FloatingActionButton navigate;

    private FloatingSearchView floatingSearchView;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_maps);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        initViews();
        initListeners();

        setupMap();

        setupFloatingSearch();

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        geocoder = new Geocoder(this);
    }

    private void initViews() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheetLayout));
        bottomSheetTitle = (TextView) findViewById(R.id.bottomSheetTitle);
        bottomSheetText = (TextView) findViewById(R.id.bottomSheetText);
        maps = (FloatingActionButton) findViewById(R.id.fab_maps);
        navigate = (FloatingActionButton) findViewById(R.id.fab_navigate);
        floatingSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            try {
                findViewById(R.id.bottomSheetLayout).setElevation(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initListeners() {
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loc = marker.getPosition().latitude + ", " + marker.getPosition().longitude;
                Log.d(TAG, "Go to maps, location: " + loc);
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + loc + "(Park)");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loc = marker.getPosition().latitude + ", " + marker.getPosition().longitude;
                Log.d(TAG, "Navigate to location: " + loc);
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + loc);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

    }

    private void setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        final SupportMapFragment mapFragment = new MySupportMapFragment().newInstance(new GoogleMapOptions().liteMode(true));
        mapFragment = new MySupportMapFragment();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setAllGesturesEnabled(true);

                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException se) {
                    Log.e(TAG, se.getLocalizedMessage());
                }

                // Draw the zones
                try {
                    zoneLayer = new GeoJsonLayer(mMap, R.raw.berlin_zones, instance);
                    GeoJsonPolygonStyle style = zoneLayer.getDefaultPolygonStyle();
                    style.setFillColor(0x22ea6464);
                    style.setStrokeColor(0xFFff0000);
                    style.setStrokeWidth(4);
                    zoneLayer.addLayerToMap();
                    zoneLayer.setOnFeatureClickListener(new GeoJsonLayer.GeoJsonOnFeatureClickListener() {
                        @Override
                        public void onFeatureClick(GeoJsonFeature feature) {
                            try {
                                Log.d(TAG, "Zone clicked: " + feature.getProperty("zone"));
                                dropMarker(feature);
                            } catch (Exception e) {
                                Log.e(TAG, e.getLocalizedMessage());
                            }

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
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Log.d(TAG, "onMapClick, loc: " + latLng);
                        dropMarker(latLng);
                    }
                });
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        Log.d(TAG, "onMapLongClick, loc: " + latLng);
                    }
                });
                mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
//                        Log.d(TAG, "onCameraMoveStarted, reason: " + i);
                        if (i == 1){    // if moved by user
//                            mMap.getUiSettings().setMyLocationButtonEnabled(true);  //enable go to my loc button
//                            Menu menu = ;
                            //TODO
                        }
                    }
                });

            }
        });

        // Show the map fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.map_fragment_container, mapFragment)
                .commit();
    }

    private void setupFloatingSearch() {
        floatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    floatingSearchView.clearSuggestions();
                } else {
                    // TODO: do it asynchronously
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(newQuery, 5);
                        List<StringSuggestion> suggestions = new LinkedList<>();
                        for (Address address : addresses) {
                            suggestions.add(new StringSuggestion(address));
                        }
                        floatingSearchView.swapSuggestions(suggestions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                dropMarker((StringSuggestion) searchSuggestion);
            }

            @Override
            public void onSearchAction(String s) {
                dropMarker(s);
            }
        });
        floatingSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem menuItem) {
                Log.d(TAG, "FloatingSearch item " + menuItem.getTitle());
                switch (menuItem.getItemId()) {
                    case R.id.action_my_location:
                        if (mMap.getCameraPosition().zoom < 17) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(mMap.getCameraPosition().target)
                                    .tilt(mMap.getCameraPosition().tilt)
                                    .zoom(16)
                                    .bearing(mMap.getCameraPosition().bearing)
                                    .build();  //TODO: follow location
                            mMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(cameraPosition));
                        }
                        break;
                    case R.id.action_north_up:
                        if (mMap.getCameraPosition().bearing != 0) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(mMap.getCameraPosition().target)
                                    .tilt(mMap.getCameraPosition().tilt)
                                    .zoom(mMap.getCameraPosition().zoom)
                                    .bearing(0)
                                    .build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        } else { // follow compass bearing
                            //TODO: follow compass bearing
                        }
                        break;
                    case R.id.action_settings:

                        break;
                }
            }
        });
    }

    public void tapEvent(int x, int y) {
        Projection pp = mMap.getProjection();
        touchLatLng = pp.fromScreenLocation(new Point(x, y));
        Log.d(TAG,String.format("tap event x=%d y=%d ",x,y) + touchLatLng);
    }

    private LatLng getLocationFromAddress(String strAddress) {
        //TODO: do it asynchronously

        Geocoder coder = new Geocoder(instance);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private void moveCameraToMarker() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(marker.getPosition())
                .tilt(mMap.getCameraPosition().tilt)
                .zoom(16)
                .bearing(mMap.getCameraPosition().bearing)
                .build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    private void dropMarker(LatLng latLng) {
        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .alpha(1)
                .position(latLng)
        );
        bottomSheetTitle.setText(R.string.zone_free);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        // TODO: find nearest address asynchronously
    }

    private void dropMarker(GeoJsonFeature feature) throws Exception {
        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .alpha(1)
                .position(touchLatLng)
        );
        String title = getString(R.string.zone) + " " +  feature.getProperty("zone");
        bottomSheetTitle.setText(title);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        // TODO: find nearest address asynchronously
    }

    private void dropMarker(StringSuggestion stringSuggestion) {
        LatLng latLng = stringSuggestion.getLatLng();
        if (latLng == null) {
            dropMarker(stringSuggestion.getBody());
        } else {
            if (marker != null) marker.remove();
            marker = mMap.addMarker(new MarkerOptions()
                    .alpha(1)
                    .position(latLng)
            );
            // TODO: check if in zone
            String title = getString(R.string.zone) + " TODO";
            bottomSheetTitle.setText(title);
            bottomSheetText.setText(stringSuggestion.getBody());
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            moveCameraToMarker();
        }
    }

    private void dropMarker(String address) {
        LatLng latLng = getLocationFromAddress(address);

        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .alpha(1)
                .position(latLng)
        );
        // TODO: check if in zone
        String title = getString(R.string.zone) + " TODO";
        bottomSheetTitle.setText(title);
        bottomSheetText.setText(address);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        moveCameraToMarker();
    }

}
