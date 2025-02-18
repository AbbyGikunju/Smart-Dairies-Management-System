/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package java.controller;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.Duration;
import model.DatabaseConnection;
import model.MilkSearchModel;
import org.controlsfx.control.Notifications;

/**
 *
 * @author ngoni
 */
public class MilkRecordsController implements Initializable {

    @FXML
    private DatePicker milkingDate;
    @FXML
    private TextField afternoonQuantity, cowIDTextField, cowNameTextField, eveningQuantity, morningQuantity, searchCow;
    @FXML
    private Button loadDataButton, edit, milkUpdate, milkSave, milkDelete;
    @FXML
    private TableView<MilkSearchModel> milkRecords;
    @FXML
    private TableColumn<MilkSearchModel, Double> amLitresColumn;
    @FXML
    private TableColumn<MilkSearchModel, Double> noonLitresColumn;
    @FXML
    private TableColumn<MilkSearchModel, Double> pmLitresColumn;
    @FXML
    private TableColumn<MilkSearchModel, Integer> cowIDColumn;
    @FXML
    private TableColumn<MilkSearchModel, String> cowNameColumn;
    @FXML
    private TableColumn<MilkSearchModel, Date> milkingDateColumn;

    ObservableList<MilkSearchModel> milkSearchModelObservableList = FXCollections.observableArrayList();

    @FXML
    void milkDeletePressed() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Do you wish to continue");
        alert.showAndWait();
        if(alert.getResult() != ButtonType.OK){
            alert.close();
        }else if(alert.getResult() == ButtonType.OK){
            DatabaseConnection databaseConnection = new DatabaseConnection();
            Connection connection = databaseConnection.getConnection();
            String deleteQuery = "DELETE FROM dairy_farm.milking_records WHERE cowName = ?";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setString(1, cowNameTextField.getText());
                preparedStatement.executeUpdate();
            }catch (SQLException sqlException){
                sqlException.printStackTrace();
            }
            milkSearchModelObservableList.remove(milkRecords.getSelectionModel().getSelectedItem());
            milkingDate.setValue(null);
            cowNameTextField.clear();
            cowIDTextField.clear();
            morningQuantity.clear();
            afternoonQuantity.clear();
            eveningQuantity.clear();
            searchCow.clear();
            Notifications delete = Notifications.create()
                    .text("Record successfully deleted")
                    .position(Pos.TOP_RIGHT)
                    .hideCloseButton()
                    .hideAfter(Duration.seconds(3));
            delete.darkStyle();
            delete.showInformation();
        }
    }
    @FXML
    void milkSaveBtnPressed() {
        if(milkingDate.getValue() == null || cowNameTextField.getText().isEmpty() ||
                cowIDTextField.getText().isEmpty() || morningQuantity.getText().isEmpty() ||
                afternoonQuantity.getText().isEmpty() || eveningQuantity.getText().isEmpty()){
            Notifications notifications = Notifications.create()
                    .text("Please enter all details before saving.")
                    .position(Pos.TOP_RIGHT)
                    .hideCloseButton()
                    .hideAfter(Duration.seconds(3));
            notifications.darkStyle();
            notifications.showError();
        }
        else{
            add_MilkRecord();
            if(!(milkRecords.getItems().isEmpty())){
                addLastRow();
            }
            Notifications milkNotification = Notifications.create()
                    .text("Details successfully saved")
                    .position(Pos.TOP_RIGHT)
                    .hideCloseButton()
                    .hideAfter(Duration.seconds(3));
            milkNotification.darkStyle();
            milkNotification.showInformation();
            milkingDate.setValue(null);
            cowNameTextField.clear();
            cowIDTextField.clear();
            morningQuantity.clear();
            afternoonQuantity.clear();
            eveningQuantity.clear();
            searchCow.clear();
        }
    }
    @FXML
    void milkUpdatePressed() {
        Notifications update = Notifications.create()
                .text("Details successfully updated")
                .position(Pos.TOP_RIGHT)
                .hideCloseButton()
                .hideAfter(Duration.seconds(3));
        update.darkStyle();
        update.showInformation();
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection connection = databaseConnection.getConnection();
        String updateQuery = "UPDATE dairy_farm.milking_records set morningQty = ?, afternoonQty = ?, eveningQty = ?, milkingDate = ? WHERE cowName = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setDouble(1, Double.parseDouble(morningQuantity.getText()));
            preparedStatement.setDouble(2, Double.parseDouble(afternoonQuantity.getText()));
            preparedStatement.setDouble(3, Double.parseDouble(eveningQuantity.getText()));
            preparedStatement.setDate(4, java.sql.Date.valueOf(milkingDate.getValue()));
            preparedStatement.setString(5, cowNameTextField.getText());
            preparedStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
        milkSearchModelObservableList.clear();
        if(milkRecords.getItems().isEmpty()){
            loadDataButton.setOnAction(actionEvent -> {
                retrieveFromDatabase();
                addLastRow();
            });
        }
    }
    @FXML
    void editButtonPressed() {
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<>() {
            @Override
            public DateCell call(DatePicker datePicker) {
                return new DateCell() {
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(milkingDate.getValue()) || item.isAfter(milkingDate.getValue())) {
                            setDisable(false);
                        }
                    }
                };
            }
        };
        milkingDate.setDayCellFactory(dayCellFactory);
        cowNameTextField.setDisable(false);
        cowIDTextField.setDisable(false);
        morningQuantity.setDisable(false);
        afternoonQuantity.setDisable(false);
        eveningQuantity.setDisable(false);
        cowIDTextField.setStyle("-fx-control-inner-background: #FAF9F9");
        cowNameTextField.setStyle("-fx-control-inner-background: #FAF9F9");
        morningQuantity.setStyle("-fx-control-inner-background: #FAF9F9");
        afternoonQuantity.setStyle("-fx-control-inner-background: #FAF9F9");
        eveningQuantity.setStyle("-fx-control-inner-background: #FAF9F9");
        edit.setDisable(false);
        milkUpdate.setDisable(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        setValueToTextField();
        if(milkSearchModelObservableList.isEmpty()){
            edit.setDisable(true);
            milkUpdate.setDisable(true);
            milkDelete.setDisable(true);
        }
        loadDataButton.setOnAction(actionEvent -> {
            milkSave.setDisable(false);
            addLastRow();
            retrieveFromDatabase();
        });
    }
    private void add_MilkRecord(){
        DatabaseConnection connection = new DatabaseConnection();
        Connection dbConnection = connection.getConnection();
        String id = cowIDTextField.getText();
        String name = cowNameTextField.getText();
        String amLitres = morningQuantity.getText();
        String noonLitres = afternoonQuantity.getText();
        String pmLitres = eveningQuantity.getText();
        LocalDate milkDate = milkingDate.getValue();
        String insertToDB = "INSERT INTO dairy_farm.milking_records(cowID, cowName, morningQty," +
                "afternoonQty, eveningQty, milkingDate)VALUES('";
        String insertDBValues = id+"', '"+name+"', '"+amLitres+"', '"+noonLitres+"', '"+pmLitres+"', '"+milkDate+"')";
        String dataBase = insertToDB + insertDBValues;
        try {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(dataBase);
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addLastRow(){
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connectToDB = dbConnection.getConnection();
        String recordQuery = "SELECT * FROM dairy_farm.milking_records ORDER BY cowID DESC LIMIT 1 OFFSET 0";
        try {
            Statement newStatement = connectToDB.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = newStatement.executeQuery(recordQuery);
            resultSet.first();
            int cowID = resultSet.getInt("cowID");
            String cowName = resultSet.getString("cowName");
            Double morningQty = resultSet.getDouble("morningQty");
            Double afternoonQty = resultSet.getDouble("afternoonQty");
            Double eveningQty = resultSet.getDouble("EveningQty");
            Date milkingDate = resultSet.getDate("milkingDate");
            milkSearchModelObservableList.add(new MilkSearchModel(cowID, cowName, morningQty, afternoonQty, eveningQty, milkingDate));
            search();
            connectToDB.close();
        }catch(SQLException exception){
            exception.printStackTrace();
        }
    }
    private void setValueToTextField(){
        milkRecords.setOnMouseClicked(mouseEvent -> {
            edit.setDisable(false);
            milkDelete.setDisable(false);
            MilkSearchModel milkSearchModel = milkRecords.getItems().get(milkRecords.getSelectionModel().getSelectedIndex());
            milkingDate.setValue(LocalDate.parse(milkSearchModel.getMilkingDate().toString()));
            cowNameTextField.setText(milkSearchModel.getName());
            cowIDTextField.setText(String.valueOf(milkSearchModel.getID()));
            morningQuantity.setText(String.valueOf(milkSearchModel.getMorningLitres()));
            afternoonQuantity.setText(String.valueOf(milkSearchModel.getAfternoonLitres()));
            eveningQuantity.setText(String.valueOf(milkSearchModel.getEveningLitres()));
            grayOutFields();
        });
    }
    private void search(){
        cowIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        cowNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        amLitresColumn.setCellValueFactory(new PropertyValueFactory<>("morningLitres"));
        noonLitresColumn.setCellValueFactory(new PropertyValueFactory<>("afternoonLitres"));
        pmLitresColumn.setCellValueFactory(new PropertyValueFactory<>("eveningLitres"));
        milkingDateColumn.setCellValueFactory(new PropertyValueFactory<>("milkingDate"));
        milkRecords.setItems(milkSearchModelObservableList);
        FilteredList<MilkSearchModel> filteredList = new FilteredList<>(milkSearchModelObservableList, b -> true);
        searchCow.textProperty().addListener((observableValue, oldValue, newValue) -> filteredList.setPredicate(milkSearchModel -> {
            if(newValue.isEmpty()){
                return true;
            }
            String searchResult = newValue.toLowerCase();
            if(milkSearchModel.getName().toLowerCase().contains(searchResult)){
                return true;
            }
            if(milkSearchModel.getMilkingDate().toString().toLowerCase().contains(searchResult)){
                return true;
            }
            int numValue = milkSearchModel.getID();
            return String.valueOf(numValue).toLowerCase().contains(searchResult);
        }));
        SortedList<MilkSearchModel> searchModelData = new SortedList<>(filteredList);
        searchModelData.comparatorProperty().bind(milkRecords.comparatorProperty());
        milkRecords.setItems(searchModelData);
        milkRecords.getSortOrder().add(cowIDColumn);
    }
    private void grayOutFields(){
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<>() {
            @Override
            public DateCell call(DatePicker datePicker) {
                return new DateCell() {
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(milkingDate.getValue()) || item.isAfter(milkingDate.getValue())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb");
                        }
                    }
                };
            }
        };
        milkingDate.setDayCellFactory(dayCellFactory);
        cowNameTextField.setDisable(true);
        cowIDTextField.setDisable(true);
        morningQuantity.setDisable(true);
        afternoonQuantity.setDisable(true);
        eveningQuantity.setDisable(true);
        cowIDTextField.setStyle("-fx-control-inner-background: #E5E3E3");
        cowNameTextField.setStyle("-fx-control-inner-background: #E5E3E3");
        morningQuantity.setStyle("-fx-control-inner-background: #E5E3E3");
        afternoonQuantity.setStyle("-fx-control-inner-background: #E5E3E3");
        eveningQuantity.setStyle("-fx-control-inner-background: #E5E3E3");
        edit.setVisible(true);
        milkUpdate.setDisable(true);
        milkSave.setDisable(true);
    }
    private void retrieveFromDatabase(){
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connectToDB = dbConnection.getConnection();
        String milkRecordsQuery = "SELECT * FROM dairy_farm.milking_records ORDER BY cowID DESC LIMIT 1000 OFFSET 1";
        try {
            Statement statement = connectToDB.createStatement();
            ResultSet queryResult = statement.executeQuery(milkRecordsQuery);
            while (queryResult.next()){
                int ID = queryResult.getInt("cowID");
                String name = queryResult.getString("cowName");
                Double amLitres = queryResult.getDouble("morningQty");
                Double noonLitres = queryResult.getDouble("afternoonQty");
                Double pmLitres = queryResult.getDouble("eveningQty");
                Date milkDate = queryResult.getDate("milkingDate");
                milkSearchModelObservableList.add(new MilkSearchModel(ID, name, amLitres, noonLitres, pmLitres, milkDate));
                PreparedStatement ps = connectToDB.prepareStatement("SELECT COUNT(*) FROM dairy_farm.milking_records WHERE cowID > ?");
                ps.setInt(1, 0);
                ResultSet rs = ps.executeQuery();
                int n = 0;
                if(rs.next()){
                    n = rs.getInt(1);
                }
                if(n>0){
                    if(!(milkRecords.getColumns().isEmpty())){
                        loadDataButton.setOnAction(actionEvent1 -> {
                            Notifications data = Notifications.create()
                                    .text("Data is up to date")
                                    .position(Pos.TOP_RIGHT)
                                    .hideCloseButton()
                                    .hideAfter(Duration.seconds(3));
                            data.darkStyle();
                            data.showInformation();
                        });
                    }
                }
                search();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
