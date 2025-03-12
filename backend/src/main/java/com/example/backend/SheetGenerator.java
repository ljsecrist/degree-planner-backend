package com.example.backend;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SheetGenerator {

    public Sheet sheet;
    
    public SheetGenerator(String xlsx) {
        String filePath = xlsx;  // Path to the Excel file
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {
            this.sheet = workbook.getSheetAt(0);  // Read the first sheet

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Sheet getSheet(){
        return sheet;
    }
}
