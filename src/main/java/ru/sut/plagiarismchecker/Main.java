package ru.sut.plagiarismchecker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.sut.plagiarismchecker.Models.TextDao;

import java.io.IOException;
import java.io.InputStream;

/**
 * Главный класс приложения, который запускает графический интерфейс и управляет основными окнами и уведомлениями.
 */
public class Main extends Application {

    /**
     * Точка входа в приложение.
     * @param primaryStage основная сцена приложения
     * @throws IOException если возникают проблемы с загрузкой ресурсов FXML
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        DatabaseConnection.testConnection();
        TextDao.setAllSignatures();
        showMainWindow();
    }

    /**
     * Отображает главное окно приложения, загружая FXML и настраивая его вид.
     * @throws IOException если возникают проблемы с загрузкой ресурсов FXML
     */
    public void showMainWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Fxml/main-menu-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage primaryStage =  new Stage();
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Антиплагиат");
        InputStream iconStream = getClass().getResourceAsStream("Images/appIcon.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);
        primaryStage.show();
    }

    /**
     * Отображает окно базы данных в модальном режиме.
     * @throws IOException если возникают проблемы с загрузкой ресурсов FXML
     */
    public static void showDatabase() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Fxml/database-page.fxml"));
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("База данных");
        stage.initModality(Modality.APPLICATION_MODAL);
        InputStream iconStream = Main.class.getResourceAsStream("Images/appIcon.png");
        Image image = new Image(iconStream);
        stage.getIcons().add(image);
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.showAndWait();
    }

    /**
     * Отображает информационное уведомление.
     * @param text текст уведомления
     */
    public static void showNotification(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(text);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        InputStream iconStream = Main.class.getResourceAsStream("Images/appIcon.png");
        Image icon = new Image(iconStream);
        stage.getIcons().add(icon);
        alert.showAndWait();
    }

    /**
     * Точка входа в приложение.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch();
    }
}