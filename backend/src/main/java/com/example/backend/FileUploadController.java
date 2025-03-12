package com.example.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static Student currentStudent;


    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {

            // Save the file temporarily
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs(); // Create directory if not exists

            File savedFile = new File(uploadDir + file.getOriginalFilename());
            file.transferTo(savedFile);

            // Process the uploaded PDF using PDFParser
            PDFParser.processPDF(savedFile.getAbsolutePath());

            return "File processed successfully: " + file.getOriginalFilename();
        } catch (IOException e) {
            return "File upload failed: " + e.getMessage();
        } catch (Exception e) {
            return "Error processing file: " + e.getMessage();
        }
    }
    
    @GetMapping("/dropdown-options")
    public Map<String, List<String>> getDropdownOptions() {
        Sheet majors = new SheetGenerator("src\\main\\resources\\Major-List.xlsx").getSheet();
        Sheet minors = new SheetGenerator("src\\main\\resources\\Minor-List.xlsx").getSheet();

        ArrayList<String> majorList = new ArrayList<>();

        for(Row row : majors) {
            //System.out.println(row.getCell(0).toString());
            majorList.add(row.getCell(0).toString());
        }

        ArrayList<String> minorList = new ArrayList<>();

        for(Row row : minors) {
            //System.out.println(row.getCell(0).toString());
            minorList.add(row.getCell(0).toString());
        }
    
        Map<String, List<String>> options = new HashMap<>();
        options.put("dropdown1", majorList);
        options.put("dropdown2", minorList);
    
        return options;
    }

    @PostMapping("/submit-selections")
    public String handleSelections(@RequestBody Map<String, List<String>> selections) {
        List<String> selectedDropdown1 = selections.get("dropdown1");
        List<String> selectedDropdown2 = selections.get("dropdown2");

        System.out.println("Received selections:");
        System.out.println("Dropdown 1: " + selectedDropdown1);
        System.out.println("Dropdown 2: " + selectedDropdown2);

        ArrayList<Concentration> majors = new ArrayList<>();
        for (String str : selectedDropdown1) {
            majors.add(new Concentration(false, str));
        }

        ArrayList<Concentration> minors = new ArrayList<>();
        for (String str : selectedDropdown2) {
            minors.add(new Concentration(true, str));
        }

        // Store the generated Student object
        currentStudent = Driver.generatePlanner("output\\ParsedTranscript.xlsx", majors, minors);

        return "Selections received successfully!";
    }


    @GetMapping("/student-progress")
    public String getStudentProgress() {
        // Assuming the Student object was created in handleSelections()
        if (currentStudent == null) {
            return "No student data available. Please submit your selections first.";
        }

        return currentStudent.getProgressString();
    }



}