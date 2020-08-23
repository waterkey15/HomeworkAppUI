package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import sample.model.HomeworkDataSource;
import sample.model.HomeworkItem;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private ListView<HomeworkItem> homeworkItemsListView;
    @FXML
    private TextArea homeworkDetailsTextArea;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;

    @FXML
    private ToggleButton filterThisWeeksItems;

    @FXML
    private ToggleButton filterLongTermProjects;

    @FXML
    private ToggleButton filterPassedDeadlines;


    private DateTimeFormatter formatter;

    private HomeworkDataSource dataSource;


    private Predicate<HomeworkItem> wantPassedDeadlines;
    private Predicate<HomeworkItem> wantLongTermProjects;
    private Predicate<HomeworkItem> wantThisWeeksItems;
    private Predicate<HomeworkItem> wantTodaysItems;
    private Predicate<HomeworkItem> wantAllItems;


    private FilteredList<HomeworkItem> filteredList;




    @FXML
    public void initialize(){
        dataSource = new HomeworkDataSource();
        dataSource.open();

        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                HomeworkItem item = homeworkItemsListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);

        homeworkItemsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HomeworkItem>() {
            @Override
            public void changed(ObservableValue<? extends HomeworkItem> observableValue, HomeworkItem oldValue, HomeworkItem newValue) {
                if(newValue != null){
                    HomeworkItem item = homeworkItemsListView.getSelectionModel().getSelectedItem();
                    homeworkDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });


        wantAllItems = new Predicate<HomeworkItem>() {
            @Override
            public boolean test(HomeworkItem item) {
                return true;
            }
        };


        wantTodaysItems = new Predicate<HomeworkItem>() {
            @Override
            public boolean test(HomeworkItem homeworkItem) {
                return homeworkItem.getDeadline().equals(LocalDate.now());
            }
        };

        wantThisWeeksItems = new Predicate<HomeworkItem>() {
            @Override
            public boolean test(HomeworkItem homeworkItem) {
                return homeworkItem.getDeadline().isBefore(LocalDate.now().plusDays(8)) &&
                        homeworkItem.getDeadline().isAfter(LocalDate.now().minusDays(1));
            }
        };

        wantLongTermProjects = new Predicate<HomeworkItem>() {
            @Override
            public boolean test(HomeworkItem homeworkItem) {
                return homeworkItem.getDeadline().isBefore(LocalDate.now().plusDays(90)) &&
                        homeworkItem.getDeadline().isAfter(LocalDate.now().plusDays(14));
            }
        };

        wantPassedDeadlines = new Predicate<HomeworkItem>() {
            @Override
            public boolean test(HomeworkItem homeworkItem) {
                return homeworkItem.getDeadline().isBefore(LocalDate.now());
            }
        };

        filteredList = new FilteredList<HomeworkItem>(dataSource.queryHomeworkItems(), wantAllItems);
        SortedList<HomeworkItem> sortedList = new SortedList<>(filteredList, new Comparator<HomeworkItem>() {
            @Override
            public int compare(HomeworkItem o1, HomeworkItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });

        homeworkItemsListView.setItems(sortedList);
        homeworkItemsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        homeworkItemsListView.getSelectionModel().selectFirst();


        homeworkItemsListView.setCellFactory(new Callback<ListView<HomeworkItem>, ListCell<HomeworkItem>>() {
            @Override
            public ListCell<HomeworkItem> call(ListView<HomeworkItem> homeworkItemListView) {
                ListCell<HomeworkItem> cell = new ListCell<>(){
                    @Override
                    protected void updateItem(HomeworkItem item, boolean b) {
                        super.updateItem(item, b);
                        if(b){
                            setText(null);
                        }else{
                            setText(item.getLecture());
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isEmpty)->{
                            if(isEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        });
    }
    @FXML

    public void showNewItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new Homework Item");
        dialog.setHeaderText("Use this dialog to create new homework item");


        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("homeworkItemDialog.fxml"));

        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch (IOException e){
            System.out.println("Couldn't load the Dialog");
            e.getMessage();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){

            DialogController controller = fxmlLoader.getController();

            HomeworkItem newItem = controller.processResult();
            formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


            String deadlineString = newItem.getDeadline().format(formatter).toString();
           // System.out.println(newItem.getDeadline().format(formatter).toString());

            dataSource.insertHomework(newItem.getLecture(), newItem.getDetails(), deadlineString);

            initialize();
        }


    }

    @FXML
    public void deleteItem(HomeworkItem item){
        HomeworkItem itemToBeDeleted = homeworkItemsListView.getSelectionModel().getSelectedItem();



        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Homework Item");
        alert.setHeaderText("Delete Homework Item for the class " + item.getLecture());
        alert.setContentText("Are you sure?  Press OK to confirm, or cancel to Back out.");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)) {
                dataSource.deleteItem(item);
                initialize();
        }
    }

    @FXML
    public void handleFilterButton(){
        HomeworkItem selectedItem = homeworkItemsListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(wantTodaysItems);
            if(filteredList.isEmpty()){
                homeworkDetailsTextArea.clear();
                deadlineLabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                homeworkItemsListView.getSelectionModel().select(selectedItem);
            }else {
                homeworkItemsListView.getSelectionModel().selectFirst();
            }
        }else {
            filteredList.setPredicate(wantAllItems);
            homeworkItemsListView.getSelectionModel().select(selectedItem);
        }
    }


    @FXML
    public void handleThisWeekButton(){
        HomeworkItem selectedItem = homeworkItemsListView.getSelectionModel().getSelectedItem();
        if(filterThisWeeksItems.isSelected()){
            filteredList.setPredicate(wantThisWeeksItems);
            if(filteredList.isEmpty()){
                homeworkDetailsTextArea.clear();
                deadlineLabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                homeworkItemsListView.getSelectionModel().select(selectedItem);
            }else{
                homeworkItemsListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(wantAllItems);
            homeworkItemsListView.getSelectionModel().select(selectedItem);
        }

    }

    @FXML
    public void handleLongTermProjects(){
        HomeworkItem selectedItem = homeworkItemsListView.getSelectionModel().getSelectedItem();

        if(filterLongTermProjects.isSelected()){
            filteredList.setPredicate(wantLongTermProjects);
            if(filteredList.isEmpty()){
                homeworkDetailsTextArea.clear();
                deadlineLabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                homeworkItemsListView.getSelectionModel().select(selectedItem);
            }else{
                homeworkItemsListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(wantLongTermProjects);
            homeworkItemsListView.getSelectionModel().select(selectedItem);
        }

    }
    @FXML
    public void handlePassedDeadlines(){
        HomeworkItem selectedItem = homeworkItemsListView.getSelectionModel().getSelectedItem();

        if(filterPassedDeadlines.isSelected()){
            filteredList.setPredicate(wantPassedDeadlines);
            if(filteredList.isEmpty()){
                homeworkDetailsTextArea.clear();
                deadlineLabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                homeworkItemsListView.getSelectionModel().select(selectedItem);
            }else{
                homeworkItemsListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(wantPassedDeadlines);
            homeworkItemsListView.getSelectionModel().select(selectedItem);
        }

    }

    @FXML
    public void handleEditButton(){
        System.out.println("edit button pressed");
        HomeworkItem itemToBeEdited = homeworkItemsListView.getSelectionModel().getSelectedItem();
        if(itemToBeEdited == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Homework selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a Homework to edit");
            alert.showAndWait();
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Edit Homework");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("homeworkItemDialog.fxml"));

        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch (IOException e){
            e.getMessage();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        DialogController controller = fxmlLoader.getController();
        controller.editHomework(itemToBeEdited);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == (ButtonType.OK)){
            System.out.println("ok pressed");
            HomeworkItem item = controller.updateHomework();
            formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


            String deadlineString = item.getDeadline().format(formatter).toString();
            // System.out.println(newItem.getDeadline().format(formatter).toString());

            dataSource.editItem(item.getLecture(), item.getDetails(), deadlineString, itemToBeEdited);

            initialize();

        }




    }


    @FXML
    public void handleAllItems(){
        initialize();
    }






    @FXML
    public void handleDeleteButton(){
        HomeworkItem itemTobeDeleted = homeworkItemsListView.getSelectionModel().getSelectedItem();
        deleteItem(itemTobeDeleted);
    }

    @FXML
    public void handleExitButton(){
        Platform.exit();
    }


}














