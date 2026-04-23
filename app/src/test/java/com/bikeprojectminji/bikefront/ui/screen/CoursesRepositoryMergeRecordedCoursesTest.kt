package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.course.RecordedCourseItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoursesRepositoryMergeRecordedCoursesTest {

    @Test
    fun `merge marks only canonical items that also exist in recorded store`() {
        val items = listOf(
            CourseCardUiModel(id = 10L, title = "백엔드 코스", distanceKm = 10.0, estimatedDurationMin = 30),
            CourseCardUiModel(id = 20L, title = "다른 코스", distanceKm = 20.0, estimatedDurationMin = 50),
        )
        val recordedItems = listOf(
            RecordedCourseItem(id = 10L, title = "로컬 저장 코스", distanceKm = 11.0, estimatedDurationMin = 31),
            RecordedCourseItem(id = 99L, title = "로컬 전용 코스", distanceKm = 5.0, estimatedDurationMin = 15),
        )

        val merged = mergeRecordedCoursesWithCanonicalItems(items, recordedItems)

        assertEquals(2, merged.size)
        assertTrue(merged.first { it.id == 10L }.isRecorded)
        assertFalse(merged.first { it.id == 20L }.isRecorded)
        assertFalse(merged.any { it.id == 99L })
    }
}
