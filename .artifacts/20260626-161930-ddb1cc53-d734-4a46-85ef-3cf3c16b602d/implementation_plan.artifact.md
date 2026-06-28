# Deep Restoration to Turn 9 (Stable Mobile)

Reverting the application from the experimental Next.js/LOCALU web architecture back to the premium mobile-first Turn 9 version, while keeping the new logo and real Firebase Auth.

## User Review Required
- None at this stage. The goal is strictly restoration and fixing compilation errors.

## Proposed Changes

### Data & Models
#### [Models.kt](file:///C:/Users/PAVAN KALYAN/OneDrive/Desktop/ManaDelivery%20_Bellempally/app/src/main/java/com/example/manadeliverybellempally/data/model/Models.kt)
- (Already restored) Mobile-optimized models with compatibility stubs.

#### [FirestoreRepository.kt](file:///C:/Users/PAVAN KALYAN/OneDrive/Desktop/ManaDelivery%20_Bellempally/app/src/main/java/com/example/manadeliverybellempally/data/repository/FirestoreRepository.kt)
- Add missing stubs for `getCouponsFlow`, `updateRiderKYC`, etc., to satisfy existing UI references.

### UI Restoration
#### [RiderDashboard.kt](file:///C:/Users/PAVAN KALYAN/OneDrive/Desktop/ManaDelivery%20_Bellempally/app/src/main/java/com/example/manadeliverybellempally/ui/rider/RiderDashboard.kt)
- Revert to Turn 9 mobile-first dashboard.

#### [VendorDashboard.kt](file:///C:/Users/PAVAN KALYAN/OneDrive/Desktop/ManaDelivery%20_Bellempally/app/src/main/java/com/example/manadeliverybellempally/ui/vendor/VendorDashboard.kt)
- Revert to Turn 9 mobile-first dashboard.

#### [AdminDashboard.kt](file:///C:/Users/PAVAN KALYAN/OneDrive/Desktop/ManaDelivery%20_Bellempally/app/src/main/java/com/example/manadeliverybellempally/ui/admin/AdminDashboard.kt)
- Revert to Turn 9 simplified mobile admin overview.

### Data Seeding
#### [SeedData.kt](file:///C:/Users/PAVAN KALYAN/OneDrive/Desktop/ManaDelivery%20_Bellempally/app/src/main/java/com/example/manadeliverybellempally/data/SeedData.kt)
- Update constructors to match restored mobile models.

#### [SampleData.kt](file:///C:/Users/PAVAN KALYAN/OneDrive/Desktop/ManaDelivery%20_Bellempally/app/src/main/java/com/example/manadeliverybellempally/data/SampleData.kt)
- Update constructors to match restored mobile models.

## Verification Plan
### Automated Tests
- Run `./gradlew.bat assembleDebug` to ensure no compilation errors.

### Manual Verification
- Deploy to device/emulator and verify the Maroon/Gold theme and navigation flow.
