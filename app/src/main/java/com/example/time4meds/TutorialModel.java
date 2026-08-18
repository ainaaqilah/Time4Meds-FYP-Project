package com.example.time4meds;

// TutorialModel: Model class for a single tutorial step

public class TutorialModel {
    private String title;
    private String instruction;
    private String tip;
    private int imageResId;

    public TutorialModel(String title, String instruction, String tip, int imageResId) {
        this.title = title;
        this.instruction = instruction;
        this.tip = tip;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getInstruction() { return instruction; }
    public String getTip() { return tip; }
    public int getImageResId() { return imageResId; }
}