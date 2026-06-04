package com.bikeprojectminji.bikefront.curator

import android.content.Context

class CuratorTravelPreferenceStore(context: Context) {

    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isCompleted(): Boolean {
        return preferences.getBoolean(KEY_COMPLETED, false)
    }

    fun save(preference: CuratorTravelPreference): Boolean {
        val payload = CuratorTravelPreferencePayloadMapper.toMap(preference)
        return preferences.edit()
            .putBoolean(KEY_COMPLETED, true)
            .putString(KEY_RIDE_PURPOSE, payload.getValue("ridePurpose"))
            .putString(KEY_ROUTE_PRIORITY, payload.getValue("routePriority"))
            .putString(KEY_DISTANCE_COMFORT, payload.getValue("distanceComfort"))
            .putString(KEY_AVOID_CONDITION, payload.getValue("avoidCondition"))
            .commit()
    }

    fun read(): CuratorTravelPreference {
        val defaults = CuratorTravelPreference.default()
        return CuratorTravelPreference(
            ridePurpose = readEnum(KEY_RIDE_PURPOSE, defaults.ridePurpose),
            routePriority = readEnum(KEY_ROUTE_PRIORITY, defaults.routePriority),
            distanceComfort = readEnum(KEY_DISTANCE_COMFORT, defaults.distanceComfort),
            avoidConditions = readAvoidConditions(),
        )
    }

    fun clear() {
        preferences.edit().clear().commit()
    }

    private inline fun <reified T : Enum<T>> readEnum(key: String, fallback: T): T {
        val raw = preferences.getString(key, "") ?: ""
        return enumValues<T>().firstOrNull { it.name == raw } ?: fallback
    }

    private fun readAvoidConditions(): Set<AvoidCondition> {
        val raw = preferences.getString(KEY_AVOID_CONDITION, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(",")
            .mapNotNull { name -> AvoidCondition.entries.firstOrNull { it.name == name } }
            .toSet()
    }

    companion object {
        private const val PREF_NAME = "curator_travel_preference"
        private const val KEY_COMPLETED = "completed"
        private const val KEY_RIDE_PURPOSE = "ride_purpose"
        private const val KEY_ROUTE_PRIORITY = "route_priority"
        private const val KEY_DISTANCE_COMFORT = "distance_comfort"
        private const val KEY_AVOID_CONDITION = "avoid_condition"
    }
}
