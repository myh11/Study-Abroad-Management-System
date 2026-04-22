package com.example.admissionsystem.applications.dto;

import lombok.Data;

@Data
public class ApplicationDraftRequest {

    private StudentDraftDTO student;
    private ScoreDraftDTO score;
    private PersonalStatementDraftDTO personalStatement;
    private Long batchId;
    private String targetSchoolCode;
    private String targetMajorCode;

    @Data
    public static class StudentDraftDTO {
        private String name;
        private String gender;
        private String birthDate;
        private String currentSchool;
        private String grade;
        private String email;
        private String phone;
        private String idCardNo;
    }

    @Data
    public static class ScoreDraftDTO {
        private String transcriptSchoolName;
        private String termStart;
        private String termEnd;
        private Integer chinese;
        private Integer math;
        private Integer english;
        private Integer physics;
        private Integer chemistry;
        private Integer history;
        private Integer averageScore;
        private Integer failedSubjectCount;
        private Long fileId;
    }

    @Data
    public static class PersonalStatementDraftDTO {
        private String content;
    }
}
