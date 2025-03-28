package com.example.redtaximappoc.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redtaximappoc.R

/**
 * Defines the different screens used in the app's navigation.
 */
sealed class RTScreen(
    val route: String,
    @StringRes val title: Int = R.string.ride,
    val navIcon: (@Composable () -> Unit) = {
        Icon(
            Icons.Filled.Home, contentDescription = "home"
        )
    },
) {
    data object Map : RTScreen("map")
    data object Ride : RTScreen("ride")
    data object Booking : RTScreen("booking")
    data object Profile : RTScreen("profile")

    // Bottom Navigation
    data object RideNav : RTScreen(
        route = "ride",
        title = R.string.ride,
        navIcon = {
            Icon(
                Icons.Filled.Home,
                contentDescription = "ride",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .offset(x = 10.dp)
            )
        }
    )

    data object BookingNav : RTScreen(
        route = "booking",
        title = R.string.booking,
        navIcon = {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = "booking",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .offset(x = 10.dp)
            )
        }
    )

    data object ProfileNav : RTScreen(
        route = "profile",
        title = R.string.profile,
        navIcon = {
            Icon(
                Icons.Filled.AccountBox,
                contentDescription = "profile",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .offset(x = 10.dp)
            )
        }
    )
}