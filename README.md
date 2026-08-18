# Time4Meds 💊

**A Mobile Application for Caregivers in Monitoring Elderly Medication**

Final Year Project — Bachelor of Information Technology (Hons.), Universiti Teknologi MARA (UiTM)

By **Ainaa Aqilah binti Hassan Nuddin** (2026)

## 📋 About

Medication management among the elderly is a major challenge for caregivers in Malaysia, due to complex medication routines and the elderly's tendency to forget doses. Existing digital tools are often too complex for this use case. **Time4Meds** was built to solve this: a mobile app that helps caregivers set up, track, and get alerted about elderly medication schedules with a focus on simplicity and usability.

The project was developed using the Mobile Application Development Lifecycle (MADLC), covering five phases: Identification, Design, Development, Prototyping, and Testing. Usability was validated using the System Usability Scale (SUS), which found the app usable and well-accepted by users.

## ✨ Features

- **User Authentication** — secure caregiver login and registration
- **Profile Selection** — add and manage multiple elderly profiles
- **Medication Management** — set up medication details for each profile
- **Reminder Management** — schedule medication reminders
- **Daily Checklist** — track and review medication adherence history
- **Alert Notification** — background scheduling via WorkManager ensures reminders are pushed to caregivers at the scheduled time, even when the app isn't running
- **SOS Alert** — long-press emergency button that opens the device dialer (pre-filled with 999) and launches WhatsApp with a pre-filled alert message to the saved emergency contact

## 🛠️ Tech Stack

- **Language:** Java
- **IDE:** Android Studio (Gradle build system)
- **Backend:** Firebase Authentication, Firebase Realtime Database, Firebase Storage
- **Notifications:** Android WorkManager (NotificationWorker) + NotificationManager for scheduled reminder delivery
- **Emergency Alerts:** Native device dialer + WhatsApp intent integration

## 🎥 Demo Video

[Watch the demo video](https://youtu.be/rI9qUDwfix8)

## 📄 Full Report

[Read the full FYP report](docs/Time4Meds-FYP-Project-Report.pdf)

## ⚠️ Limitations & Future Work

Currently limited to Android devices, English language only, no offline functionality, and supports only a single emergency contact per profile. Future improvements include multi-platform support, multiple language options, collaborative caregiving features, and custom medication images.

## 🚀 Getting Started

1. Clone this repository
   ```
   git clone https://github.com/ainaaqilah/Time4Meds-FYP-Project.git
   ```
2. Open the project in Android Studio
3. Add your own `google-services.json` (Firebase config) and a `functions/.env` file with your Twilio credentials — these are excluded from the repo for security
4. Build and run on an emulator or physical device

## 👤 Author

**Ainaa Aqilah binti Hassan Nuddin**

Bachelor of Information Technology (Hons.), Universiti Teknologi MARA

[GitHub](https://github.com/ainaaqilah)

---
*This project was developed as a Final Year Project (FYP).*
