package com.example.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Concentration {
    
    private boolean majmin; // false = major, true = minor
    private String name;
    private ArrayList<Requirement> reqs;

    public Concentration(boolean majmin, String name) {
        this.majmin = majmin;
        this.name = name;
        this.reqs = majmin ? getMinorRequirements() : getMajorRequirements();
    }

    private ArrayList<Requirement> getMajorRequirements() {
        return loadRequirementsFromSheet("src\\main\\resources\\Major-Requirements.xlsx");
    }

    private ArrayList<Requirement> getMinorRequirements() {
        return loadRequirementsFromSheet("src\\main\\resources\\Minor-Requirements.xlsx");
    }

    private ArrayList<Requirement> loadRequirementsFromSheet(String filePath) {
        ArrayList<Requirement> requirements = new ArrayList<>();
            
            Sheet reqsSheet = new SheetGenerator(filePath).getSheet();
            
            
            for (Row row : reqsSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                
                String majorName = row.getCell(0).getStringCellValue().trim();
                if (!majorName.equalsIgnoreCase(this.name)) continue; // Skip rows not related to this major
                
                String courses = row.getCell(1).getStringCellValue().trim();
                int numNeeded = (int) row.getCell(2).getNumericCellValue();
                String title = row.getCell(3).getStringCellValue().trim();
                String numberRequirements = row.getCell(4) != null && row.getCell(4).getCellType() != CellType.BLANK ? row.getCell(4).getStringCellValue().trim() : "";
                String typeRequirements = row.getCell(5) != null && row.getCell(5).getCellType() != CellType.BLANK ? row.getCell(5).getStringCellValue().trim() : "";
                String gradeRequirement = row.getCell(6) != null && row.getCell(6).getCellType() != CellType.BLANK ? row.getCell(6).getStringCellValue().trim() : "";

                if (courses.contains(";")) {
                    requirements.add(Requirement.fromSequences(title, parseSequences(courses, title, gradeRequirement), numNeeded, gradeRequirement));
                } else {
                    requirements.add(Requirement.fromCourseGroups(title, parseCourseList(courses), numNeeded, numberRequirements, typeRequirements, gradeRequirement));
                }

            }
        
        return requirements;
    }

    private ArrayList<String> parseCourseList(String courses) {
        return new ArrayList<>(Arrays.asList(courses.split("\\s*,\\s*")));
    }

    private ArrayList<Requirement> parseSequences(String seqs, String title, String gradeRequirement) {
        ArrayList<Requirement> requirements = new ArrayList<>();

        // Split the input by semicolons to separate different requirement groups
        String[] groups = seqs.split("\\s*;\\s*");

        for (String group : groups) {
            // Remove parentheses and trim whitespace
            group = group.replaceAll("[()]", "").trim();

            // Split into course list and number needed
            String[] parts = group.split("\\s*\\|\\s*");
            if (parts.length != 2) continue; // Ensure valid format

            // Extract course list and number of courses needed
            ArrayList<String> courseList = parseCourseList(parts[0]);
            int numNeeded = Integer.parseInt(parts[1].trim());

            // Create and add a Requirement object (Title can be adjusted as needed)
            requirements.add(Requirement.fromCourseGroups(title, courseList, numNeeded, "", "", gradeRequirement));
        }

        return requirements;
    }

    public boolean isMajmin() {
        return majmin;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Requirement> getReqs() {
        return reqs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(majmin ? "Minor: " : "Major: ").append(name).append("\n");
        for (Requirement req : reqs) {
            sb.append(req.toString()).append("\n");
        }
        return sb.toString();
    }
}
