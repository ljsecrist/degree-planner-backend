package com.example.backend;

import java.util.*;
import java.util.stream.Collectors;

public class Student {

    private int gradYr;
    private ArrayList<Course> courses;
    private ArrayList<Concentration> majors;
    private ArrayList<Concentration> minors;

    public Student(int gradYr, ArrayList<Course> courses, ArrayList<Concentration> majors, ArrayList<Concentration> minors) {
        this.gradYr = gradYr;
        this.courses = courses;
        this.majors = majors;
        this.minors = minors;
    }

    public int getGradYr() {
        return gradYr;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public ArrayList<Concentration> getMajors() {
        return majors;
    }

    public ArrayList<Concentration> getMinors() {
        return minors;
    }

    public void printProgress() {
        System.out.println("\n======== Student Progress ========");
        System.out.println("Graduation Year: " + gradYr);
        System.out.println("==================================\n");

        // Process majors
        for (Concentration major : majors) {
            System.out.println("Major: " + major.getName());
            printConcentrationProgress(major);
        }

        // Process minors
        for (Concentration minor : minors) {
            System.out.println("Minor: " + minor.getName());
            printConcentrationProgress(minor);
        }
    }

    /**
     * Processes each requirement for the given concentration.
     * A copy of the student's courses is made so that each requirement
     * “consumes” courses as they are used.
     */
    private void printConcentrationProgress(Concentration concentration) {
        List<Course> coursesLeft = new ArrayList<>(courses);
        for (Requirement req : concentration.getReqs()) {
            if (req.isSeq()) {
                System.out.println(processSequenceRequirement(req, coursesLeft));
            } else {
                System.out.println(processNonSequenceRequirement(req, coursesLeft));
            }
        }
    }

    /**
     * Processes a requirement that is made up of sequences (sub-requirements).
     */
    private String processSequenceRequirement(Requirement req, List<Course> coursesLeft) {
        StringBuilder result = new StringBuilder();
        int seqsFulfilled = 0;
        List<String[]> fulfilledCourseLists = new ArrayList<>();
        List<String[]> sequences = new ArrayList<>();

        // Build a list of sequence arrays from each sub-requirement.
        for (Requirement subReq : req.getSequences()) {
            String[] seq = new String[subReq.getNumNeeded()];
            for (int j = 0; j < subReq.getNumNeeded(); j++) {
                seq[j] = subReq.getCourseGroups().get(j);
            }
            sequences.add(seq);
        }

        int counter = 0;
        for (Requirement subReq : req.getSequences()) {
            int numFulfilled = 0;
            String[] fulfilledCourses = new String[subReq.getNumNeeded()];
            for (String course : subReq.getCourseGroups()) {
                List<Course> usedCourses = new ArrayList<>();
                for (Course myCourse : coursesLeft) {
                    if (course.equals(myCourse.getCode())
                            && (subReq.getGradeRequirement().equals("") || compareGrades(myCourse.getGrade(), subReq.getGradeRequirement()))) {
                        fulfilledCourses[numFulfilled] = course;
                        usedCourses.add(myCourse);
                        numFulfilled++;
                        if (numFulfilled >= subReq.getNumNeeded()) {
                            seqsFulfilled++;
                            break;
                        }
                    }
                }
                removeUsedCourses(coursesLeft, usedCourses);
                if (numFulfilled >= subReq.getNumNeeded()) {
                    // Remove the corresponding sequence since it has been completed
                    sequences.remove(counter);
                    counter--; // Adjust counter because of removal
                    break;
                }
            }
            fulfilledCourseLists.add(fulfilledCourses);
            counter++;
            if (seqsFulfilled >= req.getNumNeeded()) {
                break;
            }
        }

        // Print out progress based on how many sequences were fulfilled.
        if (seqsFulfilled >= req.getNumNeeded()) {
            result.append("[X] " + req.getTitle() + " (Completed)\n");
            String fulfilledStr = fulfilledCourseLists.stream()
                    .sorted((a, b) -> Double.compare(
                            (double) Arrays.stream(b).filter(Objects::nonNull).count() / b.length,
                            (double) Arrays.stream(a).filter(Objects::nonNull).count() / a.length))
                    .limit(req.getNumNeeded())
                    .map(arr -> Arrays.stream(arr).filter(Objects::nonNull).collect(Collectors.joining(", ")))
                    .collect(Collectors.joining("; "));
                    result.append("   Fulfilled by: " + fulfilledStr + "\n");
        } else if (seqsFulfilled > 0) {
            result.append("[~] " + req.getTitle() + " (Partially Completed)\n");
            String completedSequences = fulfilledCourseLists.stream()
                    .filter(arr -> Arrays.stream(arr).noneMatch(Objects::isNull))
                    .map(arr -> "(" + String.join(", ", arr) + ")")
                    .collect(Collectors.joining("; "));
                    result.append("   Sequences Completed: " + completedSequences+ "\n");
            String sequencesLeftStr = formatSequences(sequences);
            result.append("   Sequences Left: " + (req.getNumNeeded() - seqsFulfilled)
                    + " of " + sequencesLeftStr);
                    result.append(gradeRequirementSuffix(req.getGradeRequirement()) + "\n");
        } else {
            result.append("[ ] " + req.getTitle() + " (Not Completed)\n");
            String sequencesLeftStr = formatSequences(sequences);
            result.append("   Sequences Left: " + (req.getNumNeeded() - seqsFulfilled)
                    + " of " + sequencesLeftStr);
                    result.append(gradeRequirementSuffix(req.getGradeRequirement()) + "\n");
        }
        result.append("\n");

        return result.toString();
    }

    /**
     * Processes a requirement that is not based on sequences.
     */
    private String processNonSequenceRequirement(Requirement req, List<Course> coursesLeft) {
        StringBuilder result = new StringBuilder();
        int numFulfilled = 0;
        String[] fulfilledCourses = new String[req.getNumNeeded()];
        ArrayList<int[]> numRequirements = new ArrayList<>();
        ArrayList<Map.Entry<Integer, String>> typeRequirements = new ArrayList<>();
        ArrayList<String> courseList = new ArrayList<>(req.getCourseGroups());

        if (!req.getNumberRequirements().equals("")) {
            for (String pair : req.getNumReqs()) {
                numRequirements.add(new int[]{
                        Integer.parseInt(pair.substring(0, 2).replaceAll("[^0-9]", "")),
                        Integer.parseInt(pair.substring(2).replaceAll("[^0-9]", ""))
                });
            }
        }

        if(!req.getTypeRequirements().equals("")) {
            for (String pair : req.getTypeReqs()) {
                typeRequirements.add(new AbstractMap.SimpleEntry<>(
                    Integer.parseInt(pair.substring(0, 2).replaceAll("[^0-9]", "")),
                    pair.replaceAll(".*\\((.*?)\\).*", "$1")));
            }
        }

        for (String course : req.getCourseGroups()) {
            List<Course> usedCourses = new ArrayList<>();
            for (Course myCourse : coursesLeft) {
                if (((course.contains("XXX") && myCourse.getCode().toString().contains(course.substring(0,3)) 
                        && !myCourse.getCode().contains("295H") && !myCourse.getCode().contains("296H") && !myCourse.getCode().contains("297H")) || 
                        course.equals(myCourse.getCode())) && 
                        (req.getGradeRequirement().equals("") || compareGrades(myCourse.getGrade(), req.getGradeRequirement())) &&
                        !myCourse.getGrade().equals("W")) {
                        
                            if (req.getNumberRequirements().equals("") && req.getTypeRequirements().equals("")) {
                            fulfilledCourses[numFulfilled] = myCourse.getCode();
                            usedCourses.add(myCourse);
                            numFulfilled++;
                            if(!course.contains("XXX")) {
                                courseList.remove(course);
                            }
                            if (numFulfilled >= req.getNumNeeded()) {
                                break;
                            }
                        } else if (req.getTypeRequirements().equals("")){
                            boolean fulfillsRequirement = false;
                            for (int[] pair : numRequirements) {
                                if (Integer.parseInt(myCourse.getCode().substring(4, 7)) >= pair[1]) {
                                    if (req.getNumNeeded() - numFulfilled >= pair[0]) {
                                        fulfillsRequirement = true;
                                        if(!course.contains("XXX")) {
                                            courseList.remove(course);
                                        }
                                        if (pair[0] > 0) {
                                            pair[0] = pair[0] - 1;
                                        }
                                    }
                                }
                            }
                            if (fulfillsRequirement) {
                                fulfilledCourses[numFulfilled] = myCourse.getCode();
                                usedCourses.add(myCourse);
                                numFulfilled++;
                            }
                            if (numFulfilled >= req.getNumNeeded()) {
                                break;
                            }
                        } else if (req.getNumberRequirements().equals("")) {
                            boolean fulfillsRequirement = false;
                            for (Map.Entry<Integer, String> pair : typeRequirements) {
                                for(String type : myCourse.getTypes()){
                                    if (pair.getValue().contains(type)) {

                                        if (req.getNumNeeded() - numFulfilled >= pair.getKey()) {
                                            fulfillsRequirement = true;
                                            if(!course.contains("XXX")) {
                                                courseList.remove(course);
                                            }
                                            if (pair.getKey() > 0) {
                                                for (int i = 0; i < typeRequirements.size(); i++) {
                                                    if (pair.getValue().equals(typeRequirements.get(i).getValue())){
                                                        typeRequirements.set(i, Map.entry(pair.getKey() - 1, pair.getValue()));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (fulfillsRequirement) {
                                fulfilledCourses[numFulfilled] = myCourse.getCode();
                                usedCourses.add(myCourse);
                                numFulfilled++;
                            }
                            if (numFulfilled >= req.getNumNeeded()) {
                                break;
                            }
                        } else {
                            boolean fulfillsRequirement = false;
                            for (int[] pair : numRequirements) {
                                if (Integer.parseInt(myCourse.getCode().substring(4, 7)) >= pair[1]) {
                                    if (req.getNumNeeded() - numFulfilled >= pair[0]) {
                                        fulfillsRequirement = true;
                                        if (pair[0] > 0) {
                                            pair[0] = pair[0] - 1;
                                            for (Map.Entry<Integer, String> pair1 : typeRequirements) {
                                                for(String type : myCourse.getTypes()){
                                                    if (pair1.getValue().contains(type)) {
                                                        if (req.getNumNeeded() - numFulfilled >= pair1.getKey()) {
                                                            fulfillsRequirement = true;
                                                            if (pair1.getKey() > 0) {
                                                                for (int i = 0; i < typeRequirements.size(); i++) {
                                                                    if (pair1.getValue().equals(typeRequirements.get(i).getValue())){
                                                                        typeRequirements.set(i, Map.entry(pair1.getKey() - 1, pair1.getValue()));
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            fulfillsRequirement = false;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    for (Map.Entry<Integer, String> pair1 : typeRequirements) {
                                        for(String type : myCourse.getTypes()){
                                            if (type.equals(pair1.getValue())) {
                                                if (req.getNumNeeded() - numFulfilled >= pair1.getKey()) {
                                                    fulfillsRequirement = true;
                                                    if (pair1.getKey() > 0) {
                                                        for (int i = 0; i < typeRequirements.size(); i++) {
                                                            if (pair1.getValue().equals(typeRequirements.get(i).getValue())){
                                                                typeRequirements.set(i, Map.entry(pair1.getKey() - 1, pair1.getValue()));
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    fulfillsRequirement = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (fulfillsRequirement) {
                                fulfilledCourses[numFulfilled] = myCourse.getCode();
                                if(!course.contains("XXX")) {
                                    courseList.remove(course);
                                }
                                usedCourses.add(myCourse);
                                numFulfilled++;
                            }
                            if (numFulfilled >= req.getNumNeeded()) {
                                break;
                            }
                        }
                }
                if (numFulfilled >= req.getNumNeeded()) {
                    break;
                }
            }
            removeUsedCourses(coursesLeft, usedCourses);
            if (numFulfilled >= req.getNumNeeded()) {
                break;
            }
        }

        // Print out progress for the non-sequence requirement.
        if (numFulfilled >= req.getNumNeeded()) {
            result.append("[X] " + req.getTitle() + " (Completed)\n");
            result.append("   Fulfilled by: " + String.join(", ", fulfilledCourses) + "\n");
        } else if (numFulfilled > 0) {
            result.append("[~] " + req.getTitle() + " (Partially Completed)\n");
            String completed = Arrays.stream(fulfilledCourses)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
                    result.append("   Completed: " + completed + "\n");
                    result.append("   Still Needed: " + (req.getNumNeeded() - numFulfilled)
                    + " of " + String.join("; ", courseList));
                    result.append(formatRequirements(numRequirements, req.getNumberRequirements(), typeRequirements, req.getTypeRequirements()));
                    result.append(gradeRequirementSuffix(req.getGradeRequirement()) + "\n");
        } else {
            result.append("[ ] " + req.getTitle() + " (Not Completed)\n");
            result.append("   Still Needed: " + (req.getNumNeeded() - numFulfilled)
                    + " of " + String.join("; ", courseList));
                    result.append(formatRequirements(numRequirements, req.getNumberRequirements(), typeRequirements, req.getTypeRequirements()));
                    result.append(gradeRequirementSuffix(req.getGradeRequirement()) + "\n");
        }
        result.append("\n");

        return result.toString();
    }

    /**
     * Returns a suffix string to append when a grade requirement exists.
     */
    private String gradeRequirementSuffix(String gradeRequirement) {
        if (gradeRequirement.equals("")) {
            return "";
        }
        return " with a minimum grade of " + gradeRequirement;
    }

    /**
     * Formats a list of sequences (each represented as an array) into a string.
     */
    private String formatSequences(List<String[]> sequences) {
        return sequences.stream()
                .map(arr -> "(" + String.join(", ", arr) + ")")
                .collect(Collectors.joining("; "));
    }

    /**
     * Combines the formatting for both number and type requirements.
     */
    private String formatRequirements(ArrayList<int[]> numRequirements, String numberRequirements, 
            ArrayList<Map.Entry<Integer, String>> typeReqs, String typeRequirements) {

        if (numberRequirements.equals("") && typeRequirements.equals("")){
            return "";
        }

        StringBuilder result = new StringBuilder(" consisting of:");

        // First, check for special type requirements (WAC or WAC-R).
        int specialCount = 0;
        ArrayList<String> specialTypes = new ArrayList<>();
        // Use an iterator to remove special entries from typeReqs.
        Iterator<Map.Entry<Integer, String>> iter = typeReqs.iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, String> entry = iter.next();
            String type = entry.getValue();
            if (type.equals("WAC") || type.equals("WAC-R")) {
                specialCount += entry.getKey();
                if (!specialTypes.contains(type)) {
                    specialTypes.add(type);
                }
                iter.remove(); // remove so it doesn't show up in the regular type reqs
            }
        }
        // Combine special type names if there are more than one.
        String specialTypeStr = "";
        if (!specialTypes.isEmpty()) {
            if (specialTypes.size() == 1) {
                specialTypeStr = specialTypes.get(0);
            } else {
                specialTypeStr = String.join(" or ", specialTypes);
            }
        }

        // Build the number requirements part if applicable.
        if (!numberRequirements.equals("")) {
            StringBuilder sb = new StringBuilder("\n");
            for (int i = 0; i < numRequirements.size(); i++) {
                int[] pair = numRequirements.get(i);
                int count = pair[0];
                // Adjust count based on the next requirement if available.
                if (i < numRequirements.size() - 1) {
                    count -= numRequirements.get(i + 1)[0];
                }
                if (count > 0) {
                    // If the requirement is for courses numbered >=300 and there is a special type req,
                    // split the requirement into a general and a special part.
                    if (pair[1] >= 300 && specialCount > 0) {
                        int generalOnlyCount = count - specialCount;
                        if (generalOnlyCount > 0) {
                            sb.append("                    * ")
                            .append(generalOnlyCount)
                            .append(" course(s) numbered")
                            .append(" >=")
                            .append(pair[1])
                            .append("\n");
                        }
                        sb.append("                    * ")
                        .append(specialCount)
                        .append(" course(s) numbered")
                        .append(" >=")
                        .append(pair[1])
                        .append(" and is also a ")
                        .append(specialTypeStr)
                        .append("\n");
                    } else {
                        sb.append("                    * ")
                        .append(count)
                        .append(" course(s) numbered")
                        .append(" >=")
                        .append(pair[1])
                        .append("\n");
                    }
                }
            }
            // Remove the trailing newline.
            if (sb.length() > " consisting of:".length()) {
                sb.setLength(sb.length() - 1);
                result.append(sb);
            }
        }

        // Build the type requirements part if applicable.
        if (!typeRequirements.equals("")) {
            StringBuilder sb = new StringBuilder("\n");
            for (Map.Entry<Integer, String> pair : typeReqs) {
                if (pair.getKey() > 0) {
                    sb.append("                    * ")
                    .append(pair.getKey())
                    .append(" course(s) must be typed ")
                    .append(pair.getValue())
                    .append(", ");
                }
            }
            // Remove the trailing comma and space.
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 2);
                result.append(sb);
            }
        }

        return result.toString();
    }




    /**
     * Removes all courses from coursesLeft that appear in usedCourses.
     */
    private void removeUsedCourses(List<Course> coursesLeft, List<Course> usedCourses) {
        coursesLeft.removeAll(usedCourses);
    }

    public static boolean compareGrades(String grade1, String grade2) {
        Map<String, Double> gradeScale = new HashMap<>(Map.ofEntries(
                Map.entry("CIP", 4.3), Map.entry("N/A", 4.3), Map.entry("T", 4.3),
                Map.entry("A", 4.0), Map.entry("A-", 3.7),
                Map.entry("B+", 3.3), Map.entry("B", 3.0), Map.entry("B-", 2.7),
                Map.entry("C+", 2.3), Map.entry("C", 2.0), Map.entry("C-", 1.7),
                Map.entry("D+", 1.3), Map.entry("D", 1.0), Map.entry("D-", 0.7),
                Map.entry("F", 0.0)
        ));

        return gradeScale.getOrDefault(grade1.trim(), -1.0) >= gradeScale.getOrDefault(grade2, -1.0);
    }

    @Override
    public String toString() {
        return "gradYr: " + gradYr + "\ncourses: " + courses + "\nmajors: " + majors + "\nminors: " + minors;
    }

    public String getProgressString() {
        StringBuilder progress = new StringBuilder();
        progress.append("\n======== Student Progress ========\n");
        progress.append("Graduation Year: ").append(gradYr).append("\n");
        progress.append("==================================\n\n");
    
        // Process majors
        for (Concentration major : majors) {
            progress.append("Major: ").append(major.getName()).append("\n");
            progress.append(getConcentrationProgress(major));
        }
    
        // Process minors
        for (Concentration minor : minors) {
            progress.append("Minor: ").append(minor.getName()).append("\n");
            progress.append(getConcentrationProgress(minor));
        }
    
        return progress.toString();
    }
    
    private String getConcentrationProgress(Concentration concentration) {
        StringBuilder result = new StringBuilder();
        List<Course> coursesLeft = new ArrayList<>(courses);
    
        for (Requirement req : concentration.getReqs()) {
            if (req.isSeq()) {
                result.append(processSequenceRequirement(req, coursesLeft));
            } else {
                result.append(processNonSequenceRequirement(req, coursesLeft));
            }
        }
    
        return result.toString();
    }
    
}