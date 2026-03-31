package com.peopleapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peopleapp.data.model.Person
import com.peopleapp.data.repository.PeopleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PeopleRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val people: StateFlow<List<Person>> = _query
        .debounce(200)
        .flatMapLatest { q ->
            if (q.isBlank()) repository.getAllPeople()
            else repository.searchPeople(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun deletePerson(id: String) {
        viewModelScope.launch { repository.deletePerson(id) }
    }
}
