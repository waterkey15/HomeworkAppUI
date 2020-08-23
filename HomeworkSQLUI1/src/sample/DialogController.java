package sample;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import sample.model.HomeworkDataSource;
import sample.model.HomeworkDataSource;
import sample.model.HomeworkItem;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DialogController {

    @FXML
    private TextField lectureTextField;

    @FXML
    private TextArea detailsTextArea;

    @FXML
    private DatePicker deadlineDatePicker;



    private HomeworkDataSource dataSource;


   public HomeworkItem processResult(){

       dataSource = new HomeworkDataSource();


       String lectureName = lectureTextField.getText();
       String details = detailsTextArea.getText();
       LocalDate deadline = deadlineDatePicker.getValue();
      // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");



       HomeworkItem item = new HomeworkItem(lectureName, details, deadline);

       return item;
   }


    public void editHomework(HomeworkItem homeworkItem){
        lectureTextField.setText(homeworkItem.getLecture());
        detailsTextArea.setText(homeworkItem.getDetails());
        deadlineDatePicker.setValue(homeworkItem.getDeadline());

    }

    public HomeworkItem updateHomework(){

        String lectureName = lectureTextField.getText();
        String details = detailsTextArea.getText();
        LocalDate deadline = deadlineDatePicker.getValue();

        HomeworkItem item = new HomeworkItem(lectureName, details, deadline);

        return item;

    }
}
