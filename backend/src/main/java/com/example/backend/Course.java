package com.example.backend;
import java.util.ArrayList;

public class Course {
    private String term;
    private String year;
    private String code;
    private String title;
    private ArrayList<String> types;
    private int credits;
    private boolean isAP;
    private String grade;

    public Course(String term, String year, String code, String title, ArrayList<String> types, int credits, String grade){
        this.term = term;
        this.year = year;
        this.code = code;
        this.title = title;
        this.types = types;
        this.credits = credits;
        this.isAP = false;
        this.grade = grade;
    }

    public Course(String code, int credits){
        this(null, null, code, null, null, credits, "N/A");
        this.isAP = true;
    }

    public String getTerm(){
        return term;
    }

    public String getDept() {
        return code.substring(0,4);
    }

    public String getYear(){
        return year;
    }

    public String getCode(){
        return code;
    }

    public String getTitle(){
        return title;
    }

    public ArrayList<String> getTypes(){
        return types;
    }

    public int getCredits(){
        return credits;
    }

    public boolean isAP(){
        return isAP;
    }

    public String getGrade(){
        return grade;
    }

    public String toString(){
        return code + "; " + title + "; " + types + "; " + term + "; " + year + "; " + credits;
    }
}
