package com.peopledb.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peopledb.app.PeopleDbApplication
import com.peopledb.app.backup.BackupManager
import com.peopledb.app.data.Note
import com.peopledb.app.data.Person
import com.peopledb.app.data.PersonPlace
import com.peopledb.app.data.PersonPlaceDetail
import com.peopledb.app.data.PersonSummary
import com.peopledb.app.data.PersonTag
import com.peopledb.app.data.Photo
import com.peopledb.app.data.Relationship
import com.peopledb.app.data.RelationshipDetail
import com.peopledb.app.data.Repository
import com.peopledb.app.data.Tag
import com.peopledb.app.data.TagWithCount
import com.peopledb.app.util.PhotoStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private var repo: Repository = (application as PeopleDbApplication).repository

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    // --- People ---
    fun observeAllPeople(): Flow<List<PersonSummary>> = repo.observeAllPeople()
    fun searchPeopleByName(query: String): Flow<List<PersonSummary>> = repo.searchPeopleByName(query)
    fun observePerson(id: Long): Flow<Person?> = repo.observePerson(id)

    fun addPerson(name: String, birthdayEpochDay: Long?, birthdayYearKnown: Boolean, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.addPerson(
                Person(name = name, birthdayEpochDay = birthdayEpochDay, birthdayYearKnown = birthdayYearKnown)
            )
            onDone(id)
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch { repo.updatePerson(person) }
    }

    fun deletePerson(person: Person, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            person.primaryPhotoPath?.let { PhotoStorage.delete(it) }
            repo.deletePerson(person)
            onDone()
        }
    }

    fun getPeopleByTag(tagId: Long): Flow<List<PersonSummary>> = repo.getPeopleByTag(tagId)
    fun getPeopleByPlaceTag(tagId: Long): Flow<List<PersonSummary>> = repo.getPeopleByPlaceTag(tagId)

    // --- Tags ---
    fun observeTagsWithCount(isPlace: Boolean): Flow<List<TagWithCount>> = repo.observeTagsWithCount(isPlace)
    fun searchTagsWithCount(query: String): Flow<List<TagWithCount>> = repo.searchTagsWithCount(query)
    fun observeTagsForPerson(personId: Long): Flow<List<Tag>> = repo.observeTagsForPerson(personId)

    fun addGenericTag(personId: Long, tagName: String) {
        if (tagName.isBlank()) return
        viewModelScope.launch {
            val tag = repo.getOrCreateTag(tagName, isPlace = false)
            repo.attachGenericTag(personId, tag.id)
        }
    }

    fun removeGenericTag(personTag: PersonTag, tag: Tag) {
        viewModelScope.launch {
            repo.detachGenericTag(personTag)
            repo.deleteTagIfUnused(tag)
        }
    }

    // --- Places ---
    fun observePlacesForPerson(personId: Long): Flow<List<PersonPlaceDetail>> = repo.observePlacesForPerson(personId)

    fun addPlace(personId: Long, placeName: String, fromYear: Int?, toYear: Int?) {
        if (placeName.isBlank()) return
        viewModelScope.launch { repo.addPlaceToPerson(personId, placeName, fromYear, toYear) }
    }

    fun updatePersonPlace(personPlace: PersonPlace) {
        viewModelScope.launch { repo.updatePersonPlace(personPlace) }
    }

    fun removePersonPlace(id: Long) {
        viewModelScope.launch { repo.removePersonPlace(id) }
    }

    // --- Relationships ---
    fun observeRelationshipsForPerson(personId: Long): Flow<List<RelationshipDetail>> = repo.observeRelationshipsForPerson(personId)

    fun addRelationship(personId: Long, relatedPersonId: Long, type: String) {
        if (type.isBlank() || personId == relatedPersonId) return
        viewModelScope.launch { repo.addRelationship(personId, relatedPersonId, type) }
    }

    fun deleteRelationship(relationshipId: Long) {
        viewModelScope.launch { repo.deleteRelationship(relationshipId) }
    }

    // --- Notes ---
    fun observeNotesForPerson(personId: Long): Flow<List<Note>> = repo.observeNotesForPerson(personId)

    fun addNote(personId: Long, text: String, eventAt: Long? = null) {
        if (text.isBlank()) return
        viewModelScope.launch { repo.addNote(personId, text, eventAt) }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { repo.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repo.deleteNote(note) }
    }

    // --- Photos ---
    fun observePhotosForPerson(personId: Long): Flow<List<Photo>> = repo.observePhotosForPerson(personId)

    fun addPhotoFromUri(personId: Long, uri: Uri, setAsPrimary: Boolean) {
        viewModelScope.launch {
            val path = PhotoStorage.copyToInternalStorage(getApplication<Application>(), uri) ?: return@launch
            repo.addPhoto(personId, path)
            if (setAsPrimary) {
                val person = repo.getPerson(personId) ?: return@launch
                repo.updatePerson(person.copy(primaryPhotoPath = path))
            }
        }
    }

    fun setPrimaryPhoto(personId: Long, path: String) {
        viewModelScope.launch {
            val person = repo.getPerson(personId) ?: return@launch
            repo.updatePerson(person.copy(primaryPhotoPath = path))
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            repo.deletePhoto(photo)
            val person = repo.getPerson(photo.personId)
            if (person?.primaryPhotoPath == photo.filePath) {
                repo.updatePerson(person.copy(primaryPhotoPath = null))
            }
            PhotoStorage.delete(photo.filePath)
        }
    }

    // --- Backup / Restore ---
    fun exportBackup(destinationUri: Uri) {
        viewModelScope.launch {
            when (val result = BackupManager.export(getApplication<Application>(), destinationUri)) {
                is BackupManager.Result.Success -> _backupStatus.value = "Backup saved successfully."
                is BackupManager.Result.Failure -> _backupStatus.value = "Backup failed: ${result.message}"
            }
        }
    }

    fun restoreBackup(sourceUri: Uri) {
        viewModelScope.launch {
            when (val result = BackupManager.restore(getApplication<Application>(), sourceUri)) {
                is BackupManager.Result.Success -> {
                    (getApplication<Application>() as PeopleDbApplication).initDatabase()
                    repo = (getApplication<Application>() as PeopleDbApplication).repository
                    _backupStatus.value = "RESTORED"
                }
                is BackupManager.Result.Failure -> _backupStatus.value = "Restore failed: ${result.message}"
            }
        }
    }
}
