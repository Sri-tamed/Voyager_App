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
            dangerLevel = DangerLevel.DANGER
        ),
        DangerZone(
            id = "zone_2",
            name = "Caution Zone - Market District",
            latitude = 22.5826,
            longitude = 88.3539,
            radius = 300f,
            dangerLevel = DangerLevel.CAUTION
        )
    )
}
