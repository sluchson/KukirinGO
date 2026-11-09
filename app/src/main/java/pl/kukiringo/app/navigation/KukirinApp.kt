package pl.kukiringo.app.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pl.kukiringo.app.R
import pl.kukiringo.app.ui.screens.clips.ClipsScreen
import pl.kukiringo.app.ui.screens.map.MapScreen
import pl.kukiringo.app.ui.screens.meetups.MeetupsScreen
import pl.kukiringo.app.ui.screens.profile.ProfileScreen
import pl.kukiringo.app.ui.screens.ride.RideScreen

/** Zakładki dolnego paska nawigacji. */
enum class TopDestination(val route: String, val label: String, val icon: ImageVector) {
    MAP("map", "Mapa", Icons.Filled.Map),
    RIDE("ride", "Jazda", Icons.Filled.ElectricScooter),
    MEETUPS("meetups", "Zloty", Icons.Filled.Groups),
    CLIPS("clips", "kukiTok", Icons.Filled.PlayCircle),
    PROFILE("profile", "Profil", Icons.Filled.Person),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KukirinApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.logo_kukiringo),
                        contentDescription = "kukirinGO",
                        modifier = Modifier.height(26.dp),
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                TopDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.MAP.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopDestination.MAP.route) { MapScreen() }
            composable(TopDestination.RIDE.route) { RideScreen() }
            composable(TopDestination.MEETUPS.route) { MeetupsScreen() }
            composable(TopDestination.CLIPS.route) { ClipsScreen() }
            composable(TopDestination.PROFILE.route) { ProfileScreen() }
        }
    }
}
