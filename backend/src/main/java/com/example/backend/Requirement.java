package com.example.backend;

import java.util.ArrayList;
import java.util.Arrays;

public class Requirement {
    private String title;
    private ArrayList<String> courseGroups;
    private ArrayList<Requirement> sequences;
    private int numNeeded;
    private String numberRequirements;
    private String typeRequirements;
    private String gradeRequirement;
    private ArrayList<String> numReqs;
    private ArrayList<String> typeReqs;

    public static Requirement fromCourseGroups(String title, ArrayList<String> courseGroups, int numNeeded, String numberRequirements, String typeRequirements, String gradeRequirement) {
        return new Requirement(title, courseGroups, numNeeded, numberRequirements, typeRequirements, gradeRequirement, null);
    }
    
    public static Requirement fromSequences(String title, ArrayList<Requirement> sequences, int numNeeded, String gradeRequirement) {
        return new Requirement(title, null, numNeeded, null, null, gradeRequirement, sequences);
    }
    
    private Requirement(String title, ArrayList<String> courseGroups, int numNeeded, String numberRequirements, String typeRequirements, String gradeRequirement, ArrayList<Requirement> sequences) {
        this.title = title;
        this.courseGroups = courseGroups;
        this.numNeeded = numNeeded;
        this.numberRequirements = numberRequirements;
        this.typeRequirements = typeRequirements;
        this.gradeRequirement = gradeRequirement;
        this.numReqs = createNumReqs();
        this.typeReqs = createTypeReqs();
        this.sequences = sequences;
    }
    
    public boolean isSeq() {
        return courseGroups == null;
    }
    public String getTitle() {
        return title;
    }

    private ArrayList<String> createNumReqs(){
        if (numberRequirements != null) {
            ArrayList<String> list = new ArrayList<>(Arrays.asList(numberRequirements.toString().split(",(?![^()]*\\))")));
            list.replaceAll(String::trim);

            

            return list;
        } else {
            return null;
        }
    }

    private ArrayList<String> createTypeReqs(){
        if (typeRequirements != null) {
            ArrayList<String> list = new ArrayList<>(Arrays.asList(typeRequirements.toString().split(",(?![^()]*\\))")));
            list.replaceAll(String::trim);

            return list;
        } else {
            return null;
        }
    }

    public ArrayList<String> getNumReqs(){
        return numReqs;
    }

    public ArrayList<String> getTypeReqs(){
        return typeReqs;
    }

    public ArrayList<String> getCourseGroups() {
        return courseGroups;
    }

    public ArrayList<Requirement> getSequences() {
        return sequences;
    }

    public int getNumNeeded() {
        return numNeeded;
    }

    public String getNumberRequirements() {
        return numberRequirements;
    }

    public String getTypeRequirements() {
        return typeRequirements;
    }

    public String getGradeRequirement(){
        return gradeRequirement;
    }

    public boolean isElective() {
        return courseGroups.size() == 1 && !courseGroups.get(0).contains("-");
    }

    @Override
    public String toString() {
        return title + " (" + numNeeded + " needed): " + String.join(" | ", courseGroups) +
                (numberRequirements.isEmpty() ? "" : " [" + numberRequirements + "]") +
                (typeRequirements.isEmpty() ? "" : " [" + typeRequirements + "]");
    }
}
