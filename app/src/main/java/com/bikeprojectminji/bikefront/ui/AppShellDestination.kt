package com.bikeprojectminji.bikefront.ui

enum class AppShellDestination(
    val route: String,
    val label: String,
) {
    HOME(BikeFrontRoute.RIDE_START, "홈"),
    COURSES(BikeFrontRoute.COURSES, "코스"),
}

object BikeFrontRoute {
    const val SPLASH = "splash"
    const val RIDE_START = "ride_start"
    const val COURSES = "courses"
    const val FREE_RIDE_PRE = "free_ride_pre"
    const val COURSE_PRE = "course_pre/{courseId}"
    const val MY_INFO = "my_info"

    fun coursePre(courseId: Long): String = "course_pre/$courseId"

    fun isTopLevelDestination(route: String?): Boolean {
        return AppShellDestination.entries.any { it.route == route }
    }
}
