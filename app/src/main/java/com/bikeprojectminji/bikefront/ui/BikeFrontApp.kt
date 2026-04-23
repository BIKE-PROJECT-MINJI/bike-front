package com.bikeprojectminji.bikefront.ui

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bikeprojectminji.bikefront.analytics.AnalyticsTracker
import com.bikeprojectminji.bikefront.auth.AuthProfileActivity
import com.bikeprojectminji.bikefront.free.FreeRideActivity
import com.bikeprojectminji.bikefront.ui.screen.*
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme

@Composable
fun BikeFrontApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val analyticsTracker = remember(context) { AnalyticsTracker(context) }
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    LaunchedEffect(Unit) {
        analyticsTracker.track("app_opened", "app_shell", mapOf("entry" to "cold_start"))
    }

    GajaTheme {
        Scaffold(
            bottomBar = {
                if (BikeFrontRoute.isTopLevelDestination(currentRoute)) {
                    NavigationBar {
                        AppShellDestination.entries.forEach { destination ->
                            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(BikeFrontRoute.RIDE_START) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (destination == AppShellDestination.HOME) Icons.Filled.Home else Icons.Filled.Map,
                                        contentDescription = destination.label,
                                    )
                                },
                                label = { Text(destination.label) }
                            )
                        }
                    }
                }
            }
        ) { appShellPadding ->
            NavHost(
                navController = navController,
                startDestination = BikeFrontRoute.SPLASH,
                modifier = Modifier.padding(appShellPadding),
            ) {
                composable(BikeFrontRoute.SPLASH) {
                    SplashScreen(onAnimationFinished = {
                        navController.navigate(BikeFrontRoute.RIDE_START) {
                            popUpTo(BikeFrontRoute.SPLASH) { inclusive = true }
                        }
                    })
                }

                composable(BikeFrontRoute.RIDE_START) {
                    RideStartScreen(
                        innerPadding = PaddingValues(),
                        onStartFreeRide = {
                            analyticsTracker.track("ride_start_clicked", "course_list", mapOf("button" to "free_ride_quick_start"))
                            navController.navigate(BikeFrontRoute.FREE_RIDE_PRE)
                        },
                        onOpenCourse = { course ->
                            analyticsTracker.track("course_selected", "course_list", mapOf("courseId" to course.id, "source" to if (course.featuredRank != null) "recommended" else "list"))
                            navController.navigate(BikeFrontRoute.coursePre(course.id))
                        },
                        onOpenMyInfo = { navController.navigate(BikeFrontRoute.MY_INFO) }
                    )
                }

                composable(BikeFrontRoute.COURSES) {
                    CoursesScreen(
                        innerPadding = PaddingValues(),
                        onOpenCourse = { course ->
                            analyticsTracker.track("course_selected", "course_list", mapOf("courseId" to course.id, "source" to if (course.featuredRank != null) "recommended" else "list"))
                            navController.navigate(BikeFrontRoute.coursePre(course.id))
                        },
                    )
                }

                composable(BikeFrontRoute.FREE_RIDE_PRE) {
                    FreeRidePreRideScreen(
                        innerPadding = PaddingValues(),
                        onBack = { navController.popBackStack() },
                        onStartRide = {
                            context.startActivity(Intent(context, FreeRideActivity::class.java))
                        }
                    )
                }

                composable(BikeFrontRoute.COURSE_PRE) { backStackEntry ->
                    val courseIdString = backStackEntry.arguments?.getString("courseId") ?: "0"
                    val courseIdLong = courseIdString.toLongOrNull() ?: 0L

                    CoursePreRideScreen(
                        innerPadding = PaddingValues(),
                        course = CourseCardUiModel(
                            id = courseIdLong,
                            title = "선택된 코스",
                            distanceKm = 15.2,
                            estimatedDurationMin = 45
                        ),
                        onBack = { navController.popBackStack() },
                        onStartRide = {
                            val intent = Intent(context, FreeRideActivity::class.java)
                            intent.putExtra("extra_course_id", courseIdLong)
                            context.startActivity(intent)
                        }
                    )
                }

                composable(BikeFrontRoute.MY_INFO) {
                    MyInfoScreen(
                        innerPadding = PaddingValues(),
                        onOpenProfile = {
                            context.startActivity(Intent(context, AuthProfileActivity::class.java))
                        },
                        onOpenCourses = { navController.navigate(BikeFrontRoute.COURSES) },
                        onOpenRideRecords = { navController.navigate(BikeFrontRoute.RIDE_RECORDS) },
                    )
                }

                composable(BikeFrontRoute.RIDE_RECORDS) {
                    RideRecordsScreen(
                        innerPadding = PaddingValues(),
                        onOpenProfile = {
                            context.startActivity(Intent(context, AuthProfileActivity::class.java))
                        },
                        onOpenCourse = { linkedCourseId ->
                            navController.navigate(BikeFrontRoute.coursePre(linkedCourseId))
                        },
                    )
                }
            }
        }
    }
}
