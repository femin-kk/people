package com.peopleapp.ui.screens.persondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peopleapp.data.model.*
import com.peopleapp.data.repository.PeopleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val repository: PeopleRepository
) : ViewModel() {

    private val _personId = MutableStateFlow("")
    val personDetail: StateFlow<PersonDetail?> = _personId
        .filter { it.isNotBlank() }
        .flatMapLatest { repository.getPersonDetail(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All people for relationship picker
    val allPeople: StateFlow<List<Person>> = repository.getAllPeople()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPerson(personId: String) {
        _personId.value = personId
    }

    // Facts
    fun addFact(personId: String, category: String, label: String, value: String) {
        viewModelScope.launch {
            repository.saveFact(Fact(
                id = UUID.randomUUID().toString(),
                personId = personId,
                category = category,
                label = label,
                value = value
            ))
        }
    }

    fun deleteFact(id: String) = viewModelScope.launch { repository.deleteFact(id) }

    // Events
    fun addEvent(personId: String, type: String, label: String, date: Long, notes: String) {
        viewModelScope.launch {
            repository.saveEvent(Event(
                id = UUID.randomUUID().toString(),
                personId = personId,
                type = type,
                label = label,
                date = date,
                notes = notes
            ))
        }
    }

    fun deleteEvent(id: String) = viewModelScope.launch { repository.deleteEvent(id) }

    // Relationships
    fun addRelationship(fromPersonId: String, toPersonId: String, type: String, label: String, notes: String) {
        viewModelScope.launch {
            repository.saveRelationship(Relationship(
                id = UUID.randomUUID().toString(),
                fromPersonId = fromPersonId,
                toPersonId = toPersonId,
                type = type,
                label = label,
                notes = notes
            ))
        }
    }

    fun deleteRelationship(id: String) = viewModelScope.launch { repository.deleteRelationship(id) }
}
