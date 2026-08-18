package com.example.time4meds;

// Elderly: Data model class representing an elderly person's profile

import java.util.List;

public class Elderly {

    // data model
    private String id;
    private String name;
    private String age;
    private String medicalInfo;
    private String gender;
    private List<String> emergencyContact;
    private String photoUrl;

    public Elderly() { }

    // normal constructor
    public Elderly(String id, String name, String age, String medicalInfo, String gender, List<String> emergencyContact) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.medicalInfo = medicalInfo;
        this.gender = gender;
        this.emergencyContact = emergencyContact;
    }

    // mutator (setter) method
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // accessor (getter) method
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getMedicalInfo() { return medicalInfo; }
    public void setMedicalInfo(String medicalInfo) { this.medicalInfo = medicalInfo; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public List<String> getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(List<String> emergencyContact) { this.emergencyContact = emergencyContact; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
