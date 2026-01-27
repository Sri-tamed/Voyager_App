package com.example.voyager.data.model


/**
 * Represents how an emergency was triggered
 */
enum class TriggerSource {
    /**
     * User manually pressed emergency button
     */
    MANUAL,

    /**
     * Automatically triggered by app (e.g., danger detection)
     */
    AUTOMATIC,

    /**
     * Triggered by gesture (e.g., shake to alert)
     */
    GESTURE
}