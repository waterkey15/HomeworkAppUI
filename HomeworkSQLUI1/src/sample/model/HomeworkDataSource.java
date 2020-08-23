package sample.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomeworkDataSource {

    public static final String DB_NAME = "testDate.db";
    public static final String CONNECTION_STRING = "jdbc:sqlite:/Users/dorukkilic/IdeaProjects/HomeworkSqlUI1/" + DB_NAME;

    public static final String TABLE_HOMEWORK = "testTable";
    public static final String COLUMN_LECTURE = "lecture";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_DEADLINE = "deadline";

    public static final int INDEX_LECTURE = 1;
    public static final int INDEX_DETAILS = 2;
    public static final int INDEX_DEADLINE = 3;


    public static final String INSERT_HOMEWORK = "INSERT INTO " + TABLE_HOMEWORK + '(' + COLUMN_LECTURE + ", " +
            COLUMN_DETAILS + ", " + COLUMN_DEADLINE + ") VALUES(?,?,?)";

    public static final String DELETE_HOMEWORK = "DELETE FROM " + TABLE_HOMEWORK + " WHERE lecture = ?";

    public static final String EDIT_HOMEWORK = "UPDATE " + TABLE_HOMEWORK + " SET " + COLUMN_LECTURE + " = ?, " +
            COLUMN_DETAILS + " = ?, " + COLUMN_DEADLINE + " = ? WHERE " + COLUMN_LECTURE + " = ? AND " +
            COLUMN_DETAILS + " = ? AND " + COLUMN_DEADLINE + " = ?";




    PreparedStatement insertIntoHomework;
    PreparedStatement editHomework;
    PreparedStatement deleteFromHomework;


    private Connection conn;



    private static HomeworkDataSource instance;
    private DateTimeFormatter formatter;


    public HomeworkDataSource() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public static HomeworkDataSource getInstance() {
        return instance;
    }


    public boolean open(){
        try{
            conn = DriverManager.getConnection(CONNECTION_STRING);
            insertIntoHomework = conn.prepareStatement(INSERT_HOMEWORK, Statement.RETURN_GENERATED_KEYS);
            editHomework = conn.prepareStatement(EDIT_HOMEWORK, Statement.RETURN_GENERATED_KEYS);
            deleteFromHomework = conn.prepareStatement(DELETE_HOMEWORK, Statement.RETURN_GENERATED_KEYS);

            return true;
        }catch (SQLException e){
            System.out.println("coudlnt conenct to sql");
            return false;
        }
    }

    public void close(){
        try{
            if(insertIntoHomework != null){
                insertIntoHomework.close();
            }
        }catch (SQLException e){
            System.out.println("Couldnt close connection");
            e.getMessage();
        }
    }



    public ObservableList<HomeworkItem> queryHomeworkItems(){
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(TABLE_HOMEWORK);

        try(Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery(sb.toString())){
            ObservableList<HomeworkItem> homeworkItems = FXCollections.observableArrayList();
            while(results.next()){
                String lecture = results.getString(INDEX_LECTURE);
                String details = results.getString(INDEX_DETAILS);
                LocalDate deadline = LocalDate.parse(results.getString(INDEX_DEADLINE), formatter);
                HomeworkItem homeworkItem = new HomeworkItem(lecture, details, deadline);
                homeworkItems.add(homeworkItem);
            }
            return homeworkItems;



        }catch (SQLException e){
            System.out.println("couldnt load query ");
            e.getMessage();
            return null;
        }

    }

    public void insertHomework(String lecture, String details, String deadline){
        try{
            conn.setAutoCommit(false);

            insertIntoHomework.setString(1, lecture);
            insertIntoHomework.setString(2, details);
            insertIntoHomework.setString(3, deadline);

            int affectedRows = insertIntoHomework.executeUpdate();
            if(affectedRows == 1) {
                conn.commit();
            } else {
                throw new SQLException("The Homework insert failed");
            }

        }catch (Exception e){
            System.out.println("Insert homework exception: " + e.getMessage());
            try {
                System.out.println("Performing rollback");
                conn.rollback();
            } catch(SQLException e2) {
                System.out.println("Oh boy! Things are really bad! " + e2.getMessage());
            }
        }finally {
            try {
                System.out.println("Resetting default commit behavior");
                conn.setAutoCommit(true);
            } catch(SQLException e) {
                System.out.println("Couldn't reset auto-commit! " + e.getMessage());
            }

        }
    }


    public void deleteItem(HomeworkItem item){
        try{
            deleteFromHomework.setString(1, item.getLecture());
            deleteFromHomework.executeUpdate();

        }catch (SQLException e){
            e.getMessage();
        }
    }


    public void editItem(String lecture, String details, String deadline, HomeworkItem itemToBeEdited){
        try{
            editHomework.setString(1, lecture);
            editHomework.setString(2, details);
            editHomework.setString(3, deadline);
            editHomework.setString(4, itemToBeEdited.getLecture());
            editHomework.setString(5, itemToBeEdited.getDetails());

            formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String deadlineString = itemToBeEdited.getDeadline().format(formatter).toString();

            editHomework.setString(6, deadlineString);



            editHomework.executeUpdate();

        }catch (SQLException e){
            System.out.println("Edit homework exception: " + e.getMessage());
            try {
                System.out.println("Performing rollback");
                conn.rollback();
            } catch(SQLException e2) {
                System.out.println("Oh boy! Things are really bad! " + e2.getMessage());
            }
        }

    }
}
