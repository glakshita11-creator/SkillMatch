package com.skillmatch.util;

import com.skillmatch.dto.ApplicationResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class CsvExporter {

    public static String exportApplicationsToCsv(List<ApplicationResponse> applications) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // CSV Header
        pw.println("Application ID,Candidate Name,Email,Phone,Department,Degree,Graduation Year,CGPA,Opportunity Title,Company,Workplace Type,Status,Applied Date,Interview Date,Withdrawn,Recruiter Notes");

        if (applications != null) {
            for (ApplicationResponse app : applications) {
                pw.print(escapeCsv(String.valueOf(app.getId())));
                pw.print(",");
                pw.print(escapeCsv(app.getStudentName()));
                pw.print(",");
                pw.print(escapeCsv(app.getStudentEmail()));
                pw.print(",");
                pw.print(escapeCsv(app.getStudentPhone()));
                pw.print(",");
                pw.print(escapeCsv(app.getStudentDepartment()));
                pw.print(",");
                pw.print(escapeCsv(app.getStudentDegree()));
                pw.print(",");
                pw.print(escapeCsv(String.valueOf(app.getStudentGraduationYear())));
                pw.print(",");
                pw.print(escapeCsv(app.getStudentCgpa() != null ? app.getStudentCgpa().toString() : ""));
                pw.print(",");
                pw.print(escapeCsv(app.getOpportunityTitle()));
                pw.print(",");
                pw.print(escapeCsv(app.getCompanyName()));
                pw.print(",");
                pw.print(escapeCsv(app.getLocation()));
                pw.print(",");
                pw.print(escapeCsv(app.getStatus() != null ? app.getStatus().getDisplayName() : ""));
                pw.print(",");
                pw.print(escapeCsv(app.getAppliedAt() != null ? app.getAppliedAt().toString() : ""));
                pw.print(",");
                pw.print(escapeCsv(app.getInterviewDate() != null ? app.getInterviewDate().toString() : ""));
                pw.print(",");
                pw.print(escapeCsv(app.isWithdrawn() ? "Yes" : "No"));
                pw.print(",");
                pw.println(escapeCsv(app.getRecruiterNotes()));
            }
        }

        pw.flush();
        return sw.toString();
    }

    private static String escapeCsv(String data) {
        if (data == null) {
            return "\"\"";
        }
        String escaped = data.replaceAll("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
