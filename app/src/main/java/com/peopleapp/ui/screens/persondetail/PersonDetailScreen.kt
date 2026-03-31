package com.peopleapp.ui.screens.persondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peopleapp.data.model.*
import com.peopleapp.ui.screens.home.PersonAvatar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: String,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    onPersonClick: (String) -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(personId) { viewModel.loadPerson(personId) }

    val detail by viewModel.personDetail.collectAsStateWithLifecycle()
    val allPeople by viewModel.allPeople.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Facts", "Events", "Relations", "Photos")

    // Dialogs
    var showAddFact by remember { mutableStateOf(false) }
    var showAddEvent by remember { mutableStateOf(false) }
    var showAddRelation by remember { mutableStateOf(false) }

    if (detail == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val person = detail!!.person

    if (showAddFact) {
        AddFactDialog(
            onDismiss = { showAddFact = false },
            onAdd = { cat, label, value ->
                viewModel.addFact(personId, cat, label, value)
                showAddFact = false
            }
        )
    }

    if (showAddEvent) {
        AddEventDialog(
            onDismiss = { showAddEvent = false },
            onAdd = { type, label, date, notes ->
                viewModel.addEvent(personId, type, label, date, notes)
                showAddEvent = false
            }
        )
    }

    if (showAddRelation) {
        AddRelationshipDialog(
            currentPersonId = personId,
            people = allPeople.filter { it.id != personId },
            onDismiss = { showAddRelation = false },
            onAdd = { toId, type, label, notes ->
                viewModel.addRelationship(personId, toId, type, label, notes)
                showAddRelation = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(person.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTab) {
                    0 -> showAddFact = true
                    1 -> showAddEvent = true
                    2 -> showAddRelation = true
                }
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                PersonHeader(person = person, profilePhoto = detail!!.profilePhoto)
            }

            // Tabs
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { i, tab ->
                        Tab(
                            selected = selectedTab == i,
                            onClick = { selectedTab = i },
                            text = { Text(tab) }
                        )
                    }
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> {
                    if (detail!!.facts.isEmpty()) {
                        item { EmptyTabContent("No facts yet", "Tap + to add occupation, hobbies, notes…") }
                    } else {
                        items(detail!!.facts, key = { it.id }) { fact ->
                            FactItem(fact = fact, onDelete = { viewModel.deleteFact(fact.id) })
                        }
                    }
                }
                1 -> {
                    if (detail!!.events.isEmpty()) {
                        item { EmptyTabContent("No events yet", "Tap + to add birthday, when you met…") }
                    } else {
                        items(detail!!.events, key = { it.id }) { event ->
                            EventItem(event = event, onDelete = { viewModel.deleteEvent(event.id) })
                        }
                    }
                }
                2 -> {
                    if (detail!!.relationships.isEmpty()) {
                        item { EmptyTabContent("No relationships yet", "Tap + to connect to another person") }
                    } else {
                        items(detail!!.relationships, key = { it.relationship.id }) { rwp ->
                            RelationshipItem(
                                rwp = rwp,
                                onPersonClick = { onPersonClick(rwp.person.id) },
                                onDelete = { viewModel.deleteRelationship(rwp.relationship.id) }
                            )
                        }
                    }
                }
                3 -> {
                    if (detail!!.photos.isEmpty()) {
                        item { EmptyTabContent("No photos yet", "Tap + to add a photo") }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonHeader(person: Person, profilePhoto: Photo?) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PersonAvatar(name = person.name, size = 88)
        Spacer(Modifier.height(12.dp))
        Text(person.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (person.nickname.isNotBlank()) {
            Text("\"${person.nickname}\"", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (person.email.isNotBlank() || person.phone.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (person.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(person.email, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (person.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(person.phone, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        if (person.notes.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(person.notes, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun FactItem(fact: Fact, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(fact.value, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(fact.label, color = MaterialTheme.colorScheme.primary) },
        leadingContent = {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Default.Info, null,
                    Modifier.padding(8.dp).size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun EventItem(event: Event, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(event.label, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Column {
                Text(event.relativeTime(), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall)
                Text(
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(event.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.notes.isNotBlank()) {
                    Text(event.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        leadingContent = {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                Icon(Icons.Default.DateRange, null,
                    Modifier.padding(8.dp).size(20.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun RelationshipItem(rwp: RelationshipWithPerson, onPersonClick: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(rwp.person.name, fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onPersonClick))
        },
        supportingContent = {
            Text(rwp.relationship.label, color = MaterialTheme.colorScheme.primary)
        },
        leadingContent = {
            PersonAvatar(name = rwp.person.name, size = 40)
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun EmptyTabContent(title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
