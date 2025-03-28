package com.example.redtaximappoc.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.redtaximappoc.ui.screens.ride.RTMap
import com.example.redtaximappoc.ui.screens.ride.RTSelectRide

/**
 * Sets up the navigation graph for the app.
 *
 * @param navController The [NavHostController] used for handling navigation.
 */
@Composable
fun Navigation(
    navController: NavHostController,
) {
    NavHost(navController, startDestination = RTScreen.Map.route) {
        composable(RTScreen.Map.route) {
            RTMap(navController)
        }

        composable(RTScreen.Ride.route) {
            RTSelectRide()
        }

        composable(RTScreen.Booking.route) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Blue)
            ) {

            }
        }

        composable(RTScreen.Profile.route) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
            ) {

            }
        }
    }
}

/**
 * Retrieves the current route of the navigation controller.
 *
 * This function observes the current back stack entry of the `NavController` and extracts
 * the route of the current destination. It removes any parameters or trailing segments
 * after the last `/` to return only the base route.
 *
 * @param navController The [NavController] used for navigation in the app.
 * @return The base route of the current destination, or `null` if there is no active route.
 */
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route?.substringBeforeLast("/")
}