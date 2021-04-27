package com.example.ibook.bean;

public class History {

    private String book_name;
    private String author;
    private String book_id;
    private String press;
    private int status;
    private String order_time;
    private String borrow_time;
    private String giveback_time;

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOrder_time() {
        return order_time;
    }

    public void setOrder_time(String order_time) {
        this.order_time = order_time;
    }

    public String getBorrow_time() {
        return borrow_time;
    }

    public void setBorrow_time(String borrow_time) {
        this.borrow_time = borrow_time;
    }

    public String getGiveback_time() {
        return giveback_time;
    }

    public void setGiveback_time(String giveback_time) {
        this.giveback_time = giveback_time;
    }
}
