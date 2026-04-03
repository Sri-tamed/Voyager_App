# Voyager 🧭 ( - Kotlin)
#Smart Tourist Guide + Emergency Navigation App

Voyager is a Kotlin-based Android application inspired by the **Smart Guide** app, designed to maximize usability for tourists by combining **Explore + Maps + Emergency SOS safety** in one platform.

Unlike typical travel guide apps, Voyager focuses strongly on **tourist safety**, enabling users to quickly send emergency alerts, share their last location even in offline conditions, and trigger SOS actions with one tap.

---

## Why Voyager?
Travelers often face problems like:
- getting lost in unfamiliar areas
- losing network connection mid-route
- needing emergency help quickly
- needing a reliable offline fallback

Voyager addresses these pain points using:
✅ Map-based navigation  
✅ Offline last-known location fallback  
✅ Emergency mode + SOS alert logic  
✅ Beep-based alert trigger for danger zones  

---

## 📱 Bottom Navigation

Voyager contains 3 main sections (similar to Smart Guide, but optimized for safety):

### 1️⃣ Explore
- Explore dashboard similar to **Smart Guide**
- Discover places, categories, and tourist points
- Future-ready for AI itinerary & recommendation system

### 2️⃣ Map
- Live map tracking using device GPS
- Shareable location button (Send current location)
- Offline fallback enabled:
  - If network drops, app still shows **last saved location**

### 3️⃣ Emergency (Voyager’s USP)
This is the key differentiator.

Emergency module supports:
- Add/select **Top 5 emergency contacts**
- Danger zone detection (future scope / optional geofencing)
- **Beep alert triggered inside map**
- SOS button:
  - sends emergency data to selected contacts

---

## 🛡️ Emergency + SOS Workflow

### ✅ Flow:
1. User enables Emergency Mode
2. App monitors danger zone / unsafe situation triggers
3. Beep alert plays inside the map UI
4. User taps **SOS**
5. SOS sends location payload to top 5 contacts
6. App displays last-known location even if offline

### 📦 Example SOS Payload (JSON Style)
```json
{
  "user": "Tourist User",
  "type": "SOS",
  "timestamp": "2026-01-18T10:30:00",
  "location": {
    "lat": 22.xyz,
    "lng": 88.abc
  },
  "message": "I am in danger / lost. Please help."
}


## PROJECT STRUCTURE :
   Voyager_App/
│── app/
│   ├── src/main/java/com/voyager/
│   │   ├── ui/
│   │   │   ├── explore/
│   │   │   ├── map/
│   │   │   ├── emergency/
│   │   ├── data/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── utils/
│   │   └── MainActivity.kt
│   └── src/main/res/
│
└── README.md
//  new 
com.example.voyager/
│
├── data/
│   ├── local/
│   │   ├── datastore/
│   │   │   ├── UserPreferencesSerializer.kt
│   │   │   └── EmergencyContactsSerializer.kt
│   │   └── cache/
│   │       └── LastLocationCache.kt
│   │
│   ├── remote/
│   │   ├── api/ (Future: Firebase/Supabase)
│   │   └── dto/
│   │
│   ├── model/
│   │   ├── Destination.kt
│   │   ├── Experience.kt
│   │   ├── EmergencyContact.kt
│   │   ├── LocationData.kt
│   │   └── DangerZone.kt
│   │
│   └── repository/
│       ├── LocationRepository.kt
│       ├── EmergencyRepository.kt
│       ├── DestinationRepository.kt
│       └── UserRepository.kt
│
├── domain/ (Business logic layer)
│   ├── usecase/
│   │   ├── TriggerSOSUseCase.kt
│   │   ├── GetLastLocationUseCase.kt
│   │   ├── CheckDangerZoneUseCase.kt
│   │   └── ShareLocationUseCase.kt
│   │
│   └── geofence/
│       ├── GeofencingManager.kt
│       └── DangerZoneDetector.kt
│
├── service/
│   ├── LocationTrackingService.kt (Foreground service)
│   ├── EmergencyAlarmService.kt
│   └── GeofenceTransitionsJobIntentService.kt
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Theme.kt
│   │   └── Shapes.kt
│   │
│   ├── components/
│   │   ├── GlassCard.kt
│   │   ├── GradientCard.kt
│   │   ├── VoyagerBottomBar.kt
│   │   ├── VoyagerSearchBar.kt
│   │   ├── DestinationCard.kt
│   │   ├── ExperienceCard.kt
│   │   ├── SOSFloatingButton.kt
│   │   ├── DangerLevelIndicator.kt
│   │   ├── PermissionBanner.kt
│   │   └── EmptyState.kt
│   │
│   ├── screens/
│   │   ├── splash/
│   │   │   ├── SplashScreen.kt
│   │   │   └── SplashViewModel.kt
│   │   │
│   │   ├── explore/
│   │   │   ├── ExploreScreen.kt
│   │   │   ├── ExploreViewModel.kt
│   │   │   ├── components/
│   │   │   │   ├── DestinationCarousel.kt
│   │   │   │   ├── ExperiencesList.kt
│   │   │   │   └── CategoryChips.kt
│   │   │   └── detail/
│   │   │       ├── DestinationDetailScreen.kt
│   │   │       └── DestinationDetailViewModel.kt
│   │   │
│   │   ├── map/
│   │   │   ├── MapScreen.kt
│   │   │   ├── MapViewModel.kt
│   │   │   └── components/
│   │   │       ├── MapControls.kt
│   │   │       ├── LocationShareSheet.kt
│   │   │       └── OfflineBanner.kt
│   │   │
│   │   ├── emergency/
│   │   │   ├── EmergencyScreen.kt
│   │   │   ├── EmergencyViewModel.kt
│   │   │   ├── contacts/
│   │   │   │   ├── EmergencyContactsScreen.kt
│   │   │   │   └── EmergencyContactsViewModel.kt
│   │   │   └── components/
│   │   │       ├── SOSButton.kt
│   │   │       ├── DangerMeter.kt
│   │   │       └── QuickActionsGrid.kt
│   │   │
│   │   ├── profile/
│   │   │   ├── ProfileScreen.kt
│   │   │   ├── ProfileViewModel.kt
│   │   │   └── settings/
│   │   │       ├── SettingsScreen.kt
│   │   │       └── SettingsViewModel.kt
│   │   │
│   │   └── permissions/
│   │       ├── PermissionScreen.kt
│   │       └── PermissionViewModel.kt
│   │
│   └── navigation/
│       ├── NavGraph.kt
│       ├── Screen.kt
│       └── VoyagerNavHost.kt
│
├── utils/
│   ├── Constants.kt
│   ├── Extensions.kt
│   ├── PermissionUtils.kt
│   ├── DateTimeUtils.kt
│   └── NetworkUtils.kt
│
└── VoyagerApplication.kt

