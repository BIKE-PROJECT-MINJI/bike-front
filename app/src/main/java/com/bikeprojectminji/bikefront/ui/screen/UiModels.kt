package com.bikeprojectminji.bikefront.ui.screen

data class CourseCardUiModel(
    val id: Long,
    val title: String,
    val distanceKm: Double,
    val estimatedDurationMin: Int,
    val featuredRank: Int? = null,
    val isRecorded: Boolean = false,
)

data class CoursesPageUiModel(
    val items: List<CourseCardUiModel>,
    val hasNext: Boolean,
    val nextCursor: String?,
)
