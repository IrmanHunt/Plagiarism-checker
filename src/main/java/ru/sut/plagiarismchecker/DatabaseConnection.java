package ru.sut.plagiarismchecker;

import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Класс для управления подключением к базе данных SQLite.
 */
public class DatabaseConnection {

    /** Имя базы данных, полученное из конфигурационного файла */
    public static String databaseName;

    /**
     * Проверяет соединение с базой данных, используя конфигурационный файл.
     * Если база данных не выбрана или отсутствует, отображает уведомление и завершает приложение.
     */
    public static void testConnection() {
        Properties config = new Properties();

        String dirPath = Paths.get("").toAbsolutePath().toString();
        Path dir = Paths.get(dirPath);

        try (InputStream input = Files.newInputStream(dir.resolve("config.properties"))) {
            if (input == null) {
                return;
            }
            config.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        databaseName = config.getProperty("database.name");

        if (databaseName == null || databaseName.isEmpty()) {
            Main.showNotification("Не выбрана база данных!");
            Platform.exit();
        }
    }

    /**
     * Возвращает соединение с базой данных SQLite на основе заданного имени базы данных.
     * @return объект Connection для выполнения SQL запросов
     */
    public static Connection getConnection() {
        String url = "jdbc:sqlite:" + databaseName;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }
}
