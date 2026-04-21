package com.bikeprojectminji.bikefront.ui.screen

internal data class ProfileSessionSnapshot(
    val loggedIn: Boolean,
    val displayName: String?
)

internal object ProfileSessionStateResolver {
    fun resolve(isSignedIn: Boolean, displayName: String?): ProfileSessionSnapshot {
        if (!isSignedIn) {
            return ProfileSessionSnapshot(loggedIn = false, displayName = null)
        }
        val normalizedName = displayName?.takeIf { it.isNotBlank() } ?: "bikeoasis"
        return ProfileSessionSnapshot(loggedIn = true, displayName = normalizedName)
    }
}
