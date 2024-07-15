module ru.sut.plagiarismchecker {
    requires org.apache.pdfbox;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ru.sut.plagiarismchecker to javafx.fxml;
    exports ru.sut.plagiarismchecker;
    exports ru.sut.plagiarismchecker.Controllers;
    opens ru.sut.plagiarismchecker.Controllers to javafx.fxml;
    exports ru.sut.plagiarismchecker.Models;
    opens ru.sut.plagiarismchecker.Models to javafx.fxml;
}