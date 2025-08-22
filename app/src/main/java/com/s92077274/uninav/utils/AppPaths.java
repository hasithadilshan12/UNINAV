package com.s92077274.uninav.utils;

import com.google.android.gms.maps.model.LatLng;
import com.s92077274.uninav.models.MapPoint;

import android.location.Location;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class AppPaths {

    // Stores predefined campus locations
    private static final Map<String, MapPoint> predefinedMapPoints = new HashMap<>();
    // Stores predefined routes between campus locations
    private static final Map<String, List<LatLng>> campusRoutes = new HashMap<>();

    static {
        // Initialize known MapPoints
        predefinedMapPoints.put("Public Information Office ,Financial Office", new MapPoint("Public Information Office ,Financial Office", "Financial Office", 6.883387838930316f, 79.88654971785698f, "facilities"));
        predefinedMapPoints.put("Library", new MapPoint("Library", "Central Library", 6.886341603335691f, 79.88289203571986f, "academic"));
        predefinedMapPoints.put("CRC Office", new MapPoint("CRC Office", "Colombo regional Center", 6.88347942072658f, 79.88664655562894f, "office"));
        predefinedMapPoints.put("Student Registration Office", new MapPoint("Student Registration Office", "Registration Center", 6.883196206906299f, 79.88654142667893f, "academic"));
        predefinedMapPoints.put("Industry Liaison Center", new MapPoint("Industry Liaison Center", "Academic Center", 6.88297452233968f, 79.8865736131866f, "academic"));
        predefinedMapPoints.put("Student Information Center", new MapPoint("Student Information Center", "Information Center", 6.882981845254872f, 79.88648107697742f, "academic"));
        predefinedMapPoints.put("Cafeteria 1", new MapPoint("Cafeteria 1", "Student Dining Hall", 6.882640917449825f, 79.88512860668303f, "food"));
        predefinedMapPoints.put("Cafeteria 2", new MapPoint("Cafeteria 2", "Student Dining Hall", 6.887295672388694f, 79.88092240982309f, "food"));
        predefinedMapPoints.put("Toilet 1", new MapPoint("Toilet 1", "Restroom Facilities(Block 7)", 6.8835583183688565f, 79.88520893476854f, "facilities"));
        predefinedMapPoints.put("Main Entrance", new MapPoint("Main Entrance", "University Main Gate Nawala", 6.882894376548958f, 79.88676273457729f, "entrance"));
        predefinedMapPoints.put("Security Room", new MapPoint("Security Room", "Security Room", 6.882941399855268f, 79.88669958170006f, "facilities"));
        predefinedMapPoints.put("Toilet 2", new MapPoint("Toilet 2", "Restroom Facilities(Library)", 6.886109088517432f, 79.88283042826042f, "facilities"));
        predefinedMapPoints.put("Neo Space Lab OUSL", new MapPoint("Neo Space Lab OUSL", "Space Lab", 6.883157687598526f, 79.88631782459649f, "facilities"));
        predefinedMapPoints.put("People's Bank ATM", new MapPoint("People's Bank ATM", "ATM machine", 6.882820264162557f, 79.88592024839622f, "facilities"));
        predefinedMapPoints.put("Bank of Ceylon ATM", new MapPoint("Bank of Ceylon ATM", "ATM machine", 6.882817439752699f, 79.8858718848885f, "facilities"));
        predefinedMapPoints.put("Lecture Hall", new MapPoint("Lecture Hall", "Lecture Hall", 6.883239958668671f, 79.8857251503696f, "Academic"));
        predefinedMapPoints.put("Industrial Automation lab and Mechanical Engineering Labs", new MapPoint("Industrial Automation lab and Mechanical Engineering Labs", "Student Labs", 6.883476965982314f, 79.88576007141626f, "Academic"));
        predefinedMapPoints.put("Mechanical Engineering Workshop", new MapPoint("Mechanical Engineering Workshop", "Lecture Hall", 6.883691926651343f, 79.88587947188455f, "Academic"));
        predefinedMapPoints.put("Block 19", new MapPoint("Block 19", "Blocks", 6.883032000907498f, 79.88556594457505f, "Academic"));
        predefinedMapPoints.put("Faculty of Health Sciences", new MapPoint("Faculty of Health Sciences", "Faculty", 6.882898062989423f, 79.88531564252635f, "Academic"));
        predefinedMapPoints.put("Block 12", new MapPoint("Block 12", "Blocks", 6.8833497022676475f, 79.8853224588232f, "Academic"));
        predefinedMapPoints.put("Block 10 Lecture Halls", new MapPoint("Block 10 Lecture Halls", "Lecture Hall", 6.883045328596026f, 79.88502150970841f, "Academic"));
        predefinedMapPoints.put("Block 9 Lecture Halls", new MapPoint("Block 9 Lecture Halls", "Lecture Hall", 6.883210454609008f, 79.88502681102304f, "Academic"));
        predefinedMapPoints.put("Block 8 Lecture Halls", new MapPoint("Block 8 Lecture Halls", "Lecture Hall", 6.883436614127009f, 79.88505613200176f, "Academic"));
        predefinedMapPoints.put("Block 7 Auditorium", new MapPoint("Block 7 Auditorium", "Auditorium", 6.883676067903975f, 79.88514138145833f, "Academic"));
        predefinedMapPoints.put("Computer Science Lab", new MapPoint("Computer Science Lab", "Labs", 6.883732391362575f, 79.88499352135757f, "Academic"));
        predefinedMapPoints.put("Block 6 Textile & Apparel Technology Laboratories", new MapPoint("Block 6 Textile & Apparel Technology Laboratories", "Labs", 6.882721215918876f, 79.88471832298997f, "Academic"));
        predefinedMapPoints.put("Center for Environmental Studies and Sustainable Development", new MapPoint("Center for Environmental Studies and Sustainable Development", "Environmental study center", 6.883132222483538f, 79.88474287667883f, "Academic"));
        predefinedMapPoints.put("Zoology Biodiversity Museum", new MapPoint("Zoology Biodiversity Museum", "Museum", 6.883147310440679f, 79.88459111333688f, "Academic"));
        predefinedMapPoints.put("Block 2 Department of Civil Engineering Laboratories", new MapPoint("Block 2 Department of Civil Engineering Laboratories", "Labs", 6.883570664068851f, 79.88477664931786f, "Academic"));
        predefinedMapPoints.put("Faculty Of Education", new MapPoint("Faculty Of Education", "Faculty", 6.8828243500254125f, 79.8840645666408f, "Academic"));
        predefinedMapPoints.put("Pre school OUSL", new MapPoint("Pre school OUSL", "Pre School", 6.882704552515855f, 79.88379616691259f, "Academic"));
        predefinedMapPoints.put("Open University Student Vehicle Park", new MapPoint("Open University Student Vehicle Park", "Vehicle Park", 6.882856818504107f, 79.88378037869327f, "facilities"));
        predefinedMapPoints.put("Printing Press Open University", new MapPoint("Printing Press Open University", "Printing press", 6.88316743949768f, 79.88401446116109f, "facilities"));
        predefinedMapPoints.put("Medical Center and staff Day care", new MapPoint("Medical Center and staff Day care", "Day care center", 6.883136138657897f, 79.88359801469844f, "facilities"));
        predefinedMapPoints.put("Examination Hall 02", new MapPoint("Examination Hall 02", "Examination Hall", 6.883430837179069f, 79.88425007052366f, "Academic"));
        predefinedMapPoints.put("Milk Bar", new MapPoint("Milk Bar", "Milk Bar", 6.883524042574925f, 79.88431550350468f, "facilities"));
        predefinedMapPoints.put("The Open University Sri Lanka Press", new MapPoint("The Open University Sri Lanka Press", "The Open University Sri Lanka Press", 6.883517452295021f, 79.88400730468105f, "Academic"));
        predefinedMapPoints.put("Course Material Distribution Centre", new MapPoint("Course Material Distribution Centre", "Course Material Distribution Centre", 6.883503330266318f, 79.88374177954069f, "Academic"));
        predefinedMapPoints.put(" Budu Medura", new MapPoint(" Budu Medura", " Open University Budu Medura", 6.883520276703287f, 79.88347815100433f, "facilities"));
        predefinedMapPoints.put("Exam Hall 01", new MapPoint("Exam Hall 01", "Exam Hall", 6.883687312124374f, 79.88422086399399f, "Academic"));
        predefinedMapPoints.put("Automobile Laboratory", new MapPoint("Automobile Laboratory", "Labs", 6.883780028626025f, 79.88374011144836f, "Academic"));
        predefinedMapPoints.put("Science and Technology Building", new MapPoint("Science and Technology Building", "Science Building", 6.884002098019608f, 79.88369167149267f, "Academic"));
        predefinedMapPoints.put(" Examination Hall 22", new MapPoint(" Examination Hall 22", "Exam Hall", 6.88458704974357f, 79.88414331911741f, "Academic"));
        predefinedMapPoints.put("Department of Mathematics and Computer Science", new MapPoint("Department of Mathematics and Computer Science", "Computer Science Building", 6.884542050021143f, 79.88382032910988f, "Academic"));
        predefinedMapPoints.put("Faculty of Engineering Technology", new MapPoint("Faculty of Engineering Technology", "Faculty", 6.8843921698834425f, 79.8834793648744f, "Academic"));
        predefinedMapPoints.put("Faculty of Health Sciences OUSL", new MapPoint("Faculty of Health Sciences OUSL", "Faculty", 6.885019640360351f, 79.88374758437962f, "Academic"));
        predefinedMapPoints.put("Examination Hall 23", new MapPoint("Examination Hall 23", "Exam Hall", 6.885132616217157f, 79.88358732099132f, "Academic"));
        predefinedMapPoints.put("Examination Hall 3", new MapPoint("Examination Hall 3", "Exam Hall", 6.8851222600980675f, 79.88336826275052f, "Academic"));
        predefinedMapPoints.put("Toilet 3", new MapPoint("Toilet 3", "Student Toilet", 6.88532824524713f, 79.88372743540023f, "Facilities"));
        predefinedMapPoints.put("Media House", new MapPoint("Media House", "Media center", 6.885654688243899f, 79.88317760710412f, "Academic"));
        predefinedMapPoints.put(" Instructional Development and Design Centre", new MapPoint(" Instructional Development and Design Centre", "Design Center", 6.885580069084658f, 79.8825436245047f, "Academic"));
        predefinedMapPoints.put("Faculty of Humanities and Social Sciences", new MapPoint("Faculty of Humanities and Social Sciences", "Faculty", 6.886848081576389f, 79.88251410405074f, "Academic"));
        predefinedMapPoints.put("Information Technology Division", new MapPoint("Information Technology Division", "IT Division", 6.88727046555444f, 79.8824892372666f, "Office"));
        predefinedMapPoints.put("Research Unit", new MapPoint("Research Unit", "Research Unit", 6.887272639229794f, 79.88236536558716f, "Office"));
        predefinedMapPoints.put("Operations Division", new MapPoint("Operations Division", "Operations Division", 6.887237356410955f, 79.88232714410945f, "Office"));
        predefinedMapPoints.put("Regional Educational Services Division", new MapPoint("Regional Educational Services Division", "Educational Services Division", 6.887214056434813f, 79.88233250852738f, "Office"));
        predefinedMapPoints.put("Capital Works and Planning Division", new MapPoint("Capital Works and Planning Division", "Capital Works Division", 6.887168122192765f, 79.88230233367656f, "Office"));
        predefinedMapPoints.put("International Relations Unit", new MapPoint("International Relations Unit", "International Relations Unit", 6.887265982095927f, 79.88226008888314f, "Office"));
        predefinedMapPoints.put("Examinations Division", new MapPoint("Examinations Division", "Examinations Division", 6.887379102424722f, 79.88201435004146f, "Office"));
        predefinedMapPoints.put("Establishments Division", new MapPoint("Establishments Division", "Establishments Division", 6.887598897774366f, 79.8819837574954f, "Office"));
        predefinedMapPoints.put("Administrative Car Park", new MapPoint("Administrative Car Park", "Car Park", 6.886719471359535f, 79.88193951912714f, "Facilities"));
        predefinedMapPoints.put("Staff Development Center", new MapPoint("Staff Development Center", "Staff Development Center", 6.886763338627007f, 79.88163175927652f, "Office"));
        predefinedMapPoints.put("Dormitory", new MapPoint("Dormitory", "Dormitory", 6.887056427947631f, 79.88135657143276f, "Facilities"));
        predefinedMapPoints.put("Landscape Division", new MapPoint("Landscape Division", "Landscape Division", 6.886668313000202f, 79.88121751714307f, "Office"));
        predefinedMapPoints.put("Lands & Building Department", new MapPoint("Lands & Building Department", "Lands & Building Department", 6.886863169979508f, 79.88104712144259f, "Office"));
        predefinedMapPoints.put("Guest House", new MapPoint("Guest House", "Open University Guest House", 6.88684447613824f, 79.88063441636996f, "Facilities"));
        predefinedMapPoints.put("Play Ground", new MapPoint("Play Ground", "Open University Play Ground", 6.887845887661954f, 79.88130502150541f, "Facilities"));
        predefinedMapPoints.put("Postgraduate Institute of English", new MapPoint("Postgraduate Institute of English", "Postgraduate Institute of English, Open University of Sri Lanka PGIE", 6.888250733509577f, 79.88050185380025f, "Academic"));
        predefinedMapPoints.put("Exam hall 4", new MapPoint("Exam hall 4", "Exam Hall", 6.888033053195006f, 79.87979094971014f, "Academic"));
        predefinedMapPoints.put("Exam Hall 05", new MapPoint("Exam Hall 05", "OUSL Exam Hall 05", 6.887938959039237f, 79.87918207871505f, "Academic"));
        predefinedMapPoints.put("Exam Hall 06", new MapPoint("Exam Hall 06", "OUSL Exam Hall 06", 6.888175192618121f, 79.87917756779498f, "Academic"));
        predefinedMapPoints.put("TRF Hostel", new MapPoint("TRF Hostel", "TRF Hostel (Open University Sri Lanka )", 6.888365114969738f, 79.8793654415212f, "Facilities"));


        // Define custom routes and add their reverse
        List<LatLng> mainEntranceToStudentInformationCenter = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.883008617767714, 79.88665208898998),
                new LatLng(6.883014159516421, 79.88658111812254),
                new LatLng(predefinedMapPoints.get("Student Information Center").x, predefinedMapPoints.get("Student Information Center").y)
        );
        String keyME_IT = "Main Entrance_To_Student Information Center";
        campusRoutes.put(keyME_IT, mainEntranceToStudentInformationCenter);
        addReverseRoute(keyME_IT, mainEntranceToStudentInformationCenter);

        List<LatLng> mainEntranceToCafeteria_1 = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883771810274032, 79.88550174959093),
                new LatLng(6.883589724502035, 79.88547782682662),
                new LatLng(6.883615058178857, 79.88521627127022),
                new LatLng(6.882640917449825f, 79.88512860668303f),
                new LatLng(predefinedMapPoints.get("Cafeteria 1").x, predefinedMapPoints.get("Cafeteria 1").y)
        );
        String keyME_CAF_01 = "Main Entrance_To_Cafeteria 1";
        campusRoutes.put(keyME_CAF_01, mainEntranceToCafeteria_1);
        addReverseRoute(keyME_CAF_01, mainEntranceToCafeteria_1);

        List<LatLng> crcToCafeteria_1 = Arrays.asList(
                new LatLng(predefinedMapPoints.get("CRC Office").x, predefinedMapPoints.get("CRC Office").y),
                new LatLng(6.883469533390325, 79.8866590614989),
                new LatLng(6.883468201952579, 79.88669862408018),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883771810274032, 79.88550174959093),
                new LatLng(6.883589724502035, 79.88547782682662),
                new LatLng(6.883615058178857, 79.88521627127022),
                new LatLng(6.882640917449825f, 79.88512860668303f),
                new LatLng(predefinedMapPoints.get("Cafeteria 1").x, predefinedMapPoints.get("Cafeteria 1").y)
        );
        String keyCRC_CAF_01 = "CRC Office_To_Cafeteria 1";
        campusRoutes.put(keyCRC_CAF_01, crcToCafeteria_1);
        addReverseRoute(keyCRC_CAF_01, crcToCafeteria_1);

        List<LatLng> mainEntranceToStudentRegistrationOffice = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.883185795738538, 79.88666325303065),
                new LatLng(6.883196087553656f, 79.88655081603841f),
                new LatLng(predefinedMapPoints.get("Student Registration Office").x, predefinedMapPoints.get("Student Registration Office").y)
        );
        String keyME_SRO = "Main Entrance_To_Student Registration Office";
        campusRoutes.put(keyME_SRO, mainEntranceToStudentRegistrationOffice);
        addReverseRoute(keyME_SRO, mainEntranceToStudentRegistrationOffice);

        List<LatLng> mainEntranceToCrcOffice = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.883468201952579, 79.88669862408018),
                new LatLng(6.883469533390325, 79.8866590614989),
                new LatLng(predefinedMapPoints.get("CRC Office").x, predefinedMapPoints.get("CRC Office").y)
        );
        String keyME_CRC = "Main Entrance_To_CRC Office";
        campusRoutes.put(keyME_CRC, mainEntranceToCrcOffice);
        addReverseRoute(keyME_CRC, mainEntranceToCrcOffice);

        List<LatLng> mainEntranceToPublicInformationOfficeFinancialOffice = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.883345079712944, 79.88668239686629),
                new LatLng(6.8833573953295595, 79.88665082042841),
                new LatLng(predefinedMapPoints.get("Public Information Office ,Financial Office").x, predefinedMapPoints.get("Public Information Office ,Financial Office").y)
        );

        String keyME_PIO = "Main Entrance_To_Public Information Office ,Financial Office";
        campusRoutes.put(keyME_PIO, mainEntranceToPublicInformationOfficeFinancialOffice);
        addReverseRoute(keyME_PIO, mainEntranceToPublicInformationOfficeFinancialOffice);

        List<LatLng> establishmentsDivisionToMainEntrance = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Establishments Division").x, predefinedMapPoints.get("Establishments Division").y),
                new LatLng(6.887598897774366f, 79.8819837574954f),
                new LatLng(6.887684653492381, 79.88201838034811),
                new LatLng(6.887763827632987, 79.88177903270578),
                new LatLng(6.887311621305996, 79.88151534385746),
                new LatLng(6.886961842794369, 79.88225372937637),
                new LatLng(6.886659961290986, 79.88219762631564),
                new LatLng(6.886209993556812, 79.88270514922014),
                new LatLng(6.885586883875528, 79.88291167930909),
                new LatLng(6.883619743202517, 79.88341828572773),
                new LatLng(6.883608445566548, 79.88443486769717),
                new LatLng(6.883883354255399, 79.88449176594021),
                new LatLng(6.8836084455726425, 79.88669562459177),
                new LatLng(6.8829283823365435, 79.8866327498816),
                new LatLng(6.882916399382998, 79.88674540265538),
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y)
        );
        String keyED_ME = "Establishments Division_To_Main Entrance";
        campusRoutes.put(keyED_ME, establishmentsDivisionToMainEntrance);
        addReverseRoute(keyED_ME, establishmentsDivisionToMainEntrance);

        List<LatLng> establishmentsDivisionToCrcOffice = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Establishments Division").x, predefinedMapPoints.get("Establishments Division").y),
                new LatLng(6.887598897774366f, 79.8819837574954f),
                new LatLng(6.887684653492381, 79.88201838034811),
                new LatLng(6.887763827632987, 79.88177903270578),
                new LatLng(6.887311621305996, 79.88151534385746),
                new LatLng(6.886961842794369, 79.88225372937637),
                new LatLng(6.886659961290986, 79.88219762631564),
                new LatLng(6.886209993556812, 79.88270514922014),
                new LatLng(6.885586883875528, 79.88291167930909),
                new LatLng(6.883619743202517, 79.88341828572773),
                new LatLng(6.883608445566548, 79.88443486769717),
                new LatLng(6.883883354255399, 79.88449176594021),
                new LatLng(6.8836084455726425, 79.88669562459177),
                new LatLng(6.883474209061779, 79.88669312084247),
                new LatLng(6.883469533390325, 79.8866590614989),
                new LatLng(predefinedMapPoints.get("CRC Office").x, predefinedMapPoints.get("CRC Office").y)
        );
        String keyED_CRC = "Establishments Division_To_CRC Office";
        campusRoutes.put(keyED_CRC, establishmentsDivisionToCrcOffice);
        addReverseRoute(keyED_CRC, establishmentsDivisionToCrcOffice);

        List<LatLng> establishmentsDivisionToLibrary = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Establishments Division").x, predefinedMapPoints.get("Establishments Division").y),
                new LatLng(6.887500f, 79.881900f),
                new LatLng(6.887000f, 79.882500f),
                new LatLng(predefinedMapPoints.get("Library").x, predefinedMapPoints.get("Library").y)
        );
        String keyED_Library = "Establishments Division_To_Library";
        campusRoutes.put(keyED_Library, establishmentsDivisionToLibrary);
        addReverseRoute(keyED_Library, establishmentsDivisionToLibrary);

        List<LatLng> establishmentsDivisionToFacultyOfHumanitiesAndSocialSciences = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Establishments Division").x, predefinedMapPoints.get("Establishments Division").y),
                new LatLng(6.887614265504267, 79.88198138396922),
                new LatLng(6.8876673689042205, 79.8820090896117),
                new LatLng(6.887751338248419, 79.88177903270578),
                new LatLng(6.887295664438771, 79.88152078254302),
                new LatLng(6.886848081576389f, 79.88251410405074f),
                new LatLng(predefinedMapPoints.get("Faculty of Humanities and Social Sciences").x, predefinedMapPoints.get("Faculty of Humanities and Social Sciences").y)
        );
        String keyED_FHSS = "Establishments Division_To_Faculty of Humanities and Social Sciences";
        campusRoutes.put(keyED_FHSS, establishmentsDivisionToFacultyOfHumanitiesAndSocialSciences);
        addReverseRoute(keyED_FHSS, establishmentsDivisionToFacultyOfHumanitiesAndSocialSciences);

        List<LatLng> mainEntranceToLibrary = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.88361807133475, 79.88339693826187),
                new LatLng(6.886213480539401, 79.88271566433535),
                new LatLng(6.886341603335691f, 79.88289203571986f),
                new LatLng(predefinedMapPoints.get("Library").x, predefinedMapPoints.get("Library").y)
        );
        String keyME_Library = "Main Entrance_To_Library";
        campusRoutes.put(keyME_Library, mainEntranceToLibrary);
        addReverseRoute(keyME_Library, mainEntranceToLibrary);

        List<LatLng> mainEntranceToFacultyOfEngineeringTechnology = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.88361807133475, 79.88339693826187),
                new LatLng(6.884417422757129, 79.88320510227197),
                new LatLng(6.884465565511414, 79.88331900299433),
                new LatLng(6.8843921698834425f, 79.8834793648744f),
                new LatLng(predefinedMapPoints.get("Faculty of Engineering Technology").x, predefinedMapPoints.get("Faculty of Engineering Technology").y)
        );
        String keyME_FOET = "Main Entrance_To_Faculty of Engineering Technology";
        campusRoutes.put(keyME_FOET, mainEntranceToFacultyOfEngineeringTechnology);
        addReverseRoute(keyME_FOET, mainEntranceToFacultyOfEngineeringTechnology);

        List<LatLng> crcToFacultyOfEngineeringTechnology = Arrays.asList(
                new LatLng(predefinedMapPoints.get("CRC Office").x, predefinedMapPoints.get("CRC Office").y),
                new LatLng(6.883469533390325, 79.8866590614989),
                new LatLng(6.883476871937243, 79.88669580305137),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.88361807133475, 79.88339693826187),
                new LatLng(6.884417422757129, 79.88320510227197),
                new LatLng(6.884465565511414, 79.88331900299433),
                new LatLng(6.8843921698834425f, 79.8834793648744f),
                new LatLng(predefinedMapPoints.get("Faculty of Engineering Technology").x, predefinedMapPoints.get("Faculty of Engineering Technology").y)
        );
        String keyCRC_FOET = "CRC Office_To_Faculty of Engineering Technology";
        campusRoutes.put(keyCRC_FOET, crcToFacultyOfEngineeringTechnology);
        addReverseRoute(keyCRC_FOET, crcToFacultyOfEngineeringTechnology);

        List<LatLng> crcToLibrary = Arrays.asList(
                new LatLng(predefinedMapPoints.get("CRC Office").x, predefinedMapPoints.get("CRC Office").y),
                new LatLng(6.883469533390325, 79.8866590614989),
                new LatLng(6.883476871937243, 79.88669580305137),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.88361807133475, 79.88339693826187),
                new LatLng(6.886213480539401, 79.88271566433535),
                new LatLng(6.886341603335691f, 79.88289203571986f),
                new LatLng(predefinedMapPoints.get("Library").x, predefinedMapPoints.get("Library").y)
        );
        String keyCRC_Library = "CRC Office_To_Library";
        campusRoutes.put(keyCRC_Library, crcToLibrary);
        addReverseRoute(keyCRC_Library, crcToLibrary);

        List<LatLng> mainEntranceToExamHall01 = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.88360884843643, 79.88420198330321),
                new LatLng(6.883687312124374f, 79.88422086399399f),
                new LatLng(predefinedMapPoints.get("Exam Hall 01").x, predefinedMapPoints.get("Exam Hall 01").y)
        );
        String keyME_EH01 = "Main Entrance_To_Exam Hall 01";
        campusRoutes.put(keyME_EH01, mainEntranceToExamHall01);
        addReverseRoute(keyME_EH01, mainEntranceToExamHall01);

        List<LatLng> crcToExamHall01 = Arrays.asList(
                new LatLng(predefinedMapPoints.get("CRC Office").x, predefinedMapPoints.get("CRC Office").y),
                new LatLng(6.883469533390325, 79.8866590614989),
                new LatLng(6.883476871937243, 79.88669580305137),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.88360884843643, 79.88420198330321),
                new LatLng(6.883687312124374f, 79.88422086399399f),
                new LatLng(predefinedMapPoints.get("Exam Hall 01").x, predefinedMapPoints.get("Exam Hall 01").y)
        );
        String keyCRC_EH01 = "CRC Office_To_Exam Hall 01";
        campusRoutes.put(keyCRC_EH01, crcToExamHall01);
        addReverseRoute(keyCRC_EH01, crcToExamHall01);

        List<LatLng> mainEntranceToPlayGround = Arrays.asList(
                new LatLng(predefinedMapPoints.get("Main Entrance").x, predefinedMapPoints.get("Main Entrance").y),
                new LatLng(6.882894376548958f, 79.88676273457729f),
                new LatLng(6.882948416062589, 79.88664323549267),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.883615249846182, 79.88341117636902),
                new LatLng(6.885579027210578, 79.88291319061524),
                new LatLng(6.8866784505059, 79.88219264866835),
                new LatLng(6.886950061910049, 79.8822462928295),
                new LatLng(6.887525238449621, 79.8810500276603),
                new LatLng(6.887845887661954f, 79.88130502150541f),
                new LatLng(predefinedMapPoints.get("Play Ground").x, predefinedMapPoints.get("Play Ground").y)
        );
        String keyME_Playground = "Main Entrance_To_Play Ground";
        campusRoutes.put(keyME_Playground, mainEntranceToPlayGround);
        addReverseRoute(keyME_Playground, mainEntranceToPlayGround);

        List<LatLng> crcToPlayGround = Arrays.asList(
                new LatLng(predefinedMapPoints.get("CRC Office").x, predefinedMapPoints.get("CRC Office").y),
                new LatLng(6.883469533390325, 79.8866590614989),
                new LatLng(6.883476871937243, 79.88669580305137),
                new LatLng(6.8835878481437485, 79.88670114839948),
                new LatLng(6.883899968569014, 79.88449070720179),
                new LatLng(6.883618611379107, 79.88443020632579),
                new LatLng(6.883615249846182, 79.88341117636902),
                new LatLng(6.885579027210578, 79.88291319061524),
                new LatLng(6.886225393801104, 79.88271311562559),
                new LatLng(6.8866784505059, 79.88219264866835),
                new LatLng(6.886950061910049, 79.8822462928295),
                new LatLng(6.887525238449621, 79.8810500276603),
                new LatLng(6.887845887661954f, 79.88130502150541f),
                new LatLng(predefinedMapPoints.get("Play Ground").x, predefinedMapPoints.get("Play Ground").y)
        );
        String keyCRC_Playground = "CRC Office_To_Play Ground";
        campusRoutes.put(keyCRC_Playground, crcToPlayGround);
        addReverseRoute(keyCRC_Playground, crcToPlayGround);
    }



    // Adds a reverse route to allow bidirectional navigation
    private static void addReverseRoute(String originalRouteKey, List<LatLng> originalPath) {
        String[] parts = originalRouteKey.split("_To_");
        if (parts.length == 2) {
            String startName = parts[0];
            String destinationName = parts[1];
            String reverseRouteKey = destinationName + "_To_" + startName;
            List<LatLng> reversedPath = reverseLatLngList(originalPath);
            campusRoutes.put(reverseRouteKey, reversedPath);
        }
    }

    // Reverses the order of a list of LatLng points
    private static List<LatLng> reverseLatLngList(List<LatLng> originalList) {
        List<LatLng> reversedList = new ArrayList<>(originalList);
        Collections.reverse(reversedList);
        return reversedList;
    }

    // Retrieves a predefined route by start and destination names
    public static List<LatLng> getRoute(String startName, String destinationName) {
        String routeKey = startName + "_To_" + destinationName;
        return campusRoutes.get(routeKey);
    }

    // Gets a MapPoint object by its name
    public static MapPoint getMapPointByName(String name) {
        return predefinedMapPoints.get(name);
    }

    // Gets all predefined MapPoint names
    public static List<String> getAllMapPointNames() {
        return new ArrayList<>(predefinedMapPoints.keySet());
    }

    // Finds the nearest predefined MapPoint to a given location
    public static MapPoint findNearestMapPoint(LatLng currentLatLng) {
        if (currentLatLng == null || predefinedMapPoints.isEmpty()) {
            return null;
        }

        MapPoint nearestPoint = null;
        float minDistance = Float.MAX_VALUE;
        float[] results = new float[1];

        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatLng.latitude);
        currentLocation.setLongitude(currentLatLng.longitude);

        for (MapPoint point : predefinedMapPoints.values()) {
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    point.x, point.y, results);
            float distance = results[0];

            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = point;
            }
        }
        return nearestPoint;
    }

    // Generates simple navigation instructions for a route path
    public static List<String> getRouteInstructions(List<LatLng> route, String startLocationName, String destinationLocationName) {
        List<String> instructions = new ArrayList<>();
        if (route == null || route.size() < 2) {
            instructions.add("No detailed instructions available for this short route.");
            return instructions;
        }

        instructions.add("1. Start at " + startLocationName + ".");

        for (int i = 0; i < route.size() - 2; i++) {
            LatLng p1 = route.get(i);
            LatLng p2 = route.get(i + 1);
            LatLng p3 = route.get(i + 2);

            float bearing1 = calculateBearing(p1, p2);
            float bearing2 = calculateBearing(p2, p3);

            float angleDiff = bearing2 - bearing1;

            if (angleDiff > 180) angleDiff -= 360;
            if (angleDiff < -180) angleDiff += 360;

            String turnInstruction;
            if (Math.abs(angleDiff) < 15) {
                turnInstruction = "Continue straight.";
            } else if (angleDiff > 0) {
                turnInstruction = "Turn left.";
            } else {
                turnInstruction = "Turn right.";
            }

            float[] segmentDistanceResults = new float[1];
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, segmentDistanceResults);
            double segmentDistanceMeters = segmentDistanceResults[0];

            String distanceInfo = String.format(Locale.getDefault(), " (approx. %.0f meters)", segmentDistanceMeters);

            instructions.add((i + 2) + ". " + turnInstruction + distanceInfo);
        }

        instructions.add((instructions.size() + 1) + ". You have arrived at " + destinationLocationName + ".");

        return instructions;
    }

    // Calculates bearing between two LatLng points
    private static float calculateBearing(LatLng p1, LatLng p2) {
        Location loc1 = new Location("");
        loc1.setLatitude(p1.latitude);
        loc1.setLongitude(p1.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(p2.latitude);
        loc2.setLongitude(p2.longitude);

        return loc1.bearingTo(loc2);
    }
}
