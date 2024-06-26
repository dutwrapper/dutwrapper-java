package io.dutwrapper.dutwrapper.model.accounts;

import java.io.Serializable;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

// Details in http://daotao.dut.udn.vn/download2/Guide_Dangkyhoc.pdf, page 28
public class SubjectCodeItem implements Serializable {
    // Area 1
    @SerializedName("subjectid")
    private String subjectId = "";
    // Area 2
    @SerializedName("schoolyearid")
    private String schoolYearId = "";
    // Area 3
    @SerializedName("studentyearid")
    private String studentYearId = "";
    // Area 4
    @SerializedName("classid")
    private String classId = "";

    public SubjectCodeItem() {

    }

    public SubjectCodeItem(String studentYearId, String classId) {
        this.studentYearId = studentYearId;
        this.classId = classId;
    }

    public SubjectCodeItem(String subjectId, String schoolYearId, String studentYearId, String classId) {
        this.subjectId = subjectId;
        this.schoolYearId = schoolYearId;
        this.studentYearId = studentYearId;
        this.classId = classId;
    }

    public SubjectCodeItem(String input) {
        if (input.split("\\.").length == 4) {
            this.subjectId = input.split("\\.")[0];
            this.schoolYearId = input.split("\\.")[1];
            this.studentYearId = input.split("\\.")[2];
            this.classId = input.split("\\.")[3];
        } else if (input.split("\\.").length == 2) {
            this.studentYearId = input.split("\\.")[0];
            this.classId = input.split("\\.")[1];
        }
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSchoolYearId() {
        return schoolYearId;
    }

    public void setSchoolYearId(String schoolYearId) {
        this.schoolYearId = schoolYearId;
    }

    public String getStudentYearId() {
        return studentYearId;
    }

    public void setStudentYearId(String studentYearId) {
        this.studentYearId = studentYearId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(Boolean twoLastDigit) {
        if (twoLastDigit)
            return String.format("%s.%s", studentYearId, classId);
        else
            return String.format("%s.%s.%s.%s", subjectId, schoolYearId, studentYearId, classId);
    }

    public Boolean equalsTwoDigits(SubjectCodeItem codeItem) {
        return Objects.equals(this.studentYearId, codeItem.studentYearId) &&
                Objects.equals(this.classId, codeItem.classId);
    }

    public Boolean equals(SubjectCodeItem codeItem) {
        return Objects.equals(this.subjectId, codeItem.subjectId) &&
                Objects.equals(this.studentYearId, codeItem.studentYearId) &&
                this.equalsTwoDigits(codeItem);
    }
}
