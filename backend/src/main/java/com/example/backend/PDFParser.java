package com.example.backend;

import java.io.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PDFParser {

    // Magic number constants
    private static final int COLUMN_SPLIT_INDEX = 69;

    // Constants for course line parsing when term is present
    private static final int COURSE_WITH_TERM_COURSE_CODE_START = 0;
    private static final int COURSE_WITH_TERM_COURSE_CODE_END = 9;
    private static final int COURSE_WITH_TERM_TITLE_START = 9;
    private static final int COURSE_WITH_TERM_TITLE_END = 37;
    private static final int COURSE_WITH_TERM_GRADE_START = 37;
    private static final int COURSE_WITH_TERM_GRADE_END = 42;
    private static final int COURSE_WITH_TERM_CREDITS_START = 48;
    private static final int COURSE_WITH_TERM_CREDITS_END = 54;
    private static final int COURSE_WITH_TERM_TYPES_START = 59;

    // Constants for course line parsing when term is not present
    private static final int COURSE_WITHOUT_TERM_COURSE_CODE_START = 0;
    private static final int COURSE_WITHOUT_TERM_COURSE_CODE_END = 9;
    private static final int COURSE_WITHOUT_TERM_TITLE_START = 9;
    private static final int COURSE_WITHOUT_TERM_TITLE_END = 47;
    private static final int COURSE_WITHOUT_TERM_CREDITS_START = 47;
    private static final int COURSE_WITHOUT_TERM_CREDITS_END = 52;
    private static final int COURSE_WITHOUT_TERM_TYPES_START = 52;

    // Constants for Excel sheet column indices
    private static final int HEADER_ROW_INDEX = 0;
    private static final int COL_COURSE_NAME = 0;
    private static final int COL_TITLE = 1;
    private static final int COL_COURSE_STATUS = 2;
    private static final int COL_CREDITS = 3;
    private static final int COL_TERM = 4;
    private static final int COL_GRADE = 5;
    private static final int COL_WAC = 6;
    private static final int COL_WACR = 7;
    private static final int COL_WS = 8;
    private static final int COL_JCAD = 9;
    private static final int COL_GCAD = 10;
    private static final int COL_JDQR = 11;
    private static final int COL_GDQR = 12;
    private static final int COL_JLIT = 13;
    private static final int COL_GLIT = 14;
    private static final int COL_JSPE = 15;
    private static final int COL_GSPE = 16;
    private static final int COL_JCHF = 17;
    private static final int COL_GCHF = 18;
    private static final int COL_JETS = 19;
    private static final int COL_GETS = 20;
    private static final int COL_JNPS = 21;
    private static final int COL_GNPS = 22;
    private static final int COL_JWOL = 23;
    private static final int COL_GWOL = 24;
    private static final int COL_HUL = 25;
    private static final int COL_HUM = 26;
    private static final int COL_LCC = 27;
    private static final int COL_SCLB = 28;
    private static final int COL_QMR = 29;
    private static final int COL_SET = 30;
    private static final int COL_SOCS = 31;
    private static final int FIRST_DATA_ROW_INDEX = 1;

    // Constants for term header conversion
    private static final int TERM_HEADER_YEAR_PART_END = 17;
    private static final int TERM_YEAR_SUFFIX_LENGTH = 2;
    private static final int TERM_CODE_END_INDEX = 2;

    // New constants for regex patterns
    private static final int COURSE_LINE_LETTER_COUNT = 3;
    private static final int COURSE_LINE_DIGIT_COUNT = 3;
    private static final String COURSE_LINE_REGEX =
        "^[A-Z]{" + COURSE_LINE_LETTER_COUNT + "} \\d{" + COURSE_LINE_DIGIT_COUNT + "}[A-Z]? .*";

    private static final int TERM_YEAR_DIGIT_COUNT = 4;
    private static final String TERM_HEADER_REGEX =
        ".*Term \\d{" + TERM_YEAR_DIGIT_COUNT + "}.*";

    private static final int COURSE_TYPE_MIN_LENGTH = 3;
    private static final int COURSE_TYPE_MAX_LENGTH = 6;
    private static final String COURSE_TYPE_REGEX =
        "^[A-Z-]{" + COURSE_TYPE_MIN_LENGTH + "," + COURSE_TYPE_MAX_LENGTH + "}$";

    public static void main(String[] args) {
        processPDF("src\\main\\resources\\Secrist_Liam_2686252_2_14_2025.pdf");
    }

    public static void processPDF(String path) {
        try {
            
            File file = new File(path);
            PDDocument document = PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            document.close();

            // Process text and reorder columns correctly
            List<String> firstColumnLines = new ArrayList<>();
            List<String> secondColumnLines = new ArrayList<>();
            reorderColumns(text, firstColumnLines, secondColumnLines);

            // Keep only necessary lines
            firstColumnLines = filterNecessaryLines(firstColumnLines);
            secondColumnLines = filterNecessaryLines(secondColumnLines);

            // Associate standalone course types with their courses
            firstColumnLines = associateCourseTypes(firstColumnLines);
            secondColumnLines = associateCourseTypes(secondColumnLines);

            firstColumnLines.addAll(secondColumnLines);

            
            constructSheet(firstColumnLines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reorderColumns(String text, List<String> firstColumn, List<String> secondColumn) {
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (line.length() > COLUMN_SPLIT_INDEX) {  // Split at character index 69 (zero-based 68)
                String firstPart = line.substring(0, COLUMN_SPLIT_INDEX).trim();
                String secondPart = line.substring(COLUMN_SPLIT_INDEX).trim();

                if (!firstPart.isEmpty()) {
                    firstColumn.add(firstPart);
                }
                if (!secondPart.isEmpty()) {
                    secondColumn.add(secondPart);
                }
            } else {
                firstColumn.add(line.trim());
            }
        }
    }

    public static List<String> filterNecessaryLines(List<String> lines) {
        List<String> necessaryLines = new ArrayList<>();
        for (String line : lines) {
            if (isCourseLine(line) || isTermHeader(line) || isCourseType(line)) {
                necessaryLines.add(line);
            }
        }
        return necessaryLines;
    }

    public static List<String> associateCourseTypes(List<String> lines) {
        List<String> structuredLines = new ArrayList<>();
        String lastCourse = null;

        for (String line : lines) {
            if (isCourseLine(line)) {
                if (lastCourse != null) {
                    structuredLines.add(lastCourse);
                }
                lastCourse = line.trim();
            } else if (isCourseType(line)) {
                if (lastCourse != null) {
                    lastCourse += " " + line.trim();
                }
            } else {
                if (lastCourse != null) {
                    structuredLines.add(lastCourse);
                    lastCourse = null;
                }
                structuredLines.add(line.trim());
            }
        }
        if (lastCourse != null) {
            structuredLines.add(lastCourse);
        }
        return structuredLines;
    }

    public static void printFormattedCourses(List<String> lines) {
        String currentTerm = null;
        String currentTermCode = null;

        for (String line : lines) {
            if (isTermHeader(line)) {
                currentTerm = line.trim();
                currentTermCode = convertTermToCode(currentTerm);
            } else if (isCourseLine(line) && currentTermCode != null) {
                String courseCode = line.substring(COURSE_WITH_TERM_COURSE_CODE_START, COURSE_WITH_TERM_COURSE_CODE_END).trim();
                String title = line.substring(COURSE_WITH_TERM_TITLE_START, COURSE_WITH_TERM_TITLE_END).trim();
                String grade = line.substring(COURSE_WITH_TERM_GRADE_START, COURSE_WITH_TERM_GRADE_END).trim();
                String credits = line.substring(COURSE_WITH_TERM_CREDITS_START, COURSE_WITH_TERM_CREDITS_END).trim();
                String types = line.length() > COURSE_WITH_TERM_TYPES_START ? line.substring(COURSE_WITH_TERM_TYPES_START).trim().replace(" ", ", ") : "N/A";
                types = types.replace("WAC-", "WAC-R");
                
                System.out.println(courseCode + ";" + title + ";" + grade + ";" + credits + ";" + types + ";" + currentTermCode);
            } else if (isCourseLine(line) && currentTermCode == null){
                String courseCode = line.substring(COURSE_WITHOUT_TERM_COURSE_CODE_START, COURSE_WITHOUT_TERM_COURSE_CODE_END).trim();
                String title = line.substring(COURSE_WITHOUT_TERM_TITLE_START, COURSE_WITHOUT_TERM_TITLE_END).trim();
                String credits = line.substring(COURSE_WITHOUT_TERM_CREDITS_START, COURSE_WITHOUT_TERM_CREDITS_END).trim();
                String types = line.length() > COURSE_WITHOUT_TERM_TYPES_START ? line.substring(COURSE_WITHOUT_TERM_TYPES_START).trim().replace(" ", ", ") : "N/A";
                types = types.replace("WAC-", "WAC-R");
                
                System.out.println(courseCode + ";" + title + ";" + "T" + ";" + credits + ";" + types + ";" + currentTermCode);
            }
        }
    }

    public static String constructSheet(List<String> lines) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("transcript");

        Row row = sheet.createRow(HEADER_ROW_INDEX);
        row.createCell(COL_COURSE_NAME).setCellValue("Course Name");
        row.createCell(COL_TITLE).setCellValue("Title");
        row.createCell(COL_COURSE_STATUS).setCellValue("Course Status");
        row.createCell(COL_CREDITS).setCellValue("Credits");
        row.createCell(COL_TERM).setCellValue("Term");
        row.createCell(COL_GRADE).setCellValue("Grade");
        row.createCell(COL_WAC).setCellValue("WAC");
        row.createCell(COL_WACR).setCellValue("WAC-R");
        row.createCell(COL_WS).setCellValue("WS");
        row.createCell(COL_JCAD).setCellValue("JCAD");
        row.createCell(COL_GCAD).setCellValue("GCAD");
        row.createCell(COL_JDQR).setCellValue("JDQR");
        row.createCell(COL_GDQR).setCellValue("GDQR");
        row.createCell(COL_JLIT).setCellValue("JLIT");
        row.createCell(COL_GLIT).setCellValue("GLIT");
        row.createCell(COL_JSPE).setCellValue("JSPE");
        row.createCell(COL_GSPE).setCellValue("GSPE");
        row.createCell(COL_JCHF).setCellValue("JCHF");
        row.createCell(COL_GCHF).setCellValue("GCHF");
        row.createCell(COL_JETS).setCellValue("JETS");
        row.createCell(COL_GETS).setCellValue("GETS");
        row.createCell(COL_JNPS).setCellValue("JNPS");
        row.createCell(COL_GNPS).setCellValue("GNPS");
        row.createCell(COL_JWOL).setCellValue("JWOL");
        row.createCell(COL_GWOL).setCellValue("GWOL");
        row.createCell(COL_HUL).setCellValue("HUL");
        row.createCell(COL_HUM).setCellValue("HUM");
        row.createCell(COL_LCC).setCellValue("LCC");
        row.createCell(COL_SCLB).setCellValue("SCLB");
        row.createCell(COL_QMR).setCellValue("QMR");
        row.createCell(COL_SET).setCellValue("SET");
        row.createCell(COL_SOCS).setCellValue("SOCS");


        String currentTerm = null;
        String currentTermCode = null;
        int currentRow = FIRST_DATA_ROW_INDEX;

        for (String line : lines) {
            row = sheet.createRow(currentRow);

            if (isTermHeader(line)) {
                currentTerm = line.trim();
                currentTermCode = convertTermToCode(currentTerm);
                currentRow--;
            } else if (isCourseLine(line) && currentTermCode != null) {
                String courseCode = line.substring(COURSE_WITH_TERM_COURSE_CODE_START, COURSE_WITH_TERM_COURSE_CODE_END).trim().replace(" ", "-");
                String title = line.substring(COURSE_WITH_TERM_TITLE_START, COURSE_WITH_TERM_TITLE_END).trim();
                String grade = line.substring(COURSE_WITH_TERM_GRADE_START, COURSE_WITH_TERM_GRADE_END).trim();
                String credits = line.substring(COURSE_WITH_TERM_CREDITS_START, COURSE_WITH_TERM_CREDITS_END).trim();
                String types = line.length() > COURSE_WITH_TERM_TYPES_START ? line.substring(COURSE_WITH_TERM_TYPES_START).trim().replace(" ", ", ") : "N/A";
                types = types.replace("WAC-", "WAC-R");
                    
                row.createCell(COL_COURSE_NAME).setCellValue(courseCode);
                row.createCell(COL_TITLE).setCellValue(title);
                row.createCell(COL_COURSE_STATUS).setCellValue("N");
                row.createCell(COL_CREDITS).setCellValue(credits);
                row.createCell(COL_TERM).setCellValue(currentTermCode);
                row.createCell(COL_GRADE).setCellValue(grade);
                row.createCell(COL_WAC).setCellValue(types.contains("WAC"));
                row.createCell(COL_WACR).setCellValue(types.contains("WAC-R"));
                row.createCell(COL_WS).setCellValue(types.contains("WS"));
                row.createCell(COL_JCAD).setCellValue(types.contains("JCAD"));
                row.createCell(COL_GCAD).setCellValue(types.contains("GCAD"));
                row.createCell(COL_JDQR).setCellValue(types.contains("JDQR"));
                row.createCell(COL_GDQR).setCellValue(types.contains("GDQR"));
                row.createCell(COL_JLIT).setCellValue(types.contains("JLIT"));
                row.createCell(COL_GLIT).setCellValue(types.contains("GLIT"));
                row.createCell(COL_JSPE).setCellValue(types.contains("JSPE"));
                row.createCell(COL_GSPE).setCellValue(types.contains("GSPE"));
                row.createCell(COL_JCHF).setCellValue(types.contains("JCHF"));
                row.createCell(COL_GCHF).setCellValue(types.contains("GCHF"));
                row.createCell(COL_JETS).setCellValue(types.contains("JETS"));
                row.createCell(COL_GETS).setCellValue(types.contains("GETS"));
                row.createCell(COL_JNPS).setCellValue(types.contains("JNPS"));
                row.createCell(COL_GNPS).setCellValue(types.contains("GNPS"));
                row.createCell(COL_JWOL).setCellValue(types.contains("JWOL"));
                row.createCell(COL_GWOL).setCellValue(types.contains("GWOL"));
                row.createCell(COL_HUL).setCellValue(types.contains("HUL"));
                row.createCell(COL_HUM).setCellValue(types.contains("HUM"));
                row.createCell(COL_LCC).setCellValue(types.contains("LCC"));
                row.createCell(COL_SCLB).setCellValue(types.contains("SCLB"));
                row.createCell(COL_QMR).setCellValue(types.contains("QMR"));
                row.createCell(COL_SET).setCellValue(types.contains("SET"));
                row.createCell(COL_SOCS).setCellValue(types.contains("SOCS"));


            } else if (isCourseLine(line) && currentTermCode == null){
                String courseCode = line.substring(COURSE_WITHOUT_TERM_COURSE_CODE_START, COURSE_WITHOUT_TERM_COURSE_CODE_END).trim().replace(" ", "-");
                String title = line.substring(COURSE_WITHOUT_TERM_TITLE_START, COURSE_WITHOUT_TERM_TITLE_END).trim();
                String credits = line.substring(COURSE_WITHOUT_TERM_CREDITS_START, COURSE_WITHOUT_TERM_CREDITS_END).trim();
                String types = line.length() > COURSE_WITHOUT_TERM_TYPES_START ? line.substring(COURSE_WITHOUT_TERM_TYPES_START).trim().replace(" ", ", ") : "N/A";
                types = types.replace("WAC-", "WAC-R");
                    
                row.createCell(COL_COURSE_NAME).setCellValue(courseCode);
                row.createCell(COL_TITLE).setCellValue(title);
                row.createCell(COL_COURSE_STATUS).setCellValue("Non-Course Equivalency");
                row.createCell(COL_CREDITS).setCellValue(credits);
                row.createCell(COL_TERM).setCellValue("");
                row.createCell(COL_GRADE).setCellValue("T");
                row.createCell(COL_WAC).setCellValue(types.contains("WAC"));
                row.createCell(COL_WACR).setCellValue(types.contains("WAC-R"));
                row.createCell(COL_WS).setCellValue(types.contains("WS"));
                row.createCell(COL_JCAD).setCellValue(types.contains("JCAD"));
                row.createCell(COL_GCAD).setCellValue(types.contains("GCAD"));
                row.createCell(COL_JDQR).setCellValue(types.contains("JDQR"));
                row.createCell(COL_GDQR).setCellValue(types.contains("GDQR"));
                row.createCell(COL_JLIT).setCellValue(types.contains("JLIT"));
                row.createCell(COL_GLIT).setCellValue(types.contains("GLIT"));
                row.createCell(COL_JSPE).setCellValue(types.contains("JSPE"));
                row.createCell(COL_GSPE).setCellValue(types.contains("GSPE"));
                row.createCell(COL_JCHF).setCellValue(types.contains("JCHF"));
                row.createCell(COL_GCHF).setCellValue(types.contains("GCHF"));
                row.createCell(COL_JETS).setCellValue(types.contains("JETS"));
                row.createCell(COL_GETS).setCellValue(types.contains("GETS"));
                row.createCell(COL_JNPS).setCellValue(types.contains("JNPS"));
                row.createCell(COL_GNPS).setCellValue(types.contains("GNPS"));
                row.createCell(COL_JWOL).setCellValue(types.contains("JWOL"));
                row.createCell(COL_GWOL).setCellValue(types.contains("GWOL"));
                row.createCell(COL_HUL).setCellValue(types.contains("HUL"));
                row.createCell(COL_HUM).setCellValue(types.contains("HUM"));
                row.createCell(COL_LCC).setCellValue(types.contains("LCC"));
                row.createCell(COL_SCLB).setCellValue(types.contains("SCLB"));
                row.createCell(COL_QMR).setCellValue(types.contains("QMR"));
                row.createCell(COL_SET).setCellValue(types.contains("SET"));
                row.createCell(COL_SOCS).setCellValue(types.contains("SOCS"));
            }

            currentRow++;
        }

        String outputDir = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs(); // Create directory if it doesn't exist
        }
        try (FileOutputStream fos = new FileOutputStream("output\\ParsedTranscript.xlsx")) {
            workbook.write(fos);
            System.out.println("Excel file written successfully.");
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        return "output\\ParsedTranscript.xlsx";
        
    }

    public static String convertTermToCode(String termHeader) {
        String year = termHeader.substring(0, TERM_HEADER_YEAR_PART_END).trim().substring(termHeader.substring(0, TERM_HEADER_YEAR_PART_END).trim().length()-TERM_YEAR_SUFFIX_LENGTH);
        String termCode = termHeader.substring(0, TERM_CODE_END_INDEX).toUpperCase();
        
        return year + "/" + termCode;
    }

    public static boolean isCourseLine(String line) {
        return line.trim().matches(COURSE_LINE_REGEX);
    }
    
    public static boolean isTermHeader(String line) {
        return line.trim().matches(TERM_HEADER_REGEX);
    }
    
    public static boolean isCourseType(String line) {
        return line.trim().matches(COURSE_TYPE_REGEX);
    }
}