package com.wakeapp.ui.maps;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wakeapp.R;
import java.util.Arrays;

public class MapsFragment extends Fragment {

    MapView mMapView;
    private GoogleMap mMap;
    private Marker m;
    private Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

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

                // Add a marker in Buenos Aires and move the camera
                LatLng buenosAires = new LatLng(-34.6, -58.38);
                m = mMap.addMarker(new MarkerOptions().position(buenosAires).title("Marker in Buenos Aires"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(buenosAires));
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng arg0) {
                        m.setPosition(arg0);
                    }
                });
            }
        });

        String apiKey = getString(R.string.google_maps_key);
        Places.initialize(getActivity().getApplicationContext(), apiKey);
        FloatingActionButton searchButton = rootView.findViewById(R.id.searchButton);
        intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)).build(getActivity().getApplicationContext());
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(intent, 1);
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
        m.setPosition(position);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
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

}