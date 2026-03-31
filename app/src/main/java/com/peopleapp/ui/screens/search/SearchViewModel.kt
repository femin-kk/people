package com.peopleapp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peopleapp.data.model.SearchResult
import com.peopleapp.data.repository.PeopleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: PeopleRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<SearchResult> = _query
        .debounce(250)
        .flatMapLatest { q ->
            if (q.length < 2) kotlinx.coroutines.flow.flow { emit(SearchResult()) }
            else repository.searchAll(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())

    fun onQueryChange(q: String) { _query.value = q }
}
