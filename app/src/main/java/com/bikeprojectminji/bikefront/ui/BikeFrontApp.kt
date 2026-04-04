package com.bikeprojectminji.bikefront.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bikeprojectminji.bikefront.auth.AuthProfileActivity
import com.bikeprojectminji.bikefront.free.FreeRideActivity
import com.bikeprojectminji.bikefront.ui.screen.CoursePreRideScreen
import com.bikeprojectminji.bikefront.ui.screen.CoursesScreen
import com.bikeprojectminji.bikefront.ui.screen.CourseCardUiModel
import com.bikeprojectminji.bikefront.ui.screen.FreeRidePreRideScreen
import com.bikeprojectminji.bikefront.ui.screen.MyInfoScreen
import com.bikeprojectminji.bikefront.ui.screen.RideStartScreen

private enum class MainTab(
    val route: String,
    val label: String,
) {
    RIDE_START("ride_start", "라이딩 시작"),
    COURSES("courses", "코스"),
    MY_INFO("my_info", "내 정보"),
}

private object Routes {
    const val FREE_RIDE_PRE_RIDE = "free_ride_pre_ride"
    const val COURSE_PRE_RIDE = "course_pre_ride/{courseId}/{title}/{distanceKm}/{durationMin}"

    fun coursePreRide(course: CourseCardUiModel): String {
        return "course_pre_ride/${course.id}/${Uri.encode(course.title)}/${course.distanceKm}/${course.estimatedDurationMin}"
    }
}

@Composable
fun BikeFrontApp() {
    val navController = rememberNavController()
    val tabs = MainTab.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            when (tab) {
                                MainTab.RIDE_START -> Icon(Icons.AutoMirrored.Outlined.DirectionsBike, contentDescription = tab.label)
                                MainTab.COURSES -> Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = tab.label)
                                MainTab.MY_INFO -> Icon(Icons.Outlined.PersonOutline, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.RIDE_START.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(MainTab.RIDE_START.route) {
                RideStartScreen(
                    innerPadding = innerPadding,
                    onOpenFreeRide = { navController.navigate(Routes.FREE_RIDE_PRE_RIDE) },
                    onOpenCourse = { navController.navigate(Routes.coursePreRide(it)) },
                    onOpenCourses = { navController.navigate(MainTab.COURSES.route) },
                    onOpenProfile = {
                        context.startActivity(Intent(context, AuthProfileActivity::class.java))
                    },
                )
            }
            composable(MainTab.COURSES.route) {
                CoursesScreen(
                    innerPadding = innerPadding,
                    onOpenCourse = { navController.navigate(Routes.coursePreRide(it)) },
                )
            }
            composable(MainTab.MY_INFO.route) {
                MyInfoScreen(
                    innerPadding = innerPadding,
                    onOpenProfile = {
                        context.startActivity(Intent(context, AuthProfileActivity::class.java))
                    },
                )
            }
            composable(Routes.FREE_RIDE_PRE_RIDE) {
                FreeRidePreRideScreen(
                    innerPadding = innerPadding,
                    onBack = { navController.popBackStack() },
                    onStartRide = {
                        context.startActivity(FreeRideActivity.newFreeRideIntent(context))
                    },
                )
            }
            composable(
                route = Routes.COURSE_PRE_RIDE,
                arguments = listOf(
                    navArgument("courseId") { type = NavType.LongType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("distanceKm") { type = NavType.FloatType },
                    navArgument("durationMin") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val course = CourseCardUiModel(
                    id = backStackEntry.arguments?.getLong("courseId") ?: -1L,
                    title = backStackEntry.arguments?.getString("title").orEmpty(),
                    distanceKm = backStackEntry.arguments?.getFloat("distanceKm")?.toDouble() ?: 0.0,
                    estimatedDurationMin = backStackEntry.arguments?.getInt("durationMin") ?: 0,
                )
                CoursePreRideScreen(
                    innerPadding = innerPadding,
                    course = course,
                    onBack = { navController.popBackStack() },
                    onStartRide = {
                        context.startActivity(FreeRideActivity.newCourseFollowIntent(context, course.id, course.title))
                    },
                )
            }
        }
    }
}
