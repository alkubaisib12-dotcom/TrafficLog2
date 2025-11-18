# Traffic Log - Android App Project Overview

## ✅ PROJECT STATUS: COMPLETE & READY FOR SUBMISSION

This document provides a comprehensive overview of the Traffic Log Android application built for the CI560 Mobile Application Development module at the University of Brighton.

---

## 📱 APP SUMMARY

**Traffic Log** is a local-only Android vehicle reminder app that helps UK drivers track MOT, tax, insurance, and maintenance dates for their vehicles.

### Key Features:
- ✅ Local user authentication (signup/login)
- ✅ Multi-vehicle management per user
- ✅ Dashboard with status filtering (All/Due Soon/Overdue)
- ✅ Complete vehicle information storage
- ✅ Maintenance log tracking
- ✅ Date-based reminders with color coding
- ✅ Image gallery integration for vehicle photos
- ✅ All data persists locally using Room database

---

## 🏗️ ARCHITECTURE & TECH STACK

### Technology Stack:
- **IDE**: Android Studio
- **Language**: Java (app code), Kotlin DSL (Gradle)
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36
- **Database**: Room (SQLite wrapper)
- **UI**: XML layouts with Material Components
- **Architecture**: Simple Activity-based (no MVVM/Hilt for simplicity)

### Key Libraries:
```gradle
// Material Components for modern UI
implementation(libs.material)

// RecyclerView + CardView for Dashboard list
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")

// Room (local database)
implementation("androidx.room:room-runtime:2.6.1")
annotationProcessor("androidx.room:room-compiler:2.6.1")
```

### Project Structure:
```
com.example.trafficlog/
├── model/              # Room entities (User, Vehicle, MaintenanceEntry)
├── data/               # Room DAOs and AppDatabase
├── ui/                 # All Activity classes and VehicleAdapter
└── util/               # SessionManager for user session persistence
```

---

## 📊 DATABASE SCHEMA (Room)

### 1. User Entity
```java
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;  // Unique
    public String password;   // Plain text (limitation acknowledged)
}
```

### 2. Vehicle Entity
```java
@Entity(tableName = "vehicles")
public class Vehicle {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;              // Foreign key to User
    public String type;             // "Car", "Motorcycle", "Other"
    public String imageUri;         // Gallery image URI
    public String licensePlate;

    // Registration
    public String registrationDate;
    public String registrationExpiry;

    // Insurance
    public String insuranceCompany;
    public String insuranceExpiry;

    // MOT
    public String motLastDate;
    public String motNextDate;

    // Tax
    public String taxLastDate;
    public String taxNextDate;
}
```
**Date Format**: All dates stored as String in "yyyy-MM-dd" format (UK locale)

### 3. MaintenanceEntry Entity
```java
@Entity(tableName = "maintenance")
public class MaintenanceEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int vehicleId;   // Foreign key to Vehicle

    // Engine Oil
    public String engineOilLastChanged;
    public String engineOilInterval;

    // Tires
    public String tiresLastChanged;
    public String tiresMileage;

    // General Maintenance
    public String maintenanceLastService;

    // Spark Plugs
    public String sparkPlugsLastReplaced;
    public String sparkPlugsInterval;
}
```

---

## 🎨 SCREENS & USER FLOW

### 1. **Welcome Screen** (`WelcomeActivity.java`)
- **Purpose**: Entry point of the app
- **Elements**:
  - App title "Traffic Log"
  - Subtitle: "Personal MOT, tax and insurance reminders"
  - Two buttons: "Sign in" and "Sign up"
- **Navigation**:
  - "Sign in" → `LoginActivity`
  - "Sign up" → `SignupActivity`

---

### 2. **Login Screen** (`LoginActivity.java`)
- **Purpose**: Authenticate existing users
- **Elements**:
  - Username field (TextInputLayout)
  - Password field (TextInputLayout)
  - "Sign in" button
- **Validation**:
  - Non-empty username and password
  - Checks against Room database
  - Shows error if credentials invalid
- **On Success**:
  - Saves user session via `SessionManager`
  - Navigates to `DashboardActivity`

**Key Code Concept**:
```java
// Database operations run on background thread
new Thread(new Runnable() {
    @Override
    public void run() {
        User user = userDao.getUserByUsernameAndPassword(username, password);
        if (user != null) {
            // Update UI on main thread
            runOnUiThread(/* navigate to Dashboard */);
        }
    }
}).start();
```

---

### 3. **Signup Screen** (`SignupActivity.java`)
- **Purpose**: Create new user accounts
- **Elements**:
  - Username field
  - Password field
  - Confirm password field
  - "Sign up" button
- **Validation**:
  - Username min 3 characters
  - Password min 4 characters
  - Passwords must match
  - Username must be unique (checks database)
- **On Success**:
  - Creates user in Room database
  - Auto-login and navigate to Dashboard

---

### 4. **Dashboard** (`DashboardActivity.java`) - MAIN SCREEN
- **Purpose**: Show all vehicles with status indicators
- **Elements**:
  - Three filter buttons:
    - "ALL UP TO DATE" (green)
    - "DUE SOON" (orange)
    - "OVERDUE" (red)
  - RecyclerView list of vehicle cards
  - FloatingActionButton (FAB) "+" to add vehicle

**Vehicle Card Display**:
Each card shows:
- Vehicle name (license plate or type)
- Status text with color:
  - 🟢 **Green** = "Next due in X days" (>30 days)
  - 🟠 **Orange** = "Due in X days" (≤30 days)
  - 🔴 **Red** = "Overdue by X days" (past due)
- Next due date

**Status Calculation Logic**:
```java
// Finds earliest due date among registration, insurance, MOT, tax
long diffDays = (bestDateMillis - todayMillis) / (1000 * 60 * 60 * 24);

if (diffDays < 0) {
    statusType = OVERDUE;       // Red
} else if (diffDays <= 30) {
    statusType = DUE_SOON;      // Orange
} else {
    statusType = UP_TO_DATE;    // Green
}
```

**Filter Functionality**:
- Filters are implemented by filtering the `allVehicles` list
- Only vehicles matching the status are shown
- "ALL UP TO DATE" shows vehicles with status `UP_TO_DATE` (this is the filter button, not "all vehicles")

**Click Behavior**:
- Clicking a vehicle card → `VehicleDetailsActivity`
- Clicking FAB → `AddVehicleActivity`

---

### 5. **Add Vehicle Screen** (`AddVehicleActivity.java`)
- **Purpose**: Add a new vehicle to the database
- **Elements**:
  - Image picker card (tap to select from gallery)
  - Vehicle type spinner (Car/Motorcycle/Other)
  - All date fields with DatePicker dialogs:
    - License Plate **(required)**
    - Registration Date
    - Registration Expiry
    - Insurance Company **(required)**
    - Insurance Expiry **(required)**
    - MOT Last Date
    - MOT Next Date **(required)**
    - Tax Last Date
    - Tax Next Date **(required)**
  - "SAVE" button

**Image Picker**:
```java
// Modern ActivityResultLauncher approach (replaces deprecated onActivityResult)
private final ActivityResultLauncher<Intent> imagePickerLauncher =
    registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK) {
                selectedImageUri = result.getData().getData();
                ivVehicleImage.setImageURI(selectedImageUri);
            }
        }
    );
```

**Date Pickers**:
- All date fields open a `DatePickerDialog` when clicked
- Dates formatted as "yyyy-MM-dd"
- Fields are non-editable (keyboard disabled) to force DatePicker use

**On Save**:
- Validates required fields
- Saves vehicle to Room database linked to logged-in user
- Returns to Dashboard (refreshes automatically via `onResume()`)

---

### 6. **Vehicle Details Screen** (`VehicleDetailsActivity.java`)
- **Purpose**: Show complete information for a single vehicle
- **Elements**:
  - Vehicle image (if available)
  - Vehicle type
  - License plate
  - All dates (registration, insurance, MOT, tax)
  - Color-coded status summary showing next critical date
  - "Log maintenance dates" button

**Status Summary**:
- Calculates which date (registration/insurance/MOT/tax) is soonest
- Shows color-coded message:
  - Red: "Insurance overdue by 5 days (2025-01-10)"
  - Orange: "MOT due in 12 days (2025-01-30)"
  - Green: "Next due: Tax in 45 days (2025-03-05)"

**Navigation**:
- Back arrow → Dashboard
- "Log maintenance dates" → `MaintenanceActivity`

---

### 7. **Maintenance Log Screen** (`MaintenanceActivity.java`)
- **Purpose**: Track maintenance history for a specific vehicle
- **Elements** (all optional fields):

  **Engine Oil**:
  - Last Changed (date)
  - Interval (text, e.g., "6 months" or "5,000 miles")

  **Tires**:
  - Last Changed (date)
  - Mileage (text)

  **Maintenance**:
  - Last Service (date)

  **Spark Plugs**:
  - Last Replaced (date)
  - Interval (text, e.g., "40,000 km")

  - "SAVE" button

**Behavior**:
- Loads existing maintenance data if available (update mode)
- Creates new entry if none exists (insert mode)
- Date fields use DatePicker
- Interval fields are simple text inputs
- After save → returns to Dashboard

---

## 🔄 DATA FLOW & KEY CONCEPTS

### Session Management
```java
// SessionManager uses SharedPreferences for simple session persistence
public class SessionManager {
    public static void saveLoggedInUser(Context context, int userId, String username) {
        SharedPreferences prefs = context.getSharedPreferences("TrafficLogSession", MODE_PRIVATE);
        prefs.edit()
            .putInt("userId", userId)
            .putString("username", username)
            .apply();
    }

    public static int getLoggedInUserId(Context context) {
        return prefs.getInt("userId", -1);
    }
}
```

### Threading with Room
**Important**: Room database operations **must not** run on the main UI thread.

Pattern used throughout the app:
```java
// Start background thread for database operations
new Thread(new Runnable() {
    @Override
    public void run() {
        // Database operations here
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Vehicle> vehicles = db.vehicleDao().getVehiclesForUser(userId);

        // Update UI on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update UI elements here
                adapter.updateItems(items);
            }
        });
    }
}).start();
```

**Why this approach?**
- Simple and understandable for beginners
- No need for complex libraries (LiveData, Coroutines, RxJava)
- Suitable for university assignment level

### RecyclerView Pattern
```java
// 1. Define adapter
VehicleAdapter adapter = new VehicleAdapter(items, this);

// 2. Set layout manager
recyclerVehicles.setLayoutManager(new LinearLayoutManager(this));

// 3. Attach adapter
recyclerVehicles.setAdapter(adapter);

// 4. Update data
adapter.updateItems(newItems);
```

---

## 🎯 ASSIGNMENT REQUIREMENTS CHECKLIST

### ✅ Core Functionality (MUST HAVE)
- [x] Local signup and login with username + password
- [x] Logged-in user sees only their own vehicles
- [x] Add vehicle flow with all fields
- [x] Vehicles saved in Room and loaded on Dashboard
- [x] Dashboard list with status classification:
  - [x] All up to date (>30 days)
  - [x] Due soon (≤30 days)
  - [x] Overdue (past due)
- [x] Vehicle details screen showing all stored information
- [x] Maintenance log screen saving values to Room
- [x] Data persistence after app close/reopen
- [x] App builds and runs on emulator

### ✅ Quality Features (SHOULD HAVE)
- [x] Filter buttons on dashboard that actually work
- [x] Color coding for statuses (green/orange/red)
- [x] Material components (Toolbar, FAB, CardView, TextInputLayout)
- [x] Image picker for vehicle photos
- [x] DatePicker dialogs for all date fields
- [x] Input validation with error messages
- [x] Back navigation on all screens

### 🔄 Optional Enhancements (COULD HAVE)
- [ ] Push notifications for upcoming due dates
- [ ] Sorting (soonest due vehicle at top)
- [ ] Edit existing vehicle and maintenance entries
- [ ] Export data to CSV

**Note**: The core and quality features are complete. Optional enhancements are not required for a good mark but can be added if you want extra technical challenge.

---

## 🧪 TESTING GUIDE

### Manual Testing Steps:

#### 1. First Launch - Signup Flow
1. Launch app → see Welcome screen
2. Tap "Sign up"
3. Enter username (min 3 chars), password (min 4 chars), confirm password
4. Tap "Sign up" → should navigate to Dashboard
5. Dashboard should be empty with message "No vehicles"

#### 2. Add Vehicle
1. On Dashboard, tap FAB "+"
2. Tap image card → select image from gallery (optional)
3. Select vehicle type from spinner
4. Enter license plate (e.g., "AB12 CDE")
5. Tap date fields to open DatePicker, set dates:
   - Insurance expiry: set 10 days from today (will show as "Due soon")
   - MOT next date: set 2 days ago (will show as "Overdue")
   - Tax next date: set 60 days from today (will show as "Up to date")
6. Enter insurance company name
7. Tap "SAVE"
8. Should return to Dashboard showing the new vehicle

#### 3. Dashboard Filters
1. Note the vehicle's color-coded status
2. Tap "DUE SOON" button → should show only vehicles with orange status
3. Tap "OVERDUE" button → should show only vehicles with red status
4. Tap "ALL UP TO DATE" button → should show only vehicles with green status

#### 4. Vehicle Details
1. Tap on a vehicle card
2. Verify all entered information is displayed
3. Check status summary shows correct color and message
4. Tap "Log maintenance dates"

#### 5. Maintenance Log
1. Enter maintenance information:
   - Engine oil last changed: pick a date
   - Engine oil interval: "6 months"
   - Last service: pick a date
2. Tap "SAVE"
3. Return to vehicle details
4. Tap "Log maintenance dates" again → should show saved data

#### 6. Logout & Login
1. Close app completely (swipe from recent apps)
2. Reopen app → should still be logged in (session persisted)
3. To test login separately, uninstall and reinstall app
4. Sign up again, then close app
5. Reopen → tap "Sign in" instead, enter credentials

#### 7. Multi-User Test
1. Logout (uninstall/reinstall or clear app data)
2. Sign up as User A, add some vehicles
3. Logout (uninstall/reinstall or clear app data)
4. Sign up as User B, add different vehicles
5. Verify User B cannot see User A's vehicles

---

## 🔨 BUILDING THE APK

### Method 1: Android Studio UI
1. Open Android Studio
2. Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
3. Wait for build to complete
4. Click notification "locate" link
5. Find APK at: `app/build/outputs/apk/debug/app-debug.apk`

### Method 2: Gradle Command Line
```bash
# From project root directory
./gradlew assembleDebug

# Find APK at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Installing APK on Physical Device
1. Enable **Developer Options** on Android device
2. Enable **USB Debugging**
3. Connect device via USB
4. Run:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or simply drag and drop the APK file to the device via file transfer.

---

## 📝 REFLECTIVE REPORT GUIDANCE

Your 3,000-word report should cover:

### 1. Requirements Analysis (~400 words)
- Describe the problem: UK drivers need to track MOT, tax, insurance
- User needs: simple, local-only, no account sync needed
- Key features identified
- Wireframe decisions

**Example points**:
- "I chose a dashboard-centric design because users need to see all vehicles at a glance"
- "Color coding (green/orange/red) provides instant visual feedback"
- "FAB placement follows Material Design guidelines for primary actions"

### 2. Research & Design (~500 words)
- Why Room database? (recommended by Android, type-safe, easy to use)
- Why plain Activities instead of Fragments? (simpler for beginners)
- Why no backend? (assignment doesn't require it, local-only is sufficient)
- Date storage choice (String vs Long) - explain your decision
- Material Design components for modern UI
- RecyclerView for efficient list display

**Example reflection**:
- "I researched Room vs raw SQLite and chose Room because it reduces boilerplate code and prevents common SQL errors through compile-time checking"
- "I stored dates as strings in 'yyyy-MM-dd' format for simplicity, but acknowledged this limits advanced date queries"

### 3. Project Management (~300 words)
- How did you plan the work?
- What was your development order? (e.g., database → login → dashboard → details)
- Time management challenges
- How did you track progress?

### 4. Development Process (~1,000 words)
**Key topics to cover**:

**a) Database Layer**:
- Explain Room entities, DAOs, and AppDatabase
- Why singleton pattern for AppDatabase?
- Foreign key relationships (userId in Vehicle, vehicleId in MaintenanceEntry)

**b) Threading**:
- Why database operations run on background threads
- `new Thread()` vs `runOnUiThread()` pattern
- Future improvement: could use AsyncTask or Coroutines

**c) UI Components**:
- RecyclerView and Adapter pattern
- Material TextInputLayout for better UX
- DatePickerDialog for date selection
- ActivityResultLauncher for image picking (modern approach)

**d) Challenges & Solutions**:
- "Initially, the app crashed when clicking 'Save' on AddVehicleActivity. I learned this was because I ran Room operations on the main thread. I fixed it by wrapping database calls in `new Thread()`."
- "Filter buttons initially didn't work. I realized I was modifying the adapter's internal list instead of calling `updateItems()` which triggers UI refresh."

**e) Password Security**:
- **Acknowledge limitation**: "For this assignment, passwords are stored as plain text in the database. In a production app, I would use bcrypt or similar hashing algorithms for security."
- Shows awareness of real-world considerations

### 5. Testing (~400 words)
- Manual testing approach
- Test cases (signup, login, add vehicle, filters, persistence)
- Bugs found and fixed
- Tested on emulator (API 24+) and/or physical device
- Screenshots of app in use

### 6. Summary & Reflection (~400 words)
- What did you learn?
- What would you do differently?
- Future improvements (notifications, edit vehicles, data export)
- How AI assistance helped (explain what prompts you used, how you verified code, what you had to debug yourself)

**AI Declaration Example**:
- "I used Claude AI to generate initial boilerplate code for Room entities and Activities. I asked for explanations of each component and debugged issues independently. I verified all code worked as expected and can explain every line."

---

## 🚨 KNOWN LIMITATIONS & ACKNOWLEDGMENTS

### Security
- **Passwords stored as plain text** in Room database
  - Real apps should use bcrypt, Argon2, or Android Keystore
  - Acknowledged in code comments

### Data Storage
- **No data sync across devices** (local only)
- **No cloud backup** (data lost if app uninstalled)
- Dates stored as Strings (limits complex queries)

### UX Limitations
- **No edit functionality** for existing vehicles (could be added)
- **No delete functionality** (could be added)
- **No sorting options** (could sort by soonest due date)
- **No push notifications** (could use WorkManager for scheduled checks)

### Threading
- Uses basic `new Thread()` instead of modern Coroutines or RxJava
- Acceptable for assignment level, but production apps should use Coroutines

**These limitations are acceptable for a university assignment and demonstrate awareness of real-world considerations.**

---

## 📦 FILES TO SUBMIT

### 1. Android Studio Project Folder
Entire `TrafficLog2` directory including:
- `app/` source code
- `gradle/` build configuration
- `.idea/` IDE settings (optional)
- `build.gradle.kts` files

**Zip the entire project folder.**

### 2. APK File
- `app-debug.apk` (generated from Build → Build APK)
- Tested on Android 7.0+ emulator or device

### 3. Reflective Report (PDF)
- 3,000 words
- Include screenshots of the app
- Declare AI tools used (prompts and how you verified code)

---

## 🎓 KEY LEARNING OUTCOMES

By completing this project, you have demonstrated:

1. **Android Fundamentals**:
   - Activity lifecycle
   - Intents for navigation
   - XML layouts with ConstraintLayout
   - Material Design components

2. **Data Persistence**:
   - Room database setup (entities, DAOs, database class)
   - CRUD operations (Create, Read, Update)
   - Data relationships (foreign keys)

3. **UI/UX Design**:
   - RecyclerView with custom adapter
   - Material components (FAB, CardView, TextInputLayout)
   - Color-coded status indicators
   - DatePicker dialogs
   - Image picker integration

4. **Software Engineering**:
   - Multi-threading (background database operations)
   - Session management
   - Input validation
   - Project structure and organization

5. **Problem Solving**:
   - Date calculations for "due soon" logic
   - Filter implementation
   - Handling optional vs required fields

---

## 🔍 CODE QUALITY NOTES

### Good Practices Used:
- ✅ Singleton pattern for AppDatabase
- ✅ Background threads for database operations
- ✅ Material Design components
- ✅ Input validation with error messages
- ✅ Consistent date format (yyyy-MM-dd)
- ✅ Code organized into logical packages (model, data, ui, util)
- ✅ Comments where necessary

### Areas for Production Improvement:
- Use Coroutines instead of `new Thread()`
- Implement MVVM architecture with ViewModel and LiveData
- Add Hilt/Dagger for dependency injection
- Use bcrypt for password hashing
- Add proper error handling and logging
- Implement edit/delete functionality
- Add unit and UI tests

**These improvements are beyond the scope of this assignment but worth mentioning in your report.**

---

## ✅ FINAL CHECKLIST BEFORE SUBMISSION

- [ ] App builds without errors
- [ ] App runs on Android 7.0+ (API 24+)
- [ ] All 7 screens work correctly
- [ ] Data persists after app restart
- [ ] Filters work on Dashboard
- [ ] Color coding displays correctly
- [ ] Image picker works
- [ ] DatePickers work for all date fields
- [ ] Login/Signup validates correctly
- [ ] APK generated and tested
- [ ] Project folder zipped (with all source files)
- [ ] Report written (3,000 words, includes AI declaration)
- [ ] Screenshots included in report

---

## 📚 ADDITIONAL RESOURCES

### Android Documentation:
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [RecyclerView Guide](https://developer.android.com/develop/ui/views/layout/recyclerview)
- [Material Components](https://material.io/develop/android)
- [Threading in Android](https://developer.android.com/guide/background)

### Understanding Key Concepts:
- **Room Database**: Type-safe wrapper around SQLite
- **RecyclerView**: Efficient list display with ViewHolder pattern
- **Threading**: UI operations on main thread, database on background thread
- **Session Management**: SharedPreferences for simple key-value storage
- **Material Design**: Google's design system for modern Android apps

---

## 🎉 CONCLUSION

Your Traffic Log app is **complete and ready for submission**. All core features are implemented, the code is clean and understandable, and the app meets all assignment requirements.

**Good luck with your submission!**

If you need to make any changes or additions, all the code is well-structured and commented for easy modification.

---

**Last Updated**: 2025-01-18
**Project**: Traffic Log Android App
**Module**: CI560 - Mobile Application Development
**University**: University of Brighton
