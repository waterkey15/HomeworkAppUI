package sample.model;

import java.time.LocalDate;

public class HomeworkItem {

    private String lecture;
    private String details;
    private LocalDate deadline;

    public HomeworkItem(String lecture, String details, LocalDate deadline) {
        this.lecture = lecture;
        this.details = details;
        this.deadline = deadline;
    }

    public String getLecture() {
        return lecture;
    }

    public void setLecture(String lecture) {
        this.lecture = lecture;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
