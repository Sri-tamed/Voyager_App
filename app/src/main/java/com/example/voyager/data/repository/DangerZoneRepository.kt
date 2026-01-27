package com.example.voyager.data.repository

import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.DangerZone

object DangerZoneRepository {

    fun getMockDangerZones(): List<DangerZone> = listOf(
        DangerZone(
            id = "zone_1",
            name = "High Crime Area - Downtown",
            latitude = 22.5726,
            longitude = 88.3639,
            radius = 500f,
            dangerLevel = DangerLevel.HIGH  // ✅ Changed from DANGER to HIGH
        ),
        DangerZone(
            id = "zone_2",
            name = "Caution Zone - Market District",
            latitude = 22.5826,
            longitude = 88.3539,
            radius = 300f,
            dangerLevel = DangerLevel.MODERATE  // ✅ Changed from CAUTION to MODERATE
        ),
        DangerZone(
            id = "zone_3",
            name = "Extreme Danger - Industrial Area",
            latitude = 22.5626,
            longitude = 88.3739,
            radius = 400f,
            dangerLevel = DangerLevel.CRITICAL // ✅ Added EXTREME example
        ),
        DangerZone(
            id = "zone_4",
            name = "Safe Zone - Park Area",
            latitude = 22.5926,
            longitude = 88.3439,
            radius = 600f,
            dangerLevel = DangerLevel.SAFE  // ✅ Added SAFE example
        )
    )
}