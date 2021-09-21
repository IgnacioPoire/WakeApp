package com.wakeapp.ui.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;

public class MapsFragment extends Fragment {

    private VariableInterface varListener;
    private Geocoder geocoder;
    private final Handler handler = new Handler();
    private static final int LOCATION_REFRESH_TIME = 1000;

    private static GoogleMap mMap;
    private MapView mMapView;
    private Intent intent;

    private Marker userMarker;
    private Marker marker;
    private Circle circle;
    private List<Address> addresses;

    private FloatingActionButton searchButton;
    private SeekBar radiusBar;
    private TextView radiusBarScore;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {
                        LatLng position = Autocomplete
                                .getPlaceFromIntent(result.getData()).getLatLng();

                        if (position != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                            setMarker(position.latitude, position.longitude);
                            searchButton.hide();
                            radiusBar.setProgress(0);
                            radiusBar.setVisibility(View.VISIBLE);
                            radiusBarScore.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof VariableInterface) {
            varListener = (VariableInterface) context;
            geocoder = new Geocoder(requireContext(), Locale.getDefault());
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement VariableInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        varListener = null;
        geocoder = null;
    }

    private class TimerTaskToGetUserLocation extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (varListener != null) {
                        Location location = varListener.getUserLocation();
                        if (location != null) {
                            if (userMarker == null) {
                                setUserMarker(location);
                            } else {
                                System.out.println("Usermarker Updated");
                                updateUserMarker(location);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        radiusBar = rootView.findViewById(R.id.radiusBar);
        radiusBarScore = rootView.findViewById(R.id.radiusBarScore);
        searchButton = rootView.findViewById(R.id.searchButton);
        mMapView = rootView.findViewById(R.id.map_home);

        Timer timer = new Timer();
        timer.schedule(new TimerTaskToGetUserLocation(), 250, LOCATION_REFRESH_TIME);

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(requireContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                setMapStyle();

                if (varListener != null) {
                    setUserMarker(varListener.getUserLocation());

                    if (!varListener.getGeoAlarmList().isEmpty()) {
                        retrieveMarkers();
                    }
                }

                if (userMarker != null) {
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(userMarker.getPosition(), 12));
                }

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng pos) {
                        if (radiusBar.getVisibility() == View.GONE) {
                            setMarker(pos.latitude, pos.longitude);
                            searchButton.hide();
                            radiusBar.setProgress(0);
                            radiusBar.setVisibility(View.VISIBLE);
                            radiusBarScore.setVisibility(View.VISIBLE);
                        } else {
                            if (marker != null) {
                                radiusBar.setVisibility(View.GONE);
                                radiusBarScore.setVisibility(View.GONE);
                                searchButton.show();
                                marker.remove();
                                circle.remove();
                            }
                        }
                    }
                });
            }
        });

        String apiKey = getString(R.string.google_maps_key);
        Places.initialize(requireContext(), apiKey);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSomeActivityForResult();
            }
        });

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int range = progress * 7 + 50;
                circle.setRadius(range);
                radiusBarScore.setText(range + " m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    addresses = geocoder.getFromLocation(
                            marker.getPosition().latitude,
                            marker.getPosition().longitude,
                            1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                varListener.getGeoAlarmList().add(
                        new GeoAlarm(addresses
                                .get(0)
                                .getAddressLine(0),
                                marker.getPosition(),
                                circle.getRadius())
                );

                try {
                    checkFileExists();
                    File alarmFile = new File(requireActivity()
                            .getExternalFilesDir(null) + "/geoalarms.txt");
                    FileOutputStream fos = new FileOutputStream(alarmFile);
                    ObjectOutputStream os = new ObjectOutputStream(fos);
                    os.writeObject(varListener.getGeoAlarmList());
                    os.close();
                    fos.close();
                    System.out.print("SAVED " + varListener.getGeoAlarmList());
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
                radiusBar.setVisibility(View.GONE);
                radiusBarScore.setVisibility(View.GONE);
                searchButton.show();
            }
        });

        return rootView;
    }

    public void openSomeActivityForResult() {
        intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                Arrays.asList(Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.NAME)
        ).build(requireContext());
        activityResultLauncher.launch(intent);
    }

    private void setMapStyle() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        String styleType = sp.getString("styleType", "standard");
        if (mMap != null) {
            MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    getResources().getIdentifier(
                            styleType, "raw",
                            requireActivity().getPackageName()
                    )
            );
            mMap.setMapStyle(mapStyleOptions);
        }
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        setMapStyle();
        super.onResume();
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

    void setMarker(double lat, double lng) {
        MarkerOptions options = new MarkerOptions()
                .draggable(true)
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_alarm_marker))
                .position(new LatLng(lat, lng));
        marker = mMap.addMarker(options);
        circle = drawCircle(new LatLng(lat, lng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
    }

    private Circle drawCircle(LatLng latLng) {
        int color = ColorUtils.setAlphaComponent(
                getThemecolorButtonNormal(requireContext()), 77);
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(50)
                .fillColor(color)
                .strokeColor(getThemeAccentColor(requireContext()))
                .strokeWidth(3);
        return mMap.addCircle(circleOptions);
    }

    private void drawCircle(LatLng latLng, double radius) {
        int color = ColorUtils.setAlphaComponent(
                getThemecolorButtonNormal(requireContext()), 77);
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .fillColor(color)
                .strokeColor(getThemeAccentColor(requireContext()))
                .strokeWidth(3);
        mMap.addCircle(circleOptions);
    }

    public static int getThemeAccentColor (final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme ().resolveAttribute (R.attr.colorAccent, value, true);
        return value.data;
    }


    public static int getThemecolorButtonNormal (final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme ().resolveAttribute (R.attr.colorButtonNormal, value, true);
        return value.data;
    }

    private void retrieveMarkers() {
        ArrayList<GeoAlarm> geoAlarms = varListener.getGeoAlarmList();
        for (int i = 0; i < geoAlarms.size(); i++) {
            double lat = geoAlarms.get(i).getLatLng().latitude;
            double lng = geoAlarms.get(i).getLatLng().longitude;
            MarkerOptions options = new MarkerOptions()
                    .draggable(false)
                    .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_alarm_marker))
                    .position(new LatLng(lat, lng));
            mMap.addMarker(options);
            drawCircle(new LatLng(lat, lng), geoAlarms.get(i).getRadius());
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(
            Context context,
            @DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        if (vectorDrawable != null) {
            vectorDrawable.setBounds(
                    0,
                    0,
                    vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(
                    vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return null;
    }

    private void checkFileExists() {
        File alarmFile = new File(requireActivity()
                .getExternalFilesDir(null) + "/geoalarms.txt");
        try {
            if (!alarmFile.exists()) {
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

    private void setUserMarker(Location location){
        if (location != null) {
            MarkerOptions options = new MarkerOptions()
                    .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_user_marker))
                    .position(new LatLng(location.getLatitude(), location.getLongitude()));
            userMarker = mMap.addMarker(options);
            assert userMarker != null;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 12));
        }
    }

    private void updateUserMarker(Location location){
        if (location != null) {
            userMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }
}