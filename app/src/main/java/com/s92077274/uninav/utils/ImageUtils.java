package com.s92077274.uninav.utils;

import android.content.Context;
import android.util.Log;

import com.s92077274.uninav.R;

import java.util.HashMap;
import java.util.Map;

public class ImageUtils {

    // Maps MapPoint names to their corresponding drawable resource names
    private static final Map<String, String> LOCATION_IMAGE_MAP = new HashMap<>();

    static {
        // Define mappings for each predefined location to its image drawable
        LOCATION_IMAGE_MAP.put("Library", "library_image");
        LOCATION_IMAGE_MAP.put("CRC Office", "crc_office_image");
        LOCATION_IMAGE_MAP.put("Main Entrance", "main_entrance_image");
        LOCATION_IMAGE_MAP.put("Public Information Office ,Financial Office", "public_info_office_image");
        LOCATION_IMAGE_MAP.put("Student Registration Office", "student_regoffice_image");
        LOCATION_IMAGE_MAP.put("Industry Liaison Center", "industry_liaison_center_image");
        LOCATION_IMAGE_MAP.put("Student Information Center", "student_info_center_image");
        LOCATION_IMAGE_MAP.put("Cafeteria 1", "cafeteria1_image");
        LOCATION_IMAGE_MAP.put("Cafeteria 2", "cafeteria2_image");
        LOCATION_IMAGE_MAP.put("Toilet 1", "toilet1_image");
        LOCATION_IMAGE_MAP.put("Security Room", "security_room_image");
        LOCATION_IMAGE_MAP.put("Toilet 2", "toilet2_image");
        LOCATION_IMAGE_MAP.put("Neo Space Lab OUSL", "neo_space_lab_image");
        LOCATION_IMAGE_MAP.put("People's Bank ATM", "peoples_bank_atm_image");
        LOCATION_IMAGE_MAP.put("Bank of Ceylon ATM", "boc_atm_image");
        LOCATION_IMAGE_MAP.put("Lecture Hall", "lecture_hall_image");
        LOCATION_IMAGE_MAP.put("Industrial Automation lab and Mechanical Engineering Labs", "industrial_automation_lab_image");
        LOCATION_IMAGE_MAP.put("Mechanical Engineering Workshop", "mechanical_eng_workshop_image");
        LOCATION_IMAGE_MAP.put("Block 19", "block19_image");
        LOCATION_IMAGE_MAP.put("Faculty of Health Sciences", "faculty_health_sciences_image");
        LOCATION_IMAGE_MAP.put("Block 12", "block12_image");
        LOCATION_IMAGE_MAP.put("Block 10 Lecture Halls", "block10_lecture_halls_image");
        LOCATION_IMAGE_MAP.put("Block 9 Lecture Halls", "block9_lecture_halls_image");
        LOCATION_IMAGE_MAP.put("Block 8 Lecture Halls", "block8_lecture_halls_image");
        LOCATION_IMAGE_MAP.put("Block 7 Auditorium", "block7_auditorium_image");
        LOCATION_IMAGE_MAP.put("Computer Science Lab", "comp_science_lab_image");
        LOCATION_IMAGE_MAP.put("Block 6 Textile & Apparel Technology Laboratories", "block6_textile_lab_image");
        LOCATION_IMAGE_MAP.put("Center for Environmental Studies and Sustainable Development", "env_studies_center_image");
        LOCATION_IMAGE_MAP.put("Zoology Biodiversity Museum", "zoology_museum_image");
        LOCATION_IMAGE_MAP.put("Block 2 Department of Civil Engineering Laboratories", "block2_civil_eng_lab_image");
        LOCATION_IMAGE_MAP.put("Faculty Of Education", "faculty_education_image");
        LOCATION_IMAGE_MAP.put("Pre school OUSL", "pre_school_ousl_image");
        LOCATION_IMAGE_MAP.put("Open University Student Vehicle Park", "student_carpark_image");
        LOCATION_IMAGE_MAP.put("Printing Press Open University", "printing_press_image");
        LOCATION_IMAGE_MAP.put("Medical Center and staff Day care", "medical_center_daycare_image");
        LOCATION_IMAGE_MAP.put("Examination Hall 02", "exam_hall02_image");
        LOCATION_IMAGE_MAP.put("Milk Bar", "milk_bar_image");
        LOCATION_IMAGE_MAP.put("The Open University Sri Lanka Press", "ousl_press_image");
        LOCATION_IMAGE_MAP.put("Course Material Distribution Centre", "course_material_dist_center_image");
        LOCATION_IMAGE_MAP.put(" Budu Medura", "budu_medura_image");
        LOCATION_IMAGE_MAP.put("Exam Hall 01", "examhall_01_image");
        LOCATION_IMAGE_MAP.put("Automobile Laboratory", "automobile_lab_image");
        LOCATION_IMAGE_MAP.put("Science and Technology Building", "science_tech_building_image");
        LOCATION_IMAGE_MAP.put(" Examination Hall 22", "exam_hall22_image");
        LOCATION_IMAGE_MAP.put("Department of Mathematics and Computer Science", "math_comp_science_dept_image");
        LOCATION_IMAGE_MAP.put("Faculty of Engineering Technology", "faculty_eng_tech_image");
        LOCATION_IMAGE_MAP.put("Faculty of Health Sciences OUSL", "faculty_health_sciences_ousl_image");
        LOCATION_IMAGE_MAP.put("Examination Hall 23", "exam_hall23_image");
        LOCATION_IMAGE_MAP.put("Examination Hall 3", "exam_hall3_image");
        LOCATION_IMAGE_MAP.put("Toilet 3", "toilet3_image");
        LOCATION_IMAGE_MAP.put("Media House", "media_house_image");
        LOCATION_IMAGE_MAP.put(" Instructional Development and Design Centre", "iddc_image");
        LOCATION_IMAGE_MAP.put("Faculty of Humanities and Social Sciences", "faculty_humanities_ss_image");
        LOCATION_IMAGE_MAP.put("Information Technology Division", "it_division_image");
        LOCATION_IMAGE_MAP.put("Research Unit", "research_unit_image");
        LOCATION_IMAGE_MAP.put("Operations Division", "operations_division_image");
        LOCATION_IMAGE_MAP.put("Regional Educational Services Division", "regional_edu_services_image");
        LOCATION_IMAGE_MAP.put("Capital Works and Planning Division", "capital_works_division_image");
        LOCATION_IMAGE_MAP.put("International Relations Unit", "intl_relations_unit_image");
        LOCATION_IMAGE_MAP.put("Examinations Division", "examinations_division_image");
        LOCATION_IMAGE_MAP.put("Establishments Division", "establishments_division_image");
        LOCATION_IMAGE_MAP.put("Administrative Car Park", "admin_car_park_image");
        LOCATION_IMAGE_MAP.put("Staff Development Center", "staff_dev_center_image");
        LOCATION_IMAGE_MAP.put("Dormitory", "dormitory_image");
        LOCATION_IMAGE_MAP.put("Landscape Division", "landscape_division_image");
        LOCATION_IMAGE_MAP.put("Lands & Building Department", "lands_building_dept_image");
        LOCATION_IMAGE_MAP.put("Guest House", "guest_house_image");
        LOCATION_IMAGE_MAP.put("Play Ground", "play_ground_image");
        LOCATION_IMAGE_MAP.put("Postgraduate Institute of English", "pgie_image");
        LOCATION_IMAGE_MAP.put("Exam hall 4", "exam_hall4_image");
        LOCATION_IMAGE_MAP.put("Exam Hall 05", "exam_hall05_image");
        LOCATION_IMAGE_MAP.put("Exam Hall 06", "exam_hall06_image");
        LOCATION_IMAGE_MAP.put("TRF Hostel", "trf_hostel_image");
    }

    // Retrieves the drawable resource ID for a given location name
    public static int getDrawableIdForLocation(Context context, String locationName) {
        String drawableName = LOCATION_IMAGE_MAP.get(locationName);
        if (drawableName != null && !drawableName.isEmpty()) {
            int resourceId = context.getResources().getIdentifier(
                    drawableName, "drawable", context.getPackageName());
            if (resourceId != 0) {
                Log.d("ImageUtils", "Found drawable ID " + resourceId + " for " + locationName);
                return resourceId;
            } else {
                Log.w("ImageUtils", "No drawable found for name: " + drawableName + " (MapPoint: " + locationName + ")");
            }
        } else {
            Log.w("ImageUtils", "No drawable name mapping found for MapPoint: " + locationName);
        }
        // Fallback to a placeholder image if no specific image is found
        return R.drawable.ic_image_placeholder;
    }
}
