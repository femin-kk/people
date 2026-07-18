package com.peopledb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.peopledb.app.ui.nav.Routes
import com.peopledb.app.ui.screens.AddEditPersonScreen
import com.peopledb.app.ui.screens.PeopleListScreen
import com.peopledb.app.ui.screens.PersonDetailScreen
import com.peopledb.app.ui.screens.SettingsScreen
import com.peopledb.app.ui.screens.TagPeopleScreen
import com.peopledb.app.ui.screens.TagsScreen
import com.peopledb.app.ui.theme.PeopleDBTheme
import com.peopledb.app.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeopleDBTheme {
                PeopleDbApp(viewModel)
            }
        }
    }
}

@Composable
fun PeopleDbApp(viewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.PEOPLE_LIST) {
        composable(Routes.PEOPLE_LIST) {
            PeopleListScreen(
                viewModel = viewModel,
                onPersonClick = { id -> navController.navigate(Routes.personDetail(id)) },
                onAddPerson = { navController.navigate(Routes.ADD_PERSON) },
                onOpenTags = { navController.navigate(Routes.TAGS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ADD_PERSON) {
            AddEditPersonScreen(
                viewModel = viewModel,
                personId = null,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_PERSON,
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: return@composable
            AddEditPersonScreen(
                viewModel = viewModel,
                personId = personId,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PERSON_DETAIL,
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: return@composable
            PersonDetailScreen(
                viewModel = viewModel,
                personId = personId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.editPerson(personId)) },
                onDeleted = {
                    navController.popBackStack(Routes.PEOPLE_LIST, inclusive = false)
                },
                onOpenPerson = { otherId ->
                    navController.navigate(Routes.personDetail(otherId))
                }
            )
        }

        composable(Routes.TAGS) {
            TagsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTagClick = { tagId, isPlace, tagName ->
                    navController.navigate(Routes.tagPeople(tagId, isPlace, tagName))
                }
            )
        }

        composable(
            route = Routes.TAG_PEOPLE,
            arguments = listOf(
                navArgument("tagId") { type = NavType.LongType },
                navArgument("isPlace") { type = NavType.BoolType },
                navArgument("tagName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tagId = backStackEntry.arguments?.getLong("tagId") ?: return@composable
            val isPlace = backStackEntry.arguments?.getBoolean("isPlace") ?: false
            val tagName = backStackEntry.arguments?.getString("tagName")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: ""
            TagPeopleScreen(
                viewModel = viewModel,
                tagId = tagId,
                isPlace = isPlace,
                tagName = tagName,
                onBack = { navController.popBackStack() },
                onPersonClick = { id -> navController.navigate(Routes.personDetail(id)) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRestored = {
                    viewModel.clearBackupStatus()
                    navController.popBackStack(Routes.PEOPLE_LIST, inclusive = false)
                }
            )
        }
    }
}
