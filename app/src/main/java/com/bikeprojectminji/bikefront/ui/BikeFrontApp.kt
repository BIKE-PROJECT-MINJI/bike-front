package com.bikeprojectminji.bikefront.ui

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bikeprojectminji.bikefront.auth.AuthProfileActivity
import com.bikeprojectminji.bikefront.free.FreeRideActivity
import com.bikeprojectminji.bikefront.ui.screen.*
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme

@Composable
fun BikeFrontApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    GajaTheme {
        NavHost(navController = navController, startDestination = "splash") {
            // New Splash Screen Entry
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
                    onStartFreeRide = { navController.navigate("free_ride_pre") },
                    onOpenCourse = { course ->
                        navController.navigate("course_pre/${course.id}")
                    }
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
