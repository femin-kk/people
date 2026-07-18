package com.peopledb.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peopledb.app.data.Person
import com.peopledb.app.ui.components.PersonAvatar
import com.peopledb.app.util.PhotoStorage
import com.peopledb.app.viewmodel.AppViewModel
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonScreen(
    viewModel: AppViewModel,
    personId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val existingPerson by (if (personId != null) viewModel.observePerson(personId) else remember { kotlinx.coroutines.flow.flowOf(null) })
        .collectAsState(initial = null)

    var name by remember { mutableStateOf("") }
    var birthdayEpochDay by remember { mutableStateOf<Long?>(null) }
    var yearKnown by remember { mutableStateOf(true) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(existingPerson) {
        if (!initialized && existingPerson != null) {
            name = existingPerson!!.name
            birthdayEpochDay = existingPerson!!.birthdayEpochDay
            yearKnown = existingPerson!!.birthdayYearKnown
            photoPath = existingPerson!!.primaryPhotoPath
            initialized = true
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = PhotoStorage.copyToInternalStorage(context, uri)
            if (savedPath != null) photoPath = savedPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (personId == null) "Add Person" else "Edit Person") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PersonAvatar(name = name.ifBlank { "?" }, photoPath = photoPath, size = 96.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(0.dp))
                        Text("  Choose photo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            val bdayLabel = birthdayEpochDay?.let {
                val d = java.time.LocalDate.ofEpochDay(it)
                if (yearKnown) d.toString() else "${d.monthValue}/${d.dayOfMonth}"
            } ?: "Not set"

            Text("Birthday", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(bdayLabel, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(if (birthdayEpochDay == null) "Set date" else "Change")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = !yearKnown, onCheckedChange = { yearKnown = !it })
                Text("Year unknown (only show month/day)")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (personId == null) {
                            viewModel.addPerson(name.trim(), birthdayEpochDay, yearKnown) { newId ->
                                photoPath?.let { path -> viewModel.setPrimaryPhoto(newId, path) }
                            }
                        } else {
                            viewModel.updatePerson(
                                Person(
                                    id = personId,
                                    name = name.trim(),
                                    birthdayEpochDay = birthdayEpochDay,
                                    birthdayYearKnown = yearKnown,
                                    primaryPhotoPath = photoPath,
                                    createdAt = existingPerson?.createdAt ?: System.currentTimeMillis()
                                )
                            )
                        }
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = birthdayEpochDay?.let {
            java.time.LocalDate.ofEpochDay(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        birthdayEpochDay = date.toEpochDay()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
