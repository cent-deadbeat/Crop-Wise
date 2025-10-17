package com.example.soilmate;

public class FAQModel {
    private String question;
    private String answer;
    private String category; // New: "Getting Started", "Troubleshooting", etc.
    private boolean expanded;
    private boolean isHeader; // New: For section headers

    // Constructor for regular FAQ items
    public FAQModel(String question, String answer, String category) {
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.expanded = false;
        this.isHeader = false;
    }

    // Constructor for section headers (no question/answer needed)
    public FAQModel(String category) {
        this.category = category;
        this.isHeader = true;
        this.question = "";
        this.answer = "";
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getCategory() {
        return category;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isHeader() {
        return isHeader;
    }

    // Setters
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}