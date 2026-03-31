package com.peopleapp.ui.screens.addperson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peopleapp.data.model.Person
import com.peopleapp.data.repository.PeopleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditPersonUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val nickname: String = "",
    val email: String = "",
    val phone: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null
)

@HiltViewModel
class AddEditPersonViewModel @Inject constructor(
    private val repository: PeopleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditPersonUiState())
    val state: StateFlow<AddEditPersonUiState> = _state.asStateFlow()

    fun loadPerson(personId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val person = repository.getPersonById(personId)
            if (person != null) {
                _state.update {
                    it.copy(
                        id = person.id,
                        name = person.name,
                        nickname = person.nickname,
                        email = person.email,
                        phone = person.phone,
                        notes = person.notes,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = null) }
    fun onNicknameChange(v: String) = _state.update { it.copy(nickname = v) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v) }
    fun onNotesChange(v: String) = _state.update { it.copy(notes = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }
            return
        }
        viewModelScope.launch {
            repository.savePerson(
                Person(
                    id = s.id,
                    name = s.name.trim(),
                    nickname = s.nickname.trim(),
                    email = s.email.trim(),
                    phone = s.phone.trim(),
                    notes = s.notes.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            _state.update { it.copy(isSaved = true) }
        }
    }
}
