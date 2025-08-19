package com.s92077274.uninav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.s92077274.uninav.SearchResultsAdapter;
import com.s92077274.uninav.models.MapPoint;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private ImageButton btnSearchIcon;
    private RecyclerView recyclerSearchResults;
    private SearchResultsAdapter adapter;
    private List<MapPoint> allLocations;
    private List<MapPoint> filteredLocations;
    private static final String TAG = "SearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initData();
        setupRecyclerView();
        setClickListeners();
    }

    // Initializes UI components
    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnSearchIcon = findViewById(R.id.btnSearchIcon);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    // Initializes the list of all predefined locations
    private void initData() {
        allLocations = new ArrayList<>();

        allLocations.add(new MapPoint("Public Information Office ,Financial Office", "Financial Office", 6.883387838930316f, 79.88654971785698f, "facilities"));
        allLocations.add(new MapPoint("Library", "Central Library", 6.886341603335691f, 79.88289203571986f, "academic"));
        allLocations.add(new MapPoint("CRC Office", "Colombo regional Center", 6.88347942072658f, 79.88664655562894f, "office"));
        allLocations.add(new MapPoint("Student Registration Office", "Registration Center", 6.883196206906299f, 79.88654142667893f, "academic"));
        allLocations.add(new MapPoint("Industry Liaison Center", "Academic Center", 6.88297452233968f, 79.8865736131866f, "academic"));
        allLocations.add(new MapPoint("Student Information Center", "Information Center", 6.882981845254872f, 79.88648107697742f, "academic"));
        allLocations.add(new MapPoint("Cafeteria 1", "Student Dining Hall", 6.882640917449825f, 79.88512860668303f, "food"));
        allLocations.add(new MapPoint("Cafeteria 2", "Student Dining Hall", 6.887295672388694f, 79.88092240982309f, "food"));
        allLocations.add(new MapPoint("Toilet 1", "Restroom Facilities(Block 7)", 6.8835583183688565f, 79.88520893476854f, "facilities"));
        allLocations.add(new MapPoint("Main Entrance", "University Main Gate Nawala", 6.882894376548958f, 79.88676273457729f, "entrance"));
        allLocations.add(new MapPoint("Security Room", "Security Room", 6.882941399855268f, 79.88669958170006f, "facilities"));
        allLocations.add(new MapPoint("Toilet 2", "Restroom Facilities(Library)", 6.886109088517432f, 79.88283042826042f, "facilities"));
        allLocations.add(new MapPoint("Neo Space Lab OUSL", "Space Lab", 6.883157687598526f, 79.88631782459649f, "facilities"));
        allLocations.add(new MapPoint("People's Bank ATM", "ATM machine", 6.882820264162557f, 79.88592024839622f, "facilities"));
        allLocations.add(new MapPoint("Bank of Ceylon ATM", "ATM machine", 6.882817439752699f, 79.8858718848885f, "facilities"));
        allLocations.add(new MapPoint("Lecture Hall", "Lecture Hall", 6.883239958668671f, 79.8857251503696f, "Academic"));
        allLocations.add(new MapPoint("Industrial Automation lab and Mechanical Engineering Labs", "Student Labs", 6.883476965982314f, 79.88576007141626f, "Academic"));
        allLocations.add(new MapPoint("Mechanical Engineering Workshop", "Lecture Hall", 6.883691926651343f, 79.88587947188455f, "Academic"));
        allLocations.add(new MapPoint("Block 19", "Blocks", 6.883032000907498f, 79.88556594457505f, "Academic"));
        allLocations.add(new MapPoint("Faculty of Health Sciences", "Faculty", 6.882898062989423f, 79.88531564252635f, "Academic"));
        allLocations.add(new MapPoint("Block 12", "Blocks", 6.8833497022676475f, 79.8853224588232f, "Academic"));
        allLocations.add(new MapPoint("Block 10 Lecture Halls", "Lecture Hall", 6.883045328596026f, 79.88502150970841f, "Academic"));
        allLocations.add(new MapPoint("Block 9 Lecture Halls", "Lecture Hall", 6.883210454609008f, 79.88502681102304f, "Academic"));
        allLocations.add(new MapPoint("Block 8 Lecture Halls", "Lecture Hall", 6.883436614127009f, 79.88505613200176f, "Academic"));
        allLocations.add(new MapPoint("Block 7 Auditorium", "Auditorium", 6.883676067903975f, 79.88514138145833f, "Academic"));
        allLocations.add(new MapPoint("Computer Science Lab", "Labs", 6.883732391362575f, 79.88499352135757f, "Academic"));
        allLocations.add(new MapPoint("Block 6 Textile & Apparel Technology Laboratories", "Labs", 6.882721215918876f, 79.88471832298997f, "Academic"));
        allLocations.add(new MapPoint("Center for Environmental Studies and Sustainable Development", "Environmental study center", 6.883132222483538f, 79.88474287667883f, "Academic"));
        allLocations.add(new MapPoint("Zoology Biodiversity Museum", "Museum", 6.883147310440679f, 79.88459111333688f, "Academic"));
        allLocations.add(new MapPoint("Block 2 Department of Civil Engineering Laboratories", "Labs", 6.883570664068851f, 79.88477664931786f, "Academic"));
        allLocations.add(new MapPoint("Faculty Of Education", "Faculty", 6.8828243500254125f, 79.8840645666408f, "Academic"));
        allLocations.add(new MapPoint("Pre school OUSL", "Pre School", 6.882704552515855f, 79.88379616691259f, "Academic"));
        allLocations.add(new MapPoint("Open University Student Vehicle Park", "Vehicle Park", 6.882856818504107f, 79.88378037869327f, "facilities"));
        allLocations.add(new MapPoint("Printing Press Open University", "Printing press", 6.88316743949768f, 79.88401446116109f, "facilities"));
        allLocations.add(new MapPoint("Medical Center and staff Day care", "Day care center", 6.883136138657897f, 79.88359801469844f, "facilities"));
        allLocations.add(new MapPoint("Examination Hall 02", "Examination Hall", 6.883430837179069f, 79.88425007052366f, "Academic"));
        allLocations.add(new MapPoint("Milk Bar", "Milk Bar", 6.883524042574925f, 79.88431550350468f, "facilities"));
        allLocations.add(new MapPoint("The Open University Sri Lanka Press", "The Open University Sri Lanka Press", 6.883517452295021f, 79.88400730468105f, "Academic"));
        allLocations.add(new MapPoint("Course Material Distribution Centre", "Course Material Distribution Centre", 6.883503330266318f, 79.88374177954069f, "Academic"));
        allLocations.add(new MapPoint(" Budu Medura", " Open University Budu Medura", 6.883520276703287f, 79.88347815100433f, "facilities"));
        allLocations.add(new MapPoint("Exam Hall 01", "Exam Hall", 6.883687312124374f, 79.88422086399399f, "Academic"));
        allLocations.add(new MapPoint("Automobile Laboratory", "Labs", 6.883780028626025f, 79.88374011144836f, "Academic"));
        allLocations.add(new MapPoint("Science and Technology Building", "Science Building", 6.884002098019608f, 79.88369167149267f, "Academic"));
        allLocations.add(new MapPoint(" Examination Hall 22", "Exam Hall", 6.88458704974357f, 79.88414331911741f, "Academic"));
        allLocations.add(new MapPoint("Department of Mathematics and Computer Science", "Computer Science Building", 6.884542050021143f, 79.88382032910988f, "Academic"));
        allLocations.add(new MapPoint("Faculty of Engineering Technology", "Faculty", 6.8843921698834425f, 79.8834793648744f, "Academic"));
        allLocations.add(new MapPoint("Faculty of Health Sciences OUSL", "Faculty", 6.885019640360351f, 79.88374758437962f, "Academic"));
        allLocations.add(new MapPoint("Examination Hall 23", "Exam Hall", 6.885132616217157f, 79.88358732099132f, "Academic"));
        allLocations.add(new MapPoint("Examination Hall 3", "Exam Hall", 6.8851222600980675f, 79.88336826275052f, "Academic"));
        allLocations.add(new MapPoint("Toilet 3", "Student Toilet", 6.88532824524713f, 79.88372743540023f, "Facilities"));
        allLocations.add(new MapPoint("Media House", "Media center", 6.885654688243899f, 79.88317760710412f, "Academic"));
        allLocations.add(new MapPoint(" Instructional Development and Design Centre", "Design Center", 6.885580069084658f, 79.8825436245047f, "Academic"));
        allLocations.add(new MapPoint("Faculty of Humanities and Social Sciences", "Faculty", 6.886848081576389f, 79.88251410405074f, "Academic"));
        allLocations.add(new MapPoint("Information Technology Division", "IT Division", 6.88727046555444f, 79.8824892372666f, "Office"));
        allLocations.add(new MapPoint("Research Unit", "Research Unit", 6.887272639229794f, 79.88236536558716f, "Office"));
        allLocations.add(new MapPoint("Operations Division", "Operations Division", 6.887237356410955f, 79.88232714410945f, "Office"));
        allLocations.add(new MapPoint("Regional Educational Services Division", "Educational Services Division", 6.887214056434813f, 79.88233250852738f, "Office"));
        allLocations.add(new MapPoint("Capital Works and Planning Division", "Capital Works Division", 6.887168122192765f, 79.88230233367656f, "Office"));
        allLocations.add(new MapPoint("International Relations Unit", "International Relations Unit", 6.887265982095927f, 79.88226008888314f, "Office"));
        allLocations.add(new MapPoint("Examinations Division", "Examinations Division", 6.887379102424722f, 79.88201435004146f, "Office"));
        allLocations.add(new MapPoint("Establishments Division", "Establishments Division", 6.887598897774366f, 79.8819837574954f, "Office"));
        allLocations.add(new MapPoint("Administrative Car Park", "Car Park", 6.886719471359535f, 79.88193951912714f, "Facilities"));
        allLocations.add(new MapPoint("Staff Development Center", "Staff Development Center", 6.886763338627007f, 79.88163175927652f, "Office"));
        allLocations.add(new MapPoint("Dormitory", "Dormitory", 6.887056427947631f, 79.88135657143276f, "Facilities"));
        allLocations.add(new MapPoint("Landscape Division", "Landscape Division", 6.886668313000202f, 79.88121751714307f, "Office"));
        allLocations.add(new MapPoint("Lands & Building Department", "Lands & Building Department", 6.886863169979508f, 79.88104712144259f, "Office"));
        allLocations.add(new MapPoint("Guest House", "Open University Guest House", 6.88684447613824f, 79.88063441636996f, "Facilities"));
        allLocations.add(new MapPoint("Play Ground", "Open University Play Ground", 6.887845887661954f, 79.88130502150541f, "Facilities"));
        allLocations.add(new MapPoint("Postgraduate Institute of English", "Postgraduate Institute of English, Open University of Sri Lanka PGIE", 6.888250733509577f, 79.88050185380025f, "Academic"));
        allLocations.add(new MapPoint("Exam hall 4", "Exam Hall", 6.888033053195006f, 79.87979094971014f, "Academic"));
        allLocations.add(new MapPoint("Exam Hall 05", "OUSL Exam Hall 05", 6.887938959039237f, 79.87918207871505f, "Academic"));
        allLocations.add(new MapPoint("Exam Hall 06", "OUSL Exam Hall 06", 6.888175192618121f, 79.87917756779498f, "Academic"));
        allLocations.add(new MapPoint("TRF Hostel", "TRF Hostel (Open University Sri Lanka )", 6.888365114969738f, 79.8793654415212f, "Facilities"));

        filteredLocations = new ArrayList<>(allLocations);
    }

    // Sets up the RecyclerView with its adapter and layout manager
    private void setupRecyclerView() {
        adapter = new SearchResultsAdapter(filteredLocations, location -> {
            Log.d(TAG, "Search result clicked: " + location.name +
                    " X: " + location.x + ", Y: " + location.y);
            HomeActivity.addRecentSearch(this, location); // Add clicked location to recent searches

            Intent intent = new Intent(SearchActivity.this, LocationShowActivity.class);
            intent.putExtra("location_name", location.name);
            intent.putExtra("location_lat", location.x);
            intent.putExtra("location_lng", location.y);
            startActivity(intent);
        });

        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(adapter);
    }

    // Sets up click and text change listeners for UI elements
    private void setClickListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLocations(s.toString()); // Filter locations as text changes
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSearchIcon.setOnClickListener(v -> {
            String query = etSearch.getText().toString();
            filterLocations(query); // Filter locations on search icon click
        });

        // Navigation bar click listeners
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navMap = findViewById(R.id.navMap);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, HomeActivity.class));
        });
        navSearch.setOnClickListener(v -> {
            Toast.makeText(SearchActivity.this, "Already on Search page", Toast.LENGTH_SHORT).show();
        });
        navMap.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, MapActivity.class));
        });
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
        });
    }

    // Filters the list of locations based on the search query
    private void filterLocations(String query) {
        filteredLocations.clear();
        if (query.isEmpty()) {
            filteredLocations.addAll(allLocations); // Show all if query is empty
        } else {
            for (MapPoint location : allLocations) {
                // Check if location name contains the query (case-insensitive)
                if (location.name.toLowerCase().contains(query.toLowerCase())) {
                    filteredLocations.add(location);
                }
            }
        }
        adapter.notifyDataSetChanged(); // Refresh RecyclerView with filtered data
    }
}
