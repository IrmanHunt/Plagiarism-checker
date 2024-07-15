package ru.sut.plagiarismchecker.Models;

import javafx.application.Platform;
import ru.sut.plagiarismchecker.Controllers.MainMenuPageController;
import ru.sut.plagiarismchecker.DatabaseConnection;
import ru.sut.plagiarismchecker.Main;
import ru.sut.plagiarismchecker.PlagiarismChecker;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс предоставляет методы для работы с данными текстов в базе данных.
 * Включает методы для сохранения текстов, извлечения таблиц, вычисления процента оригинальности
 * и другие операции, связанные с базой данных.
 */
public class TextDao {

    /**
     * Сохраняет информацию о тексте в указанную таблицу базы данных.
     * @param text объект текста для сохранения
     * @param table имя таблицы, в которую сохраняется текст
     */
    public static void saveText(Text text, String table) {
        String query = "INSERT INTO " + table + "(text_name, authors, comment, file_name, text, table_name, timestamp, date, originality, signature) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, text.getTextName());
            statement.setString(2, text.getAuthors());
            statement.setString(3, text.getComment());
            statement.setString(4, text.getFileName());
            statement.setString(5, text.getText());
            statement.setString(6, text.getTable());
            statement.setLong(7, text.getTimestamp());
            statement.setObject(8, text.getDate());
            statement.setObject(9, text.getOriginality());
            statement.setObject(10, text.getSignature());
            statement.executeUpdate();
        } catch (SQLException se) {
            if (se.getMessage().contains("UNIQUE constraint failed")) {
                Main.showNotification("Текст с таким именем уже существует!");
            } else {
                se.printStackTrace();
            }
        }
    }

    /**
     * Возвращает список всех таблиц в текущей базе данных.
     * @return список имен таблиц
     */
    public static List<String> getTables() {
        List<String> tableNames = new ArrayList<>();

        try(Connection connection = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableNames;
    }

    /**
     * Возвращает процент оригинальности текста, хранящегося в указанной таблице,
     * сравнивая его с другими текстами по их сигнатурам.
     * @param serializedSignature1 сериализованная сигнатура текста, который нужно сравнить
     * @param table имя таблицы, содержащей тексты для сравнения
     * @return процент оригинальности текста
     */
    public static int getOriginalityPercent(String serializedSignature1, String table) {
        String query = "SELECT * FROM " + table;
        List<int[]> signature1 = PlagiarismChecker.deserializeSignature(serializedSignature1);
        double similarity = 0;

        try(Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                String serializedSignature2 = resultSet.getString("signature");
                List<int[]> signature2 = PlagiarismChecker.deserializeSignature(serializedSignature2);
                similarity += PlagiarismChecker.getSimilarity(signature1, signature2);
                if(similarity > 1) {
                    similarity = 1;
                    break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return PlagiarismChecker.getOriginalityPercent(similarity);
    }

    /**
     * Возвращает список строк, представляющих тексты, считанные из указанной таблицы,
     * которые имеют процент схожести меньше заданного порога.
     * @param serializedSignature1 сериализованная сигнатура текста, который нужно сравнить
     * @param table имя таблицы, содержащей тексты для сравнения
     * @return список строк с информацией о похожих текстах
     */
    public static List<String> getBorrowingsList(String serializedSignature1, String table) {
        String query = "SELECT * FROM " + table;
        List<int[]> signature1 = PlagiarismChecker.deserializeSignature(serializedSignature1);
        List<String> borrowingsList = new ArrayList<>();

        try(Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                String serializedSignature2 = resultSet.getString("signature");
                String textName = resultSet.getString("text_name");
                String authors = resultSet.getString("authors");
                List<int[]> signature2 = PlagiarismChecker.deserializeSignature(serializedSignature2);
                int originality = PlagiarismChecker.getOriginalityPercent(signature1, signature2);
                if(originality <= 100 - MainMenuPageController.similarity) {
                    borrowingsList.add(new String("Название: " + textName + "\nАвтор: " + authors + "\nПроцент схожести: " + (100 - originality) + "%\n\n"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return borrowingsList;
    }

    /**
     * Возвращает список объектов `Text`, представляющих тексты, считанные из указанной таблицы.
     * @param table имя таблицы, из которой нужно получить тексты
     * @return список объектов `Text`
     */
    public static List<Text> getTextsOfSelectedTable(String table) {
        String query = "SELECT * FROM " + table + " ORDER BY text_name ASC";
        List<Text> tableList = new ArrayList<>();

        try(Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                String textName = resultSet.getString("text_name");
                String authors = resultSet.getString("authors");
                String text = resultSet.getString("text");
                String tableName = resultSet.getString("table_name");
                Text textObj = new Text();
                textObj.setTextName(textName);
                textObj.setAuthors(authors);
                textObj.setText(text);
                textObj.setTable(tableName);
                tableList.add(textObj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableList;
    }

    /**
     * Добавляет новую таблицу с указанным именем в базу данных.
     * @param table имя новой таблицы
     * @return `true`, если таблица успешно добавлена; `false`, если произошла ошибка
     */
    public static boolean addTable(String table) {
        String query = "CREATE TABLE " + table + """
                (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    text_name TEXT UNIQUE NOT NULL,
                    authors TEXT NOT NULL,
                    comment TEXT,
                    file_name TEXT,
                    text TEXT,
                    table_name TEXT,
                    timestamp INTEGER,
                    date TEXT,
                    originality INTEGER,
                    signature TEXT
                );
                """;

        try(Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
        } catch (SQLException se) {
            if (se.getMessage().contains("already exists")) {
                Main.showNotification("Такая таблица уже есть!");
            } else if (se.getMessage().contains("syntax error")) {
                Main.showNotification("Некорректное название таблицы!");
            } else {
                se.printStackTrace();
            }
            return false;
        }

        return true;
    }

    /**
     * Удаляет указанную таблицу из базы данных.
     * @param table имя таблицы для удаления
     * @return `true`, если таблица успешно удалена; `false`, если произошла ошибка
     */
    public static boolean deleteTable(String table) {
        String query = "DROP TABLE " + table;

        try(Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
        } catch (SQLException se) {
            if (se.getMessage().contains("no such table")) {
                Main.showNotification("Такой таблицы не существует!");
            } else {
                se.printStackTrace();
            }
            return false;
        }

        return true;
    }

    /**
     * Удаляет текст с указанным именем из указанной таблицы.
     * @param table имя таблицы, из которой нужно удалить текст
     * @param textName имя текста для удаления
     * @return `true`, если текст успешно удален; `false`, если произошла ошибка или текст не найден
     */
    public static boolean deleteText(String table, String textName) {
        String query = "DELETE FROM " + table + " WHERE text_name = ?";

        try(Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, textName);
            int rowsDeleted = statement.executeUpdate();
            if(rowsDeleted == 0)
                return false;
        } catch (SQLException se) {
            se.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Обновляет столбец 'signature' для всех записей во всех таблицах, если он в настоящее время пустой или null.
     * <p>
     * Метод получает все таблицы из базы данных, и для каждой таблицы выбирает столбцы id, text,
     * и signature. Если значение signature пустое или null, метод вычисляет новую сигнатуру с
     * использованием алгоритма MinHash и обновляет запись с этой новой сигнатурой.
     * </p>
     */
    public static void setAllSignatures() {
        List<String> tables = TextDao.getTables();

        try(Connection connection = DatabaseConnection.getConnection()) {
            for(String table: tables) {
                PreparedStatement statement = connection.prepareStatement("SELECT id, text, signature FROM " +  table);
                ResultSet resultSet = statement.executeQuery();
                while(resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String text = resultSet.getString("text");
                    String serializedSignature = resultSet.getString("signature");
                    if(serializedSignature == null || serializedSignature.isEmpty()) {
                        List<int[]> signature = PlagiarismChecker.getMinHashSignature(text);
                        serializedSignature = PlagiarismChecker.serializeSignature(signature);
                        statement = connection.prepareStatement("UPDATE " + table + " SET signature = ? WHERE id = ?");
                        statement.setString(1, serializedSignature);
                        statement.setInt(2, id);
                        statement.executeUpdate();
                    }
                }
            }
        } catch (SQLException se) {
            Main.showNotification("Ошибка при вычислении сигнатуры! Проверьте правильность полей базы данных!");
            se.printStackTrace();
            Platform.exit();
        }
    }
}
