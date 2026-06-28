# Mana Delivery Bellempally — Production Platform

A complete advanced delivery platform for Bellempally with 4 specialized roles: **Customer**, **Vendor**, **Rider**, and **Admin**. Built with Kotlin, Jetpack Compose, and Firebase.

## 🚀 Key Features

### 👥 4-Role Ecosystem
- **Customer App**: Real-time browsing, advanced cart (single-vendor), checkout with landmark support, and live order tracking.
- **Vendor Control Center**: Comprehensive store management (Open/Busy status), product CRUD with variants, live order fulfillment, and finance hub.
- **Rider Portal**: Duty toggle (Online/Offline), real-time delivery requests, one-tap status updates, and COD cash tracking.
- **Admin Command Center**: Platform-wide dashboard, user/vendor approvals, manual order re-assignment, coupon manager, and global settings.

### 💎 Branding & Identity
- **Maroon & Gold Theme**: Premium visual identity inspired by the official 3D logo.
- **Just Call. We Deliver.**: Integrated brand tagline across all partner dashboards.
- **Localized Experience**: Optimized for Bellempally with landmark-based addresses and Telugu/Hindi text support structures.

### 🛡️ Production Grade Backend
- **Atomic Transactions**: Uses Firestore transactions for rider assignments to prevent double-booking.
- **Image Management**: Firebase Storage integration for product photos and KYC documents.
- **Cloud Messaging (FCM)**: Real-time loud alerts for new orders and status updates.
- **Security Rules**: Pre-configured Firestore rules for role-based data protection.

## 🛠️ Setup & Deployment

### 1. Firebase Configuration
1. Create a project in [Firebase Console](https://console.firebase.google.com/).
2. Enable **Phone Authentication** (under Build > Authentication).
3. Enable **Cloud Firestore** and **Firebase Storage**.
4. Register your app (package name: `com.example.manadeliverybellempally`).
5. Add your **SHA-1** and **SHA-256** fingerprints from Android Studio to the project settings.
6. Download `google-services.json` and place it in the `app/` directory.

### 2. Database Setup
- Upload the rules provided in `firestore.rules`.
- Create a document in `settings/global` using the `AdminSettings` model structure to initialize delivery fees and commission.
- (Optional) Use `SeedData.kt` to populate initial categories.

### 3. Build & Run
- Use the temporary OTP **`123456`** for testing (Current test mode enabled).
- Admin number for full access: **`7659989335`**.
- Run: `./gradlew assembleDebug` for testing or `./gradlew assembleRelease` for production.

## 📦 Tech Stack
- **UI**: Jetpack Compose (Material3)
- **Architecture**: MVVM + Clean Architecture
- **Navigation**: Navigation3
- **Local Cache**: Jetpack DataStore
- **Backend**: Firebase Auth, Firestore, Storage, FCM
- **Build**: Gradle (Kotlin DSL)

---
**Mana Delivery Bellempally** — *Just Call. We Deliver.*
