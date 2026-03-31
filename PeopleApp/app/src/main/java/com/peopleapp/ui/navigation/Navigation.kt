package com.peopleapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.peopleapp.ui.screens.addperson.AddEditPersonScreen
import com.peopleapp.ui.screens.home.HomeScreen
import com.peopleapp.ui.screens.persondetail.PersonDetailScreen
import com.peopleapp.ui.screens.search.SearchScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object AddPerson : Screen("add_person")
    object EditPerson : Screen("edit_person/{personId}") {
        fun createRoute(personId: String) = "edit_person/$personId"
    }
    object PersonDetail : Screen("person/{personId}") {
        fun createRoute(personId: String) = "person/$personId"
    }
}

@Composable
fun PeopleNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onAddPerson = { navController.navigate(Screen.AddPerson.route) },
                onPersonClick = { id -> navController.navigate(Screen.PersonDetail.createRoute(id)) },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onPersonClick = { id -> navController.navigate(Screen.PersonDetail.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddPerson.route) {
            AddEditPersonScreen(
                personId = null,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditPerson.route,
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStack ->
            val personId = backStack.arguments?.getString("personId")!!
            AddEditPersonScreen(
                personId = personId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PersonDetail.route,
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStack ->
            val personId = backStack.arguments?.getString("personId")!!
            PersonDetailScreen(
                personId = personId,
                onEdit = { navController.navigate(Screen.EditPerson.createRoute(personId)) },
                onBack = { navController.popBackStack() },
                onPersonClick = { id -> navController.navigate(Screen.PersonDetail.createRoute(id)) }
            )
        }
    }
}
