# Voyager 🧭 (Android - Kotlin)
### Smart Tourist Guide + Emergency Navigation App

Voyager is a Kotlin-based Android application inspired by the **Smart Guide** app, designed to maximize usability for tourists by combining **Explore + Maps + Emergency SOS safety** in one platform.

Unlike typical travel guide apps, Voyager focuses strongly on **tourist safety**, enabling users to quickly send emergency alerts, share their last location even in offline conditions, and trigger SOS actions with one tap.

---

## 🎯 Motivation / Why Voyager?
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

## 📱 App Modules (Bottom Navigation)

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
    "lat": 22.5726,
    "lng": 88.3639
  },
  "message": "I am in danger / lost. Please help."
}
