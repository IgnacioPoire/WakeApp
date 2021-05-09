package com.wakeapp.ui.maps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wakeapp.R;
import com.wakeapp.VariableInterface;
import com.wakeapp.models.alarms.GeoAlarm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsFragment extends Fragment {

    private VariableInterface varListener;
    private Geocoder geocoder;

    private MapView mMapView;
    private static GoogleMap mMap;
    private Intent intent;

    private static Marker userMarker;
    private Marker marker;
    private Circle circle;
    private List<Address> addresses;

    private FloatingActionButton searchButton;
    private SeekBar radiusBar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VariableInterface) {
            varListener = (VariableInterface) context;
            geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement YourActivityInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        varListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        radiusBar = rootView.findViewById(R.id.radiusBar);

        mMapView = rootView.findViewById(R.id.map_home);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (!varListener.getGeoAlarmList().isEmpty()) {
                    retrieveMarkers();
                }

                // Add a marker in Buenos Aires and move the camera
                LatLng buenosAires = new LatLng(-34.6, -58.38);
                marker = mMap.addMarker(new MarkerOptions().position(buenosAires).title("Marker in Buenos Aires"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buenosAires, 12));
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng arg0) {
                        setMarker(arg0.latitude, arg0.longitude);
                        searchButton.hide();
                        radiusBar.setProgress(0);
                        radiusBar.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        String apiKey = getString(R.string.google_maps_key);
        Places.initialize(getActivity().getApplicationContext(), apiKey);
        searchButton = rootView.findViewById(R.id.searchButton);
        intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)).build(getActivity().getApplicationContext());
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(intent, 1);
            }
        });

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                circle.setRadius(progress * 5 + 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                varListener.getGeoAlarmList().add(new GeoAlarm(addresses.get(0).getAddressLine(0), marker.getPosition(), circle.getRadius()));
                try {
                    checkFileExists();
                    File alarmFile = new File(getActivity().getExternalFilesDir(null) + "/geoalarms.txt");
                    FileOutputStream fos = new FileOutputStream(alarmFile);
                    ObjectOutputStream os = new ObjectOutputStream(fos);
                    os.writeObject(varListener.getGeoAlarmList());
                    os.close();
                    fos.close();
                    System.out.print("SAVED " +  varListener.getGeoAlarmList());
                } catch (FileNotFoundException e) {
                    System.out.println("No file found saveChanges");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("IOException in SaveChanges");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                varListener.updateListenerGeoAlarms();
                searchButton.show();
                radiusBar.setVisibility(View.GONE);
            }
        });

        return rootView;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        LatLng position = Autocomplete.getPlaceFromIntent(data).getLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        setMarker(position.latitude, position.longitude);
        searchButton.hide();
        radiusBar.setProgress(0);
        radiusBar.setVisibility(View.VISIBLE);
        /*if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }*/
    }

    void setMarker(double lat, double lng) {
        MarkerOptions options = new MarkerOptions()                 // This MarkerOptions object is needed to add a marker.
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.alarm_marker_40))      // Here it is possible to specify custom icon design.
                .position(new LatLng(lat, lng));
        marker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
        circle = drawCircle(new LatLng(lat, lng));
    }

    private Circle drawCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(50)
                .fillColor(0x33FCBA03)              // 33 for alpha (transparency) #fcba03
                .strokeColor(Color.BLACK)
                .strokeWidth(3);
        return mMap.addCircle(circleOptions);
    }

    private Circle drawCircle(LatLng latLng, double radius) {
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .fillColor(0x33FCBA03)              // 33 for alpha (transparency) #fcba03
                .strokeColor(Color.BLACK)
                .strokeWidth(3);
        return mMap.addCircle(circleOptions);
    }

    private void retrieveMarkers() {
        ArrayList<GeoAlarm> geoAlarms = varListener.getGeoAlarmList();
        for (int i = 0; i< geoAlarms.size(); i++) {
            double lat = geoAlarms.get(i).getLatLng().latitude;
            double lng = geoAlarms.get(i).getLatLng().longitude;
            MarkerOptions options = new MarkerOptions()                 // This MarkerOptions object is needed to add a marker.
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.alarm_marker_40))      // Here it is possible to specify custom icon design.
                    .position(new LatLng(lat, lng));
            mMap.addMarker(options);
            drawCircle(new LatLng(lat, lng), geoAlarms.get(i).getRadius());
        }
    }

    private void clearMarker() {
        /*if (marker != null) {
            marker.remove(); // remove from map
            marker = null;
        }
        if (circle != null) {
            circle.remove();
            circle = null;
        }*/
    }

    private void checkFileExists() {
        File alarmFile = new File(getActivity().getExternalFilesDir(null) + "/geoalarms.txt");
        try {
            if(!alarmFile.exists()) {
                alarmFile.getParentFile().mkdirs();
                alarmFile.createNewFile();
                FileOutputStream oFile = new FileOutputStream(alarmFile, true);
                oFile.close();
            }
        } catch (IOException e) {
            System.out.println("IOException in checkFileExists");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setMapMarker(Location location){
        MarkerOptions options = new MarkerOptions()                 // This MarkerOptions object is needed to add a marker.
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_marker))      // Here it is possible to specify custom icon design.
                .position(new LatLng(location.getLatitude(), location.getLongitude()));
        userMarker = mMap.addMarker(options);
    }
}