package com.example.redtaximappoc.ui.screens.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.redtaximappoc.navigation.Navigation
import com.example.redtaximappoc.navigation.RTScreen
import com.example.redtaximappoc.navigation.currentRoute
import com.example.redtaximappoc.utils.singleTopNavigator

@Composable
fun RTMainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            if (currentRoute(navController) in listOf(
                    RTScreen.Map.route, RTScreen.Booking.route, RTScreen.Profile.route
                )
            ) {
                // BottomNavigationUI(navController)
            }
        },
        content = { paddingValue ->
            Box(Modifier.padding(paddingValue)) {
                Navigation(navController)
            }
        }
    )
}

@Composable
fun BottomNavigationUI(navController: NavController) {
    NavigationBar {
        val items = listOf(RTScreen.RideNav, RTScreen.BookingNav, RTScreen.ProfileNav)
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = item.navIcon,
                label = { Text(text = stringResource(id = item.title)) },
                selected = currentRoute(navController) == item.route,
                onClick = {
                    navController.navigate(item.route)
                })
        }
    }
}
