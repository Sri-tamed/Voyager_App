package com.example.voyager.data.model


/**
 * Represents the emergency state of the app
 */
sealed class EmergencyState {
    /**
     * No emergency active
     */
    object Inactive : EmergencyState()

    /**
     * Emergency is active
     * @param triggeredAt Timestamp when emergency was triggered
     * @param source How the emergency was triggered
     */
    data class Active(
        val triggeredAt: Long,
        val source: TriggerSource
    ) : EmergencyState()
}