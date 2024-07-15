package ru.sut.plagiarismchecker.Controllers;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import ru.sut.plagiarismchecker.Main;
import ru.sut.plagiarismchecker.Models.Text;
import ru.sut.plagiarismchecker.Models.TextDao;
import ru.sut.plagiarismchecker.PlagiarismChecker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Контроллер для управления главным меню приложения.
 */
public class MainMenuPageController implements Initializable {

    private String fileName;
    public static int similarity;

    @FXML
    private Label symbolsLabel;

    @FXML
    private Label percentLabel;

    @FXML
    private Slider percentSlider;

    @FXML
    private TextField authorsField;

    @FXML
    private ComboBox<String> tablesComboBoxId;

    @FXML
    private TextArea commentField;

    @FXML
    private TextArea textArea;

    @FXML
    private TextField textNameField;

    @FXML
    private Button loadTextButtonId;

    @FXML
    private Label originalityPercentLabel;

    @FXML
    private ListView<String> borrowingsListView;

    /**
     * Обработчик события нажатия кнопки "Проверить".
     * Проверяет размер текста, выбрана ли таблица, вычисляет оригинальность текста
     * и отображает список заимствований.
     */
    @FXML
    void checkButton() {
        if (textArea.getText().length() < 505) {
            Main.showNotification("Слишком маленький размер текста!");
            return;
        }

        if (tablesComboBoxId.getValue() == null) {
            Main.showNotification("Выберите таблицу!");
            return;
        }

        String table = tablesComboBoxId.getValue();
        List<int[]> signature = PlagiarismChecker.getMinHashSignature(textArea.getText());
        String serializedSignature = PlagiarismChecker.serializeSignature(signature);
        int originality = TextDao.getOriginalityPercent(serializedSignature, table);
        originalityPercentLabel.setText(originality + "%");
        List<String> borrowingsList = TextDao.getBorrowingsList(serializedSignature, table);
        borrowingsList.sort(new SimilarityComparator());
        borrowingsListView.getItems().clear();
        borrowingsListView.getItems().addAll(borrowingsList);

    }

    /**
     * Внутренний класс для сравнения строк списка заимствований по проценту схожести.
     */
    private static class SimilarityComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            int similarity1 = getSimilarityPercentage(o1);
            int similarity2 = getSimilarityPercentage(o2);
            return Integer.compare(similarity2, similarity1);
        }

        private int getSimilarityPercentage(String line) {
            Pattern pattern = Pattern.compile("Процент схожести: (\\d+)%");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return 0;
        }
    }

    /**
     * Метод для загрузки текста из файла .txt или .pdf.
     * Отображает текст в текстовой области `textArea` и сохраняет имя файла.
     */
    @FXML
    void loadTextButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите .txt или .pdf файл");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.pdf"));
        Stage stage = (Stage) loadTextButtonId.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            try {
                String text;
                if (filePath.toLowerCase().endsWith(".pdf")) {
                    text = readPDF(selectedFile);
                } else {
                    text = Files.readString(Paths.get(filePath));
                }
                if (text.length() < 500) {
                    Main.showNotification("Слишком маленький размер текста!");
                    return;
                }
                fileName = selectedFile.getName();
                loadTextButtonId.setText("Загружен: " + fileName);
                textArea.setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Чтение текста из PDF-файла.
     * @param file PDF-файл
     * @return текст из PDF
     * @throws IOException если возникает ошибка при чтении файла
     */
    private String readPDF(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        return text;
    }

    /**
     * Метод для сохранения текста в базу данных.
     * Создает объект `Text`, заполняет его данными и вызывает метод `TextDao.saveText`.
     * После сохранения очищает поля ввода с помощью метода `clean`.
     */
    @FXML
    void saveTextButton() {
        String[] fields = {textNameField.getText(), textArea.getText()};
        String[] messages = {"Введите название текста!", "Введите текст!"};

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isEmpty()) {
                Main.showNotification(messages[i]);
                return;
            }
        }

        if (textArea.getText().length() < 510) {
            Main.showNotification("Слишком маленький размер текста!");
            textArea.clear();
            return;
        }

        if (tablesComboBoxId.getValue() == null) {
            Main.showNotification("Выберите таблицу!");
            return;
        }

        String table = tablesComboBoxId.getValue();
        List<int[]> signature = PlagiarismChecker.getMinHashSignature(textArea.getText());
        String serializedSignature = PlagiarismChecker.serializeSignature(signature);

        Text text = new Text();
        text.setTextName(textNameField.getText());
        text.setAuthors(authorsField.getText());
        text.setComment(commentField.getText());
        text.setFileName(fileName);
        text.setText(textArea.getText());
        text.setTable(tablesComboBoxId.getValue());
        text.setTimestamp(Instant.now().getEpochSecond());
        text.setDate(LocalDate.now());
        text.setOriginality(originalityPercentLabel.getText());
        text.setSignature(serializedSignature);

        TextDao.saveText(text, table);

        cleanButton();
    }

    /**
     * Метод для отображения страницы базы данных.
     * Вызывает метод `Main.showDatabase()` для открытия окна базы данных,
     * обновляет список таблиц в выпадающем списке `tablesComboBox`.
     * Если была выбрана таблица, устанавливает ее в качестве выбранной после обновления.
     * @throws IOException если возникает ошибка при отображении страницы базы данных
     */
    @FXML
    void showDatabase() throws IOException {
        String chosenTable = tablesComboBoxId.getValue();
        Main.showDatabase();
        List<String> tables = TextDao.getTables();
        tablesComboBoxId.getItems().clear();
        tablesComboBoxId.getItems().addAll(tables);
        if(chosenTable != null) {
            for(String table: tables) {
                if(chosenTable.equals(table)) {
                    tablesComboBoxId.setValue(table);
                    return;
                }
            }
        }
    }

    /**
     * Обработчик события загрузки текста через меню.
     * Вызывает метод `loadText` для загрузки текста из файла.
     */
    @FXML
    void loadMenuItem() {
        loadTextButton();
    }

    /**
     * Метод для закрытия приложения.
     * Вызывает `Platform.exit()` для закрытия JavaFX приложения.
     */
    @FXML
    void closeApp() {
        Platform.exit();
    }

    /**
     * Метод для очистки всех полей ввода и сброса заголовка кнопки "Загрузить".
     */
    @FXML
    void cleanButton() {
        textArea.clear();
        textNameField.clear();
        authorsField.clear();
        commentField.clear();
        originalityPercentLabel.setText("");
        borrowingsListView.getItems().clear();
        loadTextButtonId.setText("Загрузить (.txt, .pdf)");
    }

    /**
     * Инициализация контроллера при загрузке FXML файла.
     * Устанавливает слушатели событий для ползунка `percentSlider` и поля `textArea`.
     * Загружает список таблиц в выпадающий список `tablesComboBox`.
     * Устанавливает контекстное меню для списка заимствований `borrowingsListView`.
     * @param url не используется
     * @param resourceBundle не используется
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        percentSlider.valueProperty().addListener((observableValue, number, t1) -> {
            percentSlider.setValue(Math.round(t1.doubleValue() / 5) * 5);
            similarity = (int) percentSlider.getValue();
            percentLabel.setText("Процент схожести: " + similarity + "% и выше");
        });
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            symbolsLabel.setText(newValue.length() + " символов");
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Копировать");
        contextMenu.getItems().add(copyItem);

        copyItem.setOnAction(event -> {
            String selectedItem = borrowingsListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedItem);
                clipboard.setContent(content);
            }
        });

        borrowingsListView.setContextMenu(contextMenu);
        List<String> tables = TextDao.getTables();
        tablesComboBoxId.getItems().clear();
        tablesComboBoxId.getItems().addAll(tables);
    }
}
