package com.bikeprojectminji.bikefront.ui

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

    LaunchedEffect(Unit) {
        analyticsTracker.track("app_opened", "app_shell", mapOf("entry" to "cold_start"))
    }

    GajaTheme {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(onAnimationFinished = {
                    navController.navigate("ride_start") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }
            
            composable("ride_start") {
                RideStartScreen(
                    innerPadding = PaddingValues(),
                    onStartFreeRide = {
                        analyticsTracker.track("ride_start_clicked", "course_list", mapOf("button" to "free_ride_quick_start"))
                        navController.navigate("free_ride_pre")
                    },
                    onOpenCourse = { course ->
                        analyticsTracker.track("course_selected", "course_list", mapOf("courseId" to course.id, "source" to if (course.featuredRank != null) "recommended" else "list"))
                        navController.navigate("course_pre/${course.id}")
                    },
                    onOpenMyInfo = { navController.navigate("my_info") }
                )
            }
            
            composable("free_ride_pre") {
                FreeRidePreRideScreen(
                    innerPadding = PaddingValues(),
                    onBack = { navController.popBackStack() },
                    onStartRide = { 
                        context.startActivity(Intent(context, FreeRideActivity::class.java))
                    }
                )
            }
            
            composable("course_pre/{courseId}") { backStackEntry ->
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
            
            composable("my_info") {
                MyInfoScreen(
                    innerPadding = PaddingValues(),
                    onOpenProfile = {
                        context.startActivity(Intent(context, AuthProfileActivity::class.java))
                    }
                )
            }
        }
    }
}
