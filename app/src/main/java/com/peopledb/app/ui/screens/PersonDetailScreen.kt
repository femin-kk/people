package com.peopledb.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peopledb.app.data.Note
import com.peopledb.app.data.Photo
import com.peopledb.app.ui.components.PersonAvatar
import com.peopledb.app.util.BirthdayUtils
import com.peopledb.app.util.RelativeTime
import com.peopledb.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    viewModel: AppViewModel,
    personId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onOpenPerson: (Long) -> Unit
) {
    val person by viewModel.observePerson(personId).collectAsState(initial = null)
    val places by viewModel.observePlacesForPerson(personId).collectAsState(initial = emptyList())
    val tags by viewModel.observeTagsForPerson(personId).collectAsState(initial = emptyList())
    val relationships by viewModel.observeRelationshipsForPerson(personId).collectAsState(initial = emptyList())
    val notes by viewModel.observeNotesForPerson(personId).collectAsState(initial = emptyList())
    val photos by viewModel.observePhotosForPerson(personId).collectAsState(initial = emptyList())
    val allPeople by viewModel.observeAllPeople().collectAsState(initial = emptyList())

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddPlace by remember { mutableStateOf(false) }
    var showAddTag by remember { mutableStateOf(false) }
    var showAddRelationship by remember { mutableStateOf(false) }
    var showAddNote by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.addPhotoFromUri(personId, uri, setAsPrimary = person?.primaryPhotoPath == null)
        }
    }

    val p = person
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(p?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        if (p == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                PersonAvatar(name = p.name, photoPath = p.primaryPhotoPath, size = 80.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    val bday = BirthdayUtils.format(p.birthdayEpochDay, p.birthdayYearKnown)
                    if (bday != null) {
                        val age = BirthdayUtils.age(p.birthdayEpochDay, p.birthdayYearKnown)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Cake, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (age != null) "$bday · Age $age" else bday)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Photos (${photos.size})") {
                photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            if (photos.isNotEmpty()) {
                LazyRow {
                    items(photos, key = { it.id }) { photo ->
                        PhotoThumb(
                            photo = photo,
                            isPrimary = photo.filePath == p.primaryPhotoPath,
                            onSetPrimary = { viewModel.setPrimaryPhoto(personId, photo.filePath) },
                            onDelete = { viewModel.deletePhoto(photo) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(title = "Places (${places.size})") { showAddPlace = true }
            if (places.isEmpty()) {
                Text("No places tagged yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                places.forEach { place ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(place.placeName, fontWeight = FontWeight.Medium)
                                val range = yearRangeLabel(place.fromYear, place.toYear)
                                if (range != null) Text(range, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.removePersonPlace(place.personPlaceId) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(title = "Tags (${tags.size})") { showAddTag = true }
            Row {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag.name) },
                        modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                    )
                }
            }
            if (tags.isEmpty()) Text("No tags yet.", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(title = "Relationships (${relationships.size})") { showAddRelationship = true }
            if (relationships.isEmpty()) {
                Text("No relationships added yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                relationships.forEach { rel ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PersonAvatar(name = rel.relatedPersonName, photoPath = rel.relatedPersonPhotoPath, size = 40.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    rel.relatedPersonName,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onOpenPerson(rel.relatedPersonId) }
                                    )
                                )
                                Text(rel.type, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.deleteRelationship(rel.relationshipId) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(title = "Notes (${notes.size})") { showAddNote = true }
            if (notes.isEmpty()) {
                Text("No notes yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                notes.forEach { note ->
                    NoteRow(note = note, onDelete = { viewModel.deleteNote(note) })
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showDeleteConfirm && p != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${p.name}?") },
            text = { Text("This will permanently remove this person, their notes, places, tags, relationships, and photos.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deletePerson(p) { onDeleted() }
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddPlace) {
        AddPlaceDialog(
            onDismiss = { showAddPlace = false },
            onConfirm = { name, from, to ->
                viewModel.addPlace(personId, name, from, to)
                showAddPlace = false
            }
        )
    }

    if (showAddTag) {
        AddTagDialog(
            onDismiss = { showAddTag = false },
            onConfirm = { name ->
                viewModel.addGenericTag(personId, name)
                showAddTag = false
            }
        )
    }

    if (showAddRelationship) {
        AddRelationshipDialog(
            people = allPeople.filter { it.id != personId },
            onDismiss = { showAddRelationship = false },
            onConfirm = { relatedId, type ->
                viewModel.addRelationship(personId, relatedId, type)
                showAddRelationship = false
            }
        )
    }

    if (showAddNote) {
        AddNoteDialog(
            onDismiss = { showAddNote = false },
            onConfirm = { text ->
                viewModel.addNote(personId, text)
                showAddNote = false
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = onAdd) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }
    }
}

@Composable
private fun PhotoThumb(photo: Photo, isPrimary: Boolean, onSetPrimary: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.padding(4.dp)
    ) {
        val shape = RoundedCornerShape(8.dp)
        Image(
            painter = rememberAsyncImagePainter(photo.filePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(shape)
                .then(
                    if (isPrimary) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showMenu = true }
                )
        )
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Set as primary") }, onClick = { onSetPrimary(); showMenu = false })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false })
        }
    }
}

@Composable
private fun NoteRow(note: Note, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.text)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    RelativeTime.format(note.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Delete note")
            }
        }
    }
}

private fun yearRangeLabel(from: Int?, to: Int?): String? {
    return when {
        from != null && to != null -> "$from – $to"
        from != null -> "$from – present"
        to != null -> "until $to"
        else -> null
    }
}


