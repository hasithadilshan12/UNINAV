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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import com.s92077274.uninav.models.MapPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocationShowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_LOCATION_SHOW = 2;

    private TextView tvLocationName, tvLocationDescription;
    private Button btnNavigateTo;
    private MapPoint shownLocation;
    private LatLng currentUserLatLng;

    private Map<String, MapPoint> savedLocationMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_show);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationDescription = findViewById(R.id.tvLocationDescription);
        btnNavigateTo = findViewById(R.id.btnNavigateTo);

        initSavedLocations();

        String name = getIntent().getStringExtra("location_name");

        float lat = getIntent().getFloatExtra("location_lat", 0.0f);
        float lng = getIntent().getFloatExtra("location_lng", 0.0f);

        shownLocation = new MapPoint(name, "Selected location on campus.", lat, lng, "general");

        HomeActivity.addRecentSearch(this, shownLocation);

        tvLocationName.setText(name);
        tvLocationDescription.setText("Selected location on campus.");

        btnNavigateTo.setOnClickListener(v -> showStartLocationDialog(shownLocation));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        getDeviceLocation();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng locationLatLng = new LatLng(shownLocation.x, shownLocation.y);
        googleMap.addMarker(new MarkerOptions()
                .position(locationLatLng)
                .title(shownLocation.name)
                .snippet(shownLocation.description));


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 17f));
    }

    private void initSavedLocations() {
        savedLocationMap = new HashMap<>();

        savedLocationMap.put("Public Information Office ,Financial Office", new MapPoint("Public Information Office ,Financial Office", "Financial Office", 6.883387838930316f, 79.88654971785698f, "facilities"));
        savedLocationMap.put("Library", new MapPoint("Library", "Central Library", 6.886341603335691f, 79.88289203571986f, "academic"));
        savedLocationMap.put("CRC Office", new MapPoint("CRC Office", "Colombo regional Center", 6.88347942072658f, 79.88664655562894f, "office"));
        savedLocationMap.put("Student Registration Office", new MapPoint("Student Registration Office", "Registration Center", 6.883196206906299f, 79.88654142667893f, "academic"));
        savedLocationMap.put("Industry Liaison Center", new MapPoint("Industry Liaison Center", "Academic Center", 6.88297452233968f, 79.8865736131866f, "academic"));
        savedLocationMap.put("Student Information Center", new MapPoint("Student Information Center", "Information Center", 6.882981845254872f, 79.88648107697742f, "academic"));
        savedLocationMap.put("Cafeteria 1", new MapPoint("Cafeteria 1", "Student Dining Hall", 6.882640917449825f, 79.88512860668303f, "food"));
        savedLocationMap.put("Cafeteria 2", new MapPoint("Cafeteria 2", "Student Dining Hall", 6.887295672388694f, 79.88092240982309f, "food"));
        savedLocationMap.put("Toilet 1", new MapPoint("Toilet 1", "Restroom Facilities(Block 7)", 6.8835583183688565f, 79.88520893476854f, "facilities"));
        savedLocationMap.put("Main Entrance", new MapPoint("Main Entrance", "University Main Gate Nawala", 6.882894376548958f, 79.88676273457729f, "entrance"));
        savedLocationMap.put("Security Room", new MapPoint("Security Room", "Security Room", 6.882941399855268f, 79.88669958170006f, "facilities"));
        savedLocationMap.put("Toilet 2", new MapPoint("Toilet 2", "Restroom Facilities(Library)", 6.886109088517432f, 79.88283042826042f, "facilities"));
        savedLocationMap.put("Neo Space Lab OUSL", new MapPoint("Neo Space Lab OUSL", "Space Lab", 6.883157687598526f, 79.88631782459649f, "facilities"));
        savedLocationMap.put("People's Bank ATM", new MapPoint("People's Bank ATM", "ATM machine", 6.882820264162557f, 79.88592024839622f, "facilities"));
        savedLocationMap.put("Bank of Ceylon ATM", new MapPoint("Bank of Ceylon ATM", "ATM machine", 6.882817439752699f, 79.8858718848885f, "facilities"));
        savedLocationMap.put("Lecture Hall", new MapPoint("Lecture Hall", "Lecture Hall", 6.883239958668671f, 79.8857251503696f, "Academic"));
        savedLocationMap.put("Industrial Automation lab and Mechanical Engineering Labs", new MapPoint("Industrial Automation lab and Mechanical Engineering Labs", "Student Labs", 6.883476965982314f, 79.88576007141626f, "Academic"));
        savedLocationMap.put("Mechanical Engineering Workshop", new MapPoint("Mechanical Engineering Workshop", "Lecture Hall", 6.883691926651343f, 79.88587947188455f, "Academic"));
        savedLocationMap.put("Block 19", new MapPoint("Block 19", "Blocks", 6.883032000907498f, 79.88556594457505f, "Academic"));
        savedLocationMap.put("Faculty of Health Sciences", new MapPoint("Faculty of Health Sciences", "Faculty", 6.882898062989423f, 79.88531564252635f, "Academic"));
        savedLocationMap.put("Block 12", new MapPoint("Block 12", "Blocks", 6.8833497022676475f, 79.8853224588232f, "Academic"));
        savedLocationMap.put("Block 10 Lecture Halls", new MapPoint("Block 10 Lecture Halls", "Lecture Hall", 6.883045328596026f, 79.88502150970841f, "Academic"));
        savedLocationMap.put("Block 9 Lecture Halls", new MapPoint("Block 9 Lecture Halls", "Lecture Hall", 6.883210454609008f, 79.88502681102304f, "Academic"));
        savedLocationMap.put("Block 8 Lecture Halls", new MapPoint("Block 8 Lecture Halls", "Lecture Hall", 6.883436614127009f, 79.88505613200176f, "Academic"));
        savedLocationMap.put("Block 7 Auditorium", new MapPoint("Block 7 Auditorium", "Auditorium", 6.883676067903975f, 79.88514138145833f, "Academic"));
        savedLocationMap.put("Computer Science Lab", new MapPoint("Computer Science Lab", "Labs", 6.883732391362575f, 79.88499352135757f, "Academic"));
        savedLocationMap.put("Block 6 Textile & Apparel Technology Laboratories", new MapPoint("Block 6 Textile & Apparel Technology Laboratories", "Labs", 6.882721215918876f, 79.88471832298997f, "Academic"));
        savedLocationMap.put("Center for Environmental Studies and Sustainable Development", new MapPoint("Center for Environmental Studies and Sustainable Development", "Environmental study center", 6.883132222483538f, 79.88474287667883f, "Academic"));
        savedLocationMap.put("Zoology Biodiversity Museum", new MapPoint("Zoology Biodiversity Museum", "Museum", 6.883147310440679f, 79.88459111333688f, "Academic"));
        savedLocationMap.put("Block 2 Department of Civil Engineering Laboratories", new MapPoint("Block 2 Department of Civil Engineering Laboratories", "Labs", 6.883570664068851f, 79.88477664931786f, "Academic"));
        savedLocationMap.put("Faculty Of Education", new MapPoint("Faculty Of Education", "Faculty", 6.8828243500254125f, 79.8840645666408f, "Academic"));
        savedLocationMap.put("Pre school OUSL", new MapPoint("Pre school OUSL", "Pre School", 6.882704552515855f, 79.88379616691259f, "Academic"));
        savedLocationMap.put("Open University Student Vehicle Park", new MapPoint("Open University Student Vehicle Park", "Vehicle Park", 6.882856818504107f, 79.88378037869327f, "facilities"));
        savedLocationMap.put("Printing Press Open University", new MapPoint("Printing Press Open University", "Printing press", 6.88316743949768f, 79.88401446116109f, "facilities"));
        savedLocationMap.put("Medical Center and staff Day care", new MapPoint("Medical Center and staff Day care", "Day care center", 6.883136138657897f, 79.88359801469844f, "facilities"));
        savedLocationMap.put("Examination Hall 02", new MapPoint("Examination Hall 02", "Examination Hall", 6.883430837179069f, 79.88425007052366f, "Academic"));
        savedLocationMap.put("Milk Bar", new MapPoint("Milk Bar", "Milk Bar", 6.883524042574925f, 79.88431550350468f, "facilities"));
        savedLocationMap.put("The Open University Sri Lanka Press", new MapPoint("The Open University Sri Lanka Press", "The Open University Sri Lanka Press", 6.883517452295021f, 79.88400730468105f, "Academic"));
        savedLocationMap.put("Course Material Distribution Centre", new MapPoint("Course Material Distribution Centre", "Course Material Distribution Centre", 6.883503330266318f, 79.88374177954069f, "Academic"));
        savedLocationMap.put(" Budu Medura", new MapPoint(" Budu Medura", " Open University Budu Medura", 6.883520276703287f, 79.88347815100433f, "facilities"));
        savedLocationMap.put("Exam Hall 01", new MapPoint("Exam Hall 01", "Exam Hall", 6.883687312124374f, 79.88422086399399f, "Academic"));
        savedLocationMap.put("Automobile Laboratory", new MapPoint("Automobile Laboratory", "Labs", 6.883780028626025f, 79.88374011144836f, "Academic"));
        savedLocationMap.put("Science and Technology Building", new MapPoint("Science and Technology Building", "Science Building", 6.884002098019608f, 79.88369167149267f, "Academic"));
        savedLocationMap.put(" Examination Hall 22", new MapPoint(" Examination Hall 22", "Exam Hall", 6.88458704974357f, 79.88414331911741f, "Academic"));
        savedLocationMap.put("Department of Mathematics and Computer Science", new MapPoint("Department of Mathematics and Computer Science", "Computer Science Building", 6.884542050021143f, 79.88382032910988f, "Academic"));
        savedLocationMap.put("Faculty of Engineering Technology", new MapPoint("Faculty of Engineering Technology", "Faculty", 6.8843921698834425f, 79.8834793648744f, "Academic"));
        savedLocationMap.put("Faculty of Health Sciences OUSL", new MapPoint("Faculty of Health Sciences OUSL", "Faculty", 6.885019640360351f, 79.88374758437962f, "Academic"));
        savedLocationMap.put("Examination Hall 23", new MapPoint("Examination Hall 23", "Exam Hall", 6.885132616217157f, 79.88358732099132f, "Academic"));
        savedLocationMap.put("Examination Hall 3", new MapPoint("Examination Hall 3", "Exam Hall", 6.8851222600980675f, 79.88336826275052f, "Academic"));
        savedLocationMap.put("Toilet 3", new MapPoint("Toilet 3", "Student Toilet", 6.88532824524713f, 79.88372743540023f, "Facilities"));
        savedLocationMap.put("Media House", new MapPoint("Media House", "Media center", 6.885654688243899f, 79.88317760710412f, "Academic"));
        savedLocationMap.put(" Instructional Development and Design Centre", new MapPoint(" Instructional Development and Design Centre", "Design Center", 6.885580069084658f, 79.8825436245047f, "Academic"));
        savedLocationMap.put("Faculty of Humanities and Social Sciences", new MapPoint("Faculty of Humanities and Social Sciences", "Faculty", 6.886848081576389f, 79.88251410405074f, "Academic"));
        savedLocationMap.put("Information Technology Division", new MapPoint("Information Technology Division", "IT Division", 6.88727046555444f, 79.8824892372666f, "Office"));
        savedLocationMap.put("Research Unit", new MapPoint("Research Unit", "Research Unit", 6.887272639229794f, 79.88236536558716f, "Office"));
        savedLocationMap.put("Operations Division", new MapPoint("Operations Division", "Operations Division", 6.887237356410955f, 79.88232714410945f, "Office"));
        savedLocationMap.put("Regional Educational Services Division", new MapPoint("Regional Educational Services Division", "Educational Services Division", 6.887214056434813f, 79.88233250852738f, "Office"));
        savedLocationMap.put("Capital Works and Planning Division", new MapPoint("Capital Works and Planning Division", "Capital Works Division", 6.887168122192765f, 79.88230233367656f, "Office"));
        savedLocationMap.put("International Relations Unit", new MapPoint("International Relations Unit", "International Relations Unit", 6.887265982095927f, 79.88226008888314f, "Office"));
        savedLocationMap.put("Examinations Division", new MapPoint("Examinations Division", "Examinations Division", 6.887379102424722f, 79.88201435004146f, "Office"));
        savedLocationMap.put("Establishments Division", new MapPoint("Establishments Division", "Establishments Division", 6.887598897774366f, 79.8819837574954f, "Office"));
        savedLocationMap.put("Administrative Car Park", new MapPoint("Administrative Car Park", "Car Park", 6.886719471359535f, 79.88193951912714f, "Facilities"));
        savedLocationMap.put("Staff Development Center", new MapPoint("Staff Development Center", "Staff Development Center", 6.886763338627007f, 79.88163175927652f, "Office"));
        savedLocationMap.put("Dormitory", new MapPoint("Dormitory", "Dormitory", 6.887056427947631f, 79.88135657143276f, "Facilities"));
        savedLocationMap.put("Landscape Division", new MapPoint("Landscape Division", "Landscape Division", 6.886668313000202f, 79.88121751714307f, "Office"));
        savedLocationMap.put("Lands & Building Department", new MapPoint("Lands & Building Department", "Lands & Building Department", 6.886863169979508f, 79.88104712144259f, "Office"));
        savedLocationMap.put("Guest House", new MapPoint("Guest House", "Open University Guest House", 6.88684447613824f, 79.88063441636996f, "Facilities"));
        savedLocationMap.put("Play Ground", new MapPoint("Play Ground", "Open University Play Ground", 6.887845887661954f, 79.88130502150541f, "Facilities"));
        savedLocationMap.put("Postgraduate Institute of English", new MapPoint("Postgraduate Institute of English", "Postgraduate Institute of English, Open University of Sri Lanka PGIE", 6.888250733509577f, 79.88050185380025f, "Academic"));
        savedLocationMap.put("Exam hall 4", new MapPoint("Exam hall 4", "Exam Hall", 6.888033053195006f, 79.87979094971014f, "Academic"));
        savedLocationMap.put("Exam Hall 05", new MapPoint("Exam Hall 05", "OUSL Exam Hall 05", 6.887938959039237f, 79.87918207871505f, "Academic"));
        savedLocationMap.put("Exam Hall 06", new MapPoint("Exam Hall 06", "OUSL Exam Hall 06", 6.888175192618121f, 79.87917756779498f, "Academic"));
        savedLocationMap.put("TRF Hostel", new MapPoint("TRF Hostel", "TRF Hostel (Open University Sri Lanka )", 6.888365114969738f, 79.8793654415212f, "Facilities"));
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                Log.d("LocationShowActivity", "Got current location: " + currentUserLatLng.latitude + ", " + currentUserLatLng.longitude);
                            } else {
                                Log.d("LocationShowActivity", "Current location is null, cannot use live location as start.");
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE_LOCATION_SHOW);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_LOCATION_SHOW) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot use current location as start.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showStartLocationDialog(MapPoint destinationPoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_start_location, null);
        builder.setView(dialogView);

        AutoCompleteTextView etStartLocation = dialogView.findViewById(R.id.etStartLocation);
        Button btnUseCurrentLocation = dialogView.findViewById(R.id.btnUseCurrentLocation);
        Button btnConfirmStart = dialogView.findViewById(R.id.btnConfirmStart);

        List<String> locationNames = new ArrayList<>(savedLocationMap.keySet());
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
                    found = savedLocationMap.containsKey(s.toString().trim());
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
                Toast.makeText(LocationShowActivity.this, "Current location not available. Please grant permission or try again.", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmStart.setOnClickListener(v -> {
            String startLocationName = etStartLocation.getText().toString().trim();
            MapPoint startPoint = savedLocationMap.get(startLocationName);

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
                Toast.makeText(LocationShowActivity.this, "Please select a valid start location from the list.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void proceedToNextActivity(String startLocationName, float startLat, float startLng,
                                       boolean isLiveLocation, MapPoint destinationPoint,
                                       Class<?> targetActivityClass) {
        Intent intent = new Intent(LocationShowActivity.this, targetActivityClass);

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
