package sample.homeworkModel;

import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class HomeworkItem {
    private SimpleStringProperty lecture;
    private SimpleStringProperty details;
    private LocalDate deadline;

    public HomeworkItem(SimpleStringProperty lecture, SimpleStringProperty details, LocalDate deadline) {
        this.lecture = lecture;
        this.details = details;
        this.deadline = deadline;
    }

    public String getLecture() {
        return lecture.get();
    }

    public SimpleStringProperty lectureProperty() {
        return lecture;
    }

    public void setLecture(String lecture) {
        this.lecture.set(lecture);
    }

    public String getDetails() {
        return details.get();
    }

    public SimpleStringProperty detailsProperty() {
        return details;
    }

    public void setDetails(String details) {
        this.details.set(details);
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
