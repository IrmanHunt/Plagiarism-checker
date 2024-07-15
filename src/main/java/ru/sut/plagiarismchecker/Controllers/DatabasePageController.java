package ru.sut.plagiarismchecker.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.sut.plagiarismchecker.DatabaseConnection;
import ru.sut.plagiarismchecker.Main;
import ru.sut.plagiarismchecker.Models.Text;
import ru.sut.plagiarismchecker.Models.TextDao;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Контроллер для управления интерфейсом страницы базы данных.
 */
public class DatabasePageController implements Initializable {

    List<Text> textsList;

    @FXML
    private Label databaseLabel;

    @FXML
    private TextArea textArea;

    @FXML
    private ListView<String> tablesListView;

    @FXML
    private ListView<String> textsListView;

    @FXML
    private TextField tableTextField;

    @FXML
    private TextField textNameTextFieldId;

    /**
     * Метод для добавления новой таблицы в базу данных.
     * Извлекает имя таблицы из текстового поля и вызывает метод `TextDao.addTable`.
     * Обновляет список таблиц в представлении.
     */
    @FXML
    void addTableButton() {
        String table = tableTextField.getText();
        if(TextDao.addTable(table)) {
            List<String> tables = TextDao.getTables();
            tablesListView.getItems().clear();
            tablesListView.getItems().addAll(tables);
        }
    }

    /**
     * Метод для удаления выбранной таблицы из базы данных.
     * Извлекает имя таблицы из текстового поля и вызывает метод `TextDao.deleteTable`.
     * Обновляет список таблиц в представлении.
     * Выводит уведомление через `Main.showNotification` в случае ошибки.
     */
    @FXML
    void deleteTableButton() {

        if (tableTextField.getText().isEmpty()) {
            Main.showNotification("Выберите таблицу!");
            return;
        }

        String table = tableTextField.getText();
        if(TextDao.deleteTable(table)) {
            List<String> tables = TextDao.getTables();
            tablesListView.getItems().clear();
            tablesListView.getItems().addAll(tables);
        }
    }

    /**
     * Метод для удаления выбранного текста из выбранной таблицы.
     * Извлекает имя текста из текстового поля и вызывает метод `TextDao.deleteText`.
     * Обновляет список текстов в представлении.
     * Выводит уведомление через `Main.showNotification` в случае ошибки.
     */
    @FXML
    void deleteTextButton() {

        if(textNameTextFieldId.getText().isEmpty()) {
            Main.showNotification("Выберите текст!");
            return;
        }

        String textName = textNameTextFieldId.getText().trim();
        String table = "";

        for(Text text: textsList) {
            if(textName.equals(text.getTextName())) {
                table = text.getTable();
                break;
            }
        }

        if(TextDao.deleteText(table, textName)) {
            textsList = TextDao.getTextsOfSelectedTable(table);
            List<String> list = new ArrayList<>();
            textsList.forEach(t -> list.add("Название: " + t.getTextName() + "\nАвторы: " + t.getAuthors()));
            textsListView.getItems().clear();
            textsListView.getItems().addAll(list);
            textNameTextFieldId.clear();
        }

    }

    /**
     * Инициализация контроллера при загрузке FXML файла.
     * Устанавливает начальные значения для элементов интерфейса,
     * загружает список таблиц и устанавливает обработчики событий для выбора таблиц и текстов.
     * Выводит имя текущей базы данных в метке `databaseLabel`.
     * @param url не используется
     * @param resourceBundle не используется
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<String> tables = TextDao.getTables();
        tablesListView.getItems().addAll(tables);
        databaseLabel.setText("База данных: " + DatabaseConnection.databaseName);

        tablesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                textsList = TextDao.getTextsOfSelectedTable(newValue);
                List<String> list = new ArrayList<>();
                textsList.forEach(t -> list.add("Название: " + t.getTextName() + "\nАвторы: " + t.getAuthors()));
                textsListView.getItems().clear();
                textsListView.getItems().addAll(list);
                tableTextField.setText(newValue);
            }
        });

        textsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Scanner scanner = new Scanner(newValue);
                String[] s = scanner.nextLine().split(":");
                String selectedTextName = s[1].trim();
                for(Text text: textsList) {
                    if(selectedTextName.equals(text.getTextName())) {
                        textArea.setText(text.getText());
                        break;
                    }
                }
                textNameTextFieldId.setText(selectedTextName);
            }
        });
    }
}
