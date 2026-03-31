package com.peopleapp.ui.screens.persondetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peopleapp.data.model.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFactDialog(onDismiss: () -> Unit, onAdd: (category: String, label: String, value: String) -> Unit) {
    var selectedCategory by remember { mutableStateOf(FactCategory.CUSTOM) }
    var label by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var labelError by remember { mutableStateOf(false) }
    var valueError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Category", style = MaterialTheme.typography.labelMedium)
                FactCategory.allCategories.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (cat, catLabel) ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    if (cat != FactCategory.CUSTOM) label = catLabel
                                },
                                label = { Text(catLabel, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it; labelError = false },
                    label = { Text("Label *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = labelError
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it; valueError = false },
                    label = { Text("Value *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = valueError
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                labelError = label.isBlank()
                valueError = value.isBlank()
                if (!labelError && !valueError) {
                    onAdd(selectedCategory, label.trim(), value.trim())
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(onDismiss: () -> Unit, onAdd: (type: String, label: String, date: Long, notes: String) -> Unit) {
    var selectedType by remember { mutableStateOf(EventType.CUSTOM) }
    var label by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Type", style = MaterialTheme.typography.labelMedium)
                EventType.allTypes.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (type, typLabel) ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    if (type != EventType.CUSTOM) label = typLabel
                                },
                                label = { Text(typLabel, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it; dateError = null },
                    label = { Text("Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = dateError != null,
                    supportingText = dateError?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedDate = runCatching {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.isLenient = false
                    sdf.parse(dateString.trim())?.time
                }.getOrNull()
                if (parsedDate == null) {
                    dateError = "Enter date as YYYY-MM-DD"
                } else {
                    val finalLabel = label.ifBlank {
                        EventType.allTypes.find { it.first == selectedType }?.second ?: selectedType
                    }
                    onAdd(selectedType, finalLabel, parsedDate, notes.trim())
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRelationshipDialog(
    currentPersonId: String,
    people: List<com.peopleapp.data.model.Person>,
    onDismiss: () -> Unit,
    onAdd: (toPersonId: String, type: String, label: String, notes: String) -> Unit
) {
    var selectedPersonId by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf(RelationshipType.FRIEND) }
    var notes by remember { mutableStateOf("") }
    var personError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Relationship") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Connect to", style = MaterialTheme.typography.labelMedium)
                if (people.isEmpty()) {
                    Text("No other people available. Add more people first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = people.find { it.id == selectedPersonId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Person *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            isError = personError
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            people.forEach { person ->
                                DropdownMenuItem(
                                    text = { Text(person.name) },
                                    onClick = { selectedPersonId = person.id; expanded = false; personError = false }
                                )
                            }
                        }
                    }
                }

                Text("Relationship Type", style = MaterialTheme.typography.labelMedium)
                RelationshipType.allTypes.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (type, typLabel) ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(typLabel, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                personError = selectedPersonId == null
                if (!personError) {
                    val label = RelationshipType.allTypes.find { it.first == selectedType }?.second ?: selectedType
                    onAdd(selectedPersonId!!, selectedType, label, notes.trim())
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
