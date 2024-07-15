package ru.sut.plagiarismchecker.Models;

import java.time.LocalDate;

/**
 * Класс представляет объект текста с атрибутами, характеризующими его данные.
 */
public class Text {

    private int id;
    private String textName;
    private String authors;
    private String comment;
    private String text;
    private String table;
    private String fileName;
    private long timestamp;
    private LocalDate date;
    private String originality;
    private String signature;

    public void setId(int id) {
        this.id = id;
    }

    public void setTextName(String textName) {
        this.textName = textName;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setOriginality(String originality) {
        this.originality = originality;
    }

    public int getId() {
        return id;
    }

    public String getTextName() {
        return textName;
    }

    public String getAuthors() {
        return authors;
    }

    public String getComment() {
        return comment;
    }

    public String getFileName() {
        return fileName;
    }

    public String getText() {
        return text;
    }

    public String getTable() {
        return table;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getOriginality() {
        return originality;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

}
