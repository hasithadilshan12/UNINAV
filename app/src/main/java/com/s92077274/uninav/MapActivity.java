package com.s92077274.uninav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.s92077274.uninav.models.MapPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener
{

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_MAP = 1;

    private TextView tvSearchHint;
    private ImageView btnReCenter;
    private LinearLayout navHome, navSearch, navMap, navProfile;

    private List<MapPoint> mapPoints;
    private Map<String, MapPoint> markerMapPointMap;
    private LatLng currentUserLatLng;

    private BottomSheetDialog bottomSheetDialog;
    private MapPoint selectedDestinationPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        initMapPoints();
        setClickListeners();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        requestLocationPermission();
    }

    private void initViews() {
        tvSearchHint = findViewById(R.id.tvSearchHint);
        btnReCenter = findViewById(R.id.btnReCenter);
        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setClickListeners() {
        tvSearchHint.setOnClickListener(v -> {
            Intent searchIntent = new Intent(MapActivity.this, SearchActivity.class);
            startActivity(searchIntent);
        });

        btnReCenter.setOnClickListener(v -> {
            if (googleMap != null) {
                if (currentUserLatLng != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLatLng, 17f));
                    Toast.makeText(this, "Centered on your location", Toast.LENGTH_SHORT).show();
                } else {
                    LatLng ouslCampusCenter = new LatLng(6.883019826740543, 79.88670615788185);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ouslCampusCenter, 15f));
                    Toast.makeText(this, "Current location not available, centered on OUSL", Toast.LENGTH_LONG).show();
                }
            }
        });

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, HomeActivity.class));
            finish();
        });
        navSearch.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, SearchActivity.class));
            finish();
        });
        navMap.setOnClickListener(v -> {
            Toast.makeText(MapActivity.this, "Already on Map page", Toast.LENGTH_SHORT).show();
        });
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, ProfileActivity.class));
            finish();
        });
    }


    private void initMapPoints() {
        mapPoints = new ArrayList<>();
        markerMapPointMap = new HashMap<>();

        mapPoints.add(new MapPoint("Public Information Office ,Financial Office", "Financial Office", 6.883387838930316f, 79.88654971785698f, "facilities"));
        mapPoints.add(new MapPoint("Library", "Central Library", 6.886341603335691f, 79.88289203571986f, "academic"));
        mapPoints.add(new MapPoint("CRC Office", "Colombo regional Center", 6.88347942072658f, 79.88664655562894f, "office"));
        mapPoints.add(new MapPoint("Student Registration Office", "Registration Center", 6.883196206906299f, 79.88654142667893f, "academic"));
        mapPoints.add(new MapPoint("Industry Liaison Center", "Academic Center", 6.88297452233968f, 79.8865736131866f, "academic"));
        mapPoints.add(new MapPoint("Student Information Center", "Information Center", 6.882981845254872f, 79.88648107697742f, "academic"));
        mapPoints.add(new MapPoint("Cafeteria 1", "Student Dining Hall", 6.882640917449825f, 79.88512860668303f, "food"));
        mapPoints.add(new MapPoint("Cafeteria 2", "Student Dining Hall", 6.887295672388694f, 79.88092240982309f, "food"));
        mapPoints.add(new MapPoint("Toilet 1", "Restroom Facilities(Block 7)", 6.8835583183688565f, 79.88520893476854f, "facilities"));
        mapPoints.add(new MapPoint("Main Entrance", "University Main Gate Nawala", 6.882894376548958f, 79.88676273457729f, "entrance"));
        mapPoints.add(new MapPoint("Security Room", "Security Room", 6.882941399855268f, 79.88669958170006f, "facilities"));
        mapPoints.add(new MapPoint("Toilet 2", "Restroom Facilities(Library)", 6.886109088517432f, 79.88283042826042f, "facilities"));
        mapPoints.add(new MapPoint("Neo Space Lab OUSL", "Space Lab", 6.883157687598526f, 79.88631782459649f, "facilities"));
        mapPoints.add(new MapPoint("People's Bank ATM", "ATM machine", 6.882820264162557f, 79.88592024839622f, "facilities"));
        mapPoints.add(new MapPoint("Bank of Ceylon ATM", "ATM machine", 6.882817439752699f, 79.8858718848885f, "facilities"));
        mapPoints.add(new MapPoint("Lecture Hall", "Lecture Hall", 6.883239958668671f, 79.8857251503696f, "Academic"));
        mapPoints.add(new MapPoint("Industrial Automation lab and Mechanical Engineering Labs", "Student Labs", 6.883476965982314f, 79.88576007141626f, "Academic"));
        mapPoints.add(new MapPoint("Mechanical Engineering Workshop", "Lecture Hall", 6.883691926651343f, 79.88587947188455f, "Academic"));
        mapPoints.add(new MapPoint("Block 19", "Blocks", 6.883032000907498f, 79.88556594457505f, "Academic"));
        mapPoints.add(new MapPoint("Faculty of Health Sciences", "Faculty", 6.882898062989423f, 79.88531564252635f, "Academic"));
        mapPoints.add(new MapPoint("Block 12", "Blocks", 6.8833497022676475f, 79.8853224588232f, "Academic"));
        mapPoints.add(new MapPoint("Block 10 Lecture Halls", "Lecture Hall", 6.883045328596026f, 79.88502150970841f, "Academic"));
        mapPoints.add(new MapPoint("Block 9 Lecture Halls", "Lecture Hall", 6.883210454609008f, 79.88502681102304f, "Academic"));
        mapPoints.add(new MapPoint("Block 8 Lecture Halls", "Lecture Hall", 6.883436614127009f, 79.88505613200176f, "Academic"));
        mapPoints.add(new MapPoint("Block 7 Auditorium", "Auditorium", 6.883676067903975f, 79.88514138145833f, "Academic"));
        mapPoints.add(new MapPoint("Computer Science Lab", "Labs", 6.883732391362575f, 79.88499352135757f, "Academic"));
        mapPoints.add(new MapPoint("Block 6 Textile & Apparel Technology Laboratories", "Labs", 6.882721215918876f, 79.88471832298997f, "Academic"));
        mapPoints.add(new MapPoint("Center for Environmental Studies and Sustainable Development", "Environmental study center", 6.883132222483538f, 79.88474287667883f, "Academic"));
        mapPoints.add(new MapPoint("Zoology Biodiversity Museum", "Museum", 6.883147310440679f, 79.88459111333688f, "Academic"));
        mapPoints.add(new MapPoint("Block 2 Department of Civil Engineering Laboratories", "Labs", 6.883570664068851f, 79.88477664931786f, "Academic"));
        mapPoints.add(new MapPoint("Faculty Of Education", "Faculty", 6.8828243500254125f, 79.8840645666408f, "Academic"));
        mapPoints.add(new MapPoint("Pre school OUSL", "Pre School", 6.882704552515855f, 79.88379616691259f, "Academic"));
        mapPoints.add(new MapPoint("Open University Student Vehicle Park", "Vehicle Park", 6.882856818504107f, 79.88378037869327f, "facilities"));
        mapPoints.add(new MapPoint("Printing Press Open University", "Printing press", 6.88316743949768f, 79.88401446116109f, "facilities"));
        mapPoints.add(new MapPoint("Medical Center and staff Day care", "Day care center", 6.883136138657897f, 79.88359801469844f, "facilities"));
        mapPoints.add(new MapPoint("Examination Hall 02", "Examination Hall", 6.883430837179069f, 79.88425007052366f, "Academic"));
        mapPoints.add(new MapPoint("Milk Bar", "Milk Bar", 6.883524042574925f, 79.88431550350468f, "facilities"));
        mapPoints.add(new MapPoint("The Open University Sri Lanka Press", "The Open University Sri Lanka Press", 6.883517452295021f, 79.88400730468105f, "Academic"));
        mapPoints.add(new MapPoint("Course Material Distribution Centre", "Course Material Distribution Centre", 6.883503330266318f, 79.88374177954069f, "Academic"));
        mapPoints.add(new MapPoint(" Budu Medura", " Open University Budu Medura", 6.883520276703287f, 79.88347815100433f, "facilities"));
        mapPoints.add(new MapPoint("Exam Hall 01", "Exam Hall", 6.883687312124374f, 79.88422086399399f, "Academic"));
        mapPoints.add(new MapPoint("Automobile Laboratory", "Labs", 6.883780028626025f, 79.88374011144836f, "Academic"));
        mapPoints.add(new MapPoint("Science and Technology Building", "Science Building", 6.884002098019608f, 79.88369167149267f, "Academic"));
        mapPoints.add(new MapPoint(" Examination Hall 22", "Exam Hall", 6.88458704974357f, 79.88414331911741f, "Academic"));
        mapPoints.add(new MapPoint("Department of Mathematics and Computer Science", "Computer Science Building", 6.884542050021143f, 79.88382032910988f, "Academic"));
        mapPoints.add(new MapPoint("Faculty of Engineering Technology", "Faculty", 6.8843921698834425f, 79.8834793648744f, "Academic"));
        mapPoints.add(new MapPoint("Faculty of Health Sciences OUSL", "Faculty", 6.885019640360351f, 79.88374758437962f, "Academic"));
        mapPoints.add(new MapPoint("Examination Hall 23", "Exam Hall", 6.885132616217157f, 79.88358732099132f, "Academic"));
        mapPoints.add(new MapPoint("Examination Hall 3", "Exam Hall", 6.8851222600980675f, 79.88336826275052f, "Academic"));
        mapPoints.add(new MapPoint("Toilet 3", "Student Toilet", 6.88532824524713f, 79.88372743540023f, "Facilities"));
        mapPoints.add(new MapPoint("Media House", "Media center", 6.885654688243899f, 79.88317760710412f, "Academic"));
        mapPoints.add(new MapPoint(" Instructional Development and Design Centre", "Design Center", 6.885580069084658f, 79.8825436245047f, "Academic"));
        mapPoints.add(new MapPoint("Faculty of Humanities and Social Sciences", "Faculty", 6.886848081576389f, 79.88251410405074f, "Academic"));
        mapPoints.add(new MapPoint("Information Technology Division", "IT Division", 6.88727046555444f, 79.8824892372666f, "Office"));
        mapPoints.add(new MapPoint("Research Unit", "Research Unit", 6.887272639229794f, 79.88236536558716f, "Office"));
        mapPoints.add(new MapPoint("Operations Division", "Operations Division", 6.887237356410955f, 79.88232714410945f, "Office"));
        mapPoints.add(new MapPoint("Regional Educational Services Division", "Educational Services Division", 6.887214056434813f, 79.88233250852738f, "Office"));
        mapPoints.add(new MapPoint("Capital Works and Planning Division", "Capital Works Division", 6.887168122192765f, 79.88230233367656f, "Office"));
        mapPoints.add(new MapPoint("International Relations Unit", "International Relations Unit", 6.887265982095927f, 79.88226008888314f, "Office"));
        mapPoints.add(new MapPoint("Examinations Division", "Examinations Division", 6.887379102424722f, 79.88201435004146f, "Office"));
        mapPoints.add(new MapPoint("Establishments Division", "Establishments Division", 6.887598897774366f, 79.8819837574954f, "Office"));
        mapPoints.add(new MapPoint("Administrative Car Park", "Car Park", 6.886719471359535f, 79.88193951912714f, "Facilities"));
        mapPoints.add(new MapPoint("Staff Development Center", "Staff Development Center", 6.886763338627007f, 79.88163175927652f, "Office"));
        mapPoints.add(new MapPoint("Dormitory", "Dormitory", 6.887056427947631f, 79.88135657143276f, "Facilities"));
        mapPoints.add(new MapPoint("Landscape Division", "Landscape Division", 6.886668313000202f, 79.88121751714307f, "Office"));
        mapPoints.add(new MapPoint("Lands & Building Department", "Lands & Building Department", 6.886863169979508f, 79.88104712144259f, "Office"));
        mapPoints.add(new MapPoint("Guest House", "Open University Guest House", 6.88684447613824f, 79.88063441636996f, "Facilities"));
        mapPoints.add(new MapPoint("Play Ground", "Open University Play Ground", 6.887845887661954f, 79.88130502150541f, "Facilities"));
        mapPoints.add(new MapPoint("Postgraduate Institute of English", "Postgraduate Institute of English, Open University of Sri Lanka PGIE", 6.888250733509577f, 79.88050185380025f, "Academic"));
        mapPoints.add(new MapPoint("Exam hall 4", "Exam Hall", 6.888033053195006f, 79.87979094971014f, "Academic"));
        mapPoints.add(new MapPoint("Exam Hall 05", "OUSL Exam Hall 05", 6.887938959039237f, 79.87918207871505f, "Academic"));
        mapPoints.add(new MapPoint("Exam Hall 06", "OUSL Exam Hall 06", 6.888175192618121f, 79.87917756779498f, "Academic"));
        mapPoints.add(new MapPoint("TRF Hostel", "TRF Hostel (Open University Sri Lanka )", 6.888365114969738f, 79.8793654415212f, "Facilities"));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        googleMap.setOnMarkerClickListener(this);

        addCampusMarkers();
        zoomToOUSLBounds();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            Log.d("MapActivity", "Location permission not granted for My Location layer.");
        }
    }

    private void addCampusMarkers() {
        for (MapPoint point : mapPoints) {
            LatLng latLng = new LatLng(point.x, point.y);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(point.name)
                    .snippet(point.description));

            if (marker != null) {
                markerMapPointMap.put(marker.getId(), point);
            }
        }
    }

    private void zoomToOUSLBounds() {
        if (mapPoints.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MapPoint point : mapPoints) {
            builder.include(new LatLng(point.x, point.y));
        }
        LatLngBounds bounds = builder.build();

        int padding = 150;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        selectedDestinationPoint = markerMapPointMap.get(marker.getId());
        if (selectedDestinationPoint != null) {
            showBottomActionPanel(selectedDestinationPoint);
        }
        return true;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE_MAP);
        } else {
            getDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_MAP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                }
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Map features may be limited.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    currentUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    Log.d("MapActivity", "Got current location: " + currentUserLatLng.latitude + ", " + currentUserLatLng.longitude);
                                } else {
                                    Log.d("MapActivity", "Current location is null.");
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e("MapActivity", "Security Exception: " + e.getMessage());
        }
    }

    private void showBottomActionPanel(final MapPoint destinationPoint) {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }

        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_location_actions, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView tvPanelLocationName = bottomSheetView.findViewById(R.id.tvPanelLocationName);
        TextView tvPanelLocationDescription = bottomSheetView.findViewById(R.id.tvPanelLocationDescription);
        Button btnPanelDirections = bottomSheetView.findViewById(R.id.btnPanelDirections);
        Button btnPanelStartNav = bottomSheetView.findViewById(R.id.btnPanelStartNav);

        tvPanelLocationName.setText(destinationPoint.name);
        tvPanelLocationDescription.setText(destinationPoint.description);

        btnPanelDirections.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showStartLocationDialog(destinationPoint);
        });

        btnPanelStartNav.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (currentUserLatLng != null) {
                proceedToNextActivity(
                        "Your Current Location",
                        (float) currentUserLatLng.latitude, (float) currentUserLatLng.longitude,
                        true,
                        destinationPoint,
                        NavigationActivity.class
                );
            } else {
                Toast.makeText(MapActivity.this, "Current location not available for immediate start. Please try 'Directions' or enable location.", Toast.LENGTH_LONG).show();
            }
        });

        bottomSheetDialog.show();
    }

    private void showStartLocationDialog(MapPoint destinationPoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_start_location, null);
        builder.setView(dialogView);

        AutoCompleteTextView etStartLocation = dialogView.findViewById(R.id.etStartLocation);
        Button btnUseCurrentLocation = dialogView.findViewById(R.id.btnUseCurrentLocation);
        Button btnConfirmStart = dialogView.findViewById(R.id.btnConfirmStart);

        List<String> locationNames = new ArrayList<>();

        for (MapPoint point : mapPoints) {
            locationNames.add(point.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                locationNames
        );
        etStartLocation.setAdapter(adapter);
        etStartLocation.setThreshold(1);

        AlertDialog dialog = builder.create();


        etStartLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                boolean isInputEmpty = s.toString().trim().isEmpty();
                btnUseCurrentLocation.setEnabled(isInputEmpty);

                boolean found = false;
                if (!isInputEmpty) {
                    for (MapPoint p : mapPoints) {
                        if (p.name.equalsIgnoreCase(s.toString().trim())) {
                            found = true;
                            break;
                        }
                    }
                }
                btnConfirmStart.setEnabled(found);
            }
        });


        btnUseCurrentLocation.setOnClickListener(v -> {
            if (currentUserLatLng != null) {
                proceedToNextActivity(
                        "Your Current Location",
                        (float) currentUserLatLng.latitude, (float) currentUserLatLng.longitude,
                        true,
                        destinationPoint,
                        DestinationActivity.class
                );
                dialog.dismiss();
            } else {
                Toast.makeText(MapActivity.this, "Current location not available. Please grant permission or try again.", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmStart.setOnClickListener(v -> {
            String selectedStartName = etStartLocation.getText().toString().trim();
            MapPoint startPoint = null;

            for (MapPoint p : mapPoints) {
                if (p.name.equalsIgnoreCase(selectedStartName)) {
                    startPoint = p;
                    break;
                }
            }

            if (startPoint != null) {
                proceedToNextActivity(
                        startPoint.name,
                        startPoint.x, startPoint.y,
                        false,
                        destinationPoint,
                        DestinationActivity.class
                );
                dialog.dismiss();
            } else {
                Toast.makeText(MapActivity.this, "Please select a valid start location from the suggestions.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void proceedToNextActivity(String startLocationName, float startLat, float startLng,
                                       boolean isLiveLocation, MapPoint destinationPoint,
                                       Class<?> targetActivityClass) {
        Intent intent = new Intent(MapActivity.this, targetActivityClass);

        intent.putExtra("destination_name", destinationPoint.name);
        intent.putExtra("destination_lat", destinationPoint.x);
        intent.putExtra("destination_lng", destinationPoint.y);

        intent.putExtra("start_location_name", startLocationName);
        intent.putExtra("user_lat", startLat);
        intent.putExtra("user_lng", startLng);
        intent.putExtra("is_live_location", isLiveLocation);

        startActivity(intent);
    }
}










