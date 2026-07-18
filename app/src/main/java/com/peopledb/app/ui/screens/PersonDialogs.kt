package com.peopledb.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peopledb.app.data.PersonSummary

@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, fromYear: Int?, toYear: Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var fromYear by remember { mutableStateOf("") }
    var toYear by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add place") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Place name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = fromYear,
                        onValueChange = { fromYear = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("From year") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = toYear,
                        onValueChange = { toYear = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("To year") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Leave 'To year' blank if they still live there.")
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), fromYear.toIntOrNull(), toYear.toIntOrNull()) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add tag") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tag name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRelationshipDialog(
    people: List<PersonSummary>,
    onDismiss: () -> Unit,
    onConfirm: (relatedPersonId: Long, type: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<PersonSummary?>(null) }
    var type by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add relationship") },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = selectedPerson?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Person") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { expanded = true }
                            )
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (people.isEmpty()) {
                            DropdownMenuItem(text = { Text("No other people yet") }, onClick = { expanded = false })
                        }
                        people.forEach { person ->
                            DropdownMenuItem(
                                text = { Text(person.name) },
                                onClick = {
                                    selectedPerson = person
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Relationship (e.g. Sibling, Friend)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedPerson?.let { onConfirm(it.id, type.trim()) } },
                enabled = selectedPerson != null && type.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    initialText: String = "",
    initialEventAt: Long? = null,
    title: String = "Add note",
    onDismiss: () -> Unit,
    onConfirm: (text: String, eventAt: Long?) -> Unit
) {
    val zone = java.time.ZoneId.systemDefault()
    val initialDateTime = initialEventAt?.let {
        java.time.Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime()
    }

    var text by remember { mutableStateOf(initialText) }
    var useCustomDate by remember { mutableStateOf(initialEventAt != null) }
    var date by remember { mutableStateOf(initialDateTime?.toLocalDate() ?: java.time.LocalDate.now()) }
    var hourText by remember { mutableStateOf((initialDateTime?.hour ?: java.time.LocalTime.now().hour).toString()) }
    var minuteText by remember { mutableStateOf((initialDateTime?.minute ?: 0).toString().padStart(2, '0')) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(
                        checked = useCustomDate,
                        onCheckedChange = { useCustomDate = it }
                    )
                    Text("Set a specific date & time")
                }
                if (!useCustomDate) {
                    Text(
                        if (initialEventAt == null) "This note will be timestamped now."
                        else "Switching this off will use the note's original creation time.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(date.toString(), modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { showDatePicker = true }) { Text("Change date") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = { v ->
                                val digits = v.filter { it.isDigit() }.take(2)
                                hourText = digits
                            },
                            label = { Text("Hour (0-23)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = { v ->
                                val digits = v.filter { it.isDigit() }.take(2)
                                minuteText = digits
                            },
                            label = { Text("Minute (0-59)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val eventAt = if (useCustomDate) {
                        val hour = (hourText.toIntOrNull() ?: 0).coerceIn(0, 23)
                        val minute = (minuteText.toIntOrNull() ?: 0).coerceIn(0, 59)
                        java.time.LocalDateTime.of(date, java.time.LocalTime.of(hour, minute))
                            .atZone(zone)
                            .toInstant()
                            .toEpochMilli()
                    } else {
                        null
                    }
                    onConfirm(text.trim(), eventAt)
                },
                enabled = text.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val initialMillis = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}
