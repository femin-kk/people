package com.peopledb.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peopledb.app.ui.components.PersonAvatar
import com.peopledb.app.util.BirthdayUtils
import com.peopledb.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagPeopleScreen(
    viewModel: AppViewModel,
    tagId: Long,
    isPlace: Boolean,
    tagName: String,
    onBack: () -> Unit,
    onPersonClick: (Long) -> Unit
) {
    val people by (if (isPlace) viewModel.getPeopleByPlaceTag(tagId) else viewModel.getPeopleByTag(tagId))
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tagName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (people.isEmpty()) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("No one tagged with \"$tagName\" yet.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn {
                    items(people, key = { it.id }) { person ->
                        ListItem(
                            leadingContent = { PersonAvatar(name = person.name, photoPath = person.primaryPhotoPath) },
                            headlineContent = { Text(person.name, fontWeight = FontWeight.Medium) },
                            supportingContent = {
                                val bday = BirthdayUtils.format(person.birthdayEpochDay, person.birthdayYearKnown)
                                if (bday != null) Text(bday)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onPersonClick(person.id) }
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
