package com.peopleapp.data.repository

import com.peopleapp.data.local.dao.*
import com.peopleapp.data.local.entity.*
import com.peopleapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepository @Inject constructor(
    private val personDao: PersonDao,
    private val factDao: FactDao,
    private val eventDao: EventDao,
    private val relationshipDao: RelationshipDao,
    private val photoDao: PhotoDao
) {

    // ---- People ----

    fun getAllPeople(): Flow<List<Person>> =
        personDao.getAllPeople().map { list -> list.map { it.toDomain() } }

    fun searchPeople(query: String): Flow<List<Person>> =
        personDao.searchPeople(query).map { list -> list.map { it.toDomain() } }

    suspend fun getPersonById(id: String): Person? =
        personDao.getPersonById(id)?.toDomain()

    fun getPersonByIdFlow(id: String): Flow<Person?> =
        personDao.getPersonByIdFlow(id).map { it?.toDomain() }

    suspend fun savePerson(person: Person) {
        personDao.insertPerson(person.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePerson(id: String) {
        personDao.softDeletePerson(id)
    }

    // ---- Person Detail (combined) ----

    fun getPersonDetail(personId: String): Flow<PersonDetail?> {
        val personFlow = personDao.getPersonByIdFlow(personId)
        val factsFlow = factDao.getFactsForPerson(personId)
        val eventsFlow = eventDao.getEventsForPerson(personId)
        val relationshipsFlow = relationshipDao.getRelationshipsForPerson(personId)
        val photosFlow = photoDao.getPhotosForPerson(personId)

        return combine(personFlow, factsFlow, eventsFlow, relationshipsFlow, photosFlow) {
            person, facts, events, relationships, photos ->
            person ?: return@combine null
            val domainPhotos = photos.map { it.toDomain() }
            val profilePhoto = domainPhotos.firstOrNull { it.isProfilePhoto }

            val relationshipsWithPerson = relationships.mapNotNull { rel ->
                val otherId = if (rel.fromPersonId == personId) rel.toPersonId else rel.fromPersonId
                val otherPerson = personDao.getPersonById(otherId)
                otherPerson?.let { RelationshipWithPerson(rel.toDomain(), it.toDomain()) }
            }

            PersonDetail(
                person = person.toDomain(),
                facts = facts.map { it.toDomain() },
                events = events.map { it.toDomain() },
                relationships = relationshipsWithPerson,
                photos = domainPhotos,
                profilePhoto = profilePhoto
            )
        }
    }

    // ---- Facts ----

    fun getFactsForPerson(personId: String): Flow<List<Fact>> =
        factDao.getFactsForPerson(personId).map { it.map { f -> f.toDomain() } }

    suspend fun saveFact(fact: Fact) {
        factDao.insertFact(fact.toEntity())
    }

    suspend fun deleteFact(id: String) {
        factDao.softDeleteFact(id)
    }

    // ---- Events ----

    fun getEventsForPerson(personId: String): Flow<List<Event>> =
        eventDao.getEventsForPerson(personId).map { it.map { e -> e.toDomain() } }

    suspend fun saveEvent(event: Event) {
        eventDao.insertEvent(event.toEntity())
    }

    suspend fun deleteEvent(id: String) {
        eventDao.softDeleteEvent(id)
    }

    // ---- Relationships ----

    fun getRelationshipsForPerson(personId: String): Flow<List<Relationship>> =
        relationshipDao.getRelationshipsForPerson(personId).map { it.map { r -> r.toDomain() } }

    suspend fun saveRelationship(relationship: Relationship) {
        relationshipDao.insertRelationship(relationship.toEntity())
    }

    suspend fun deleteRelationship(id: String) {
        relationshipDao.softDeleteRelationship(id)
    }

    // ---- Photos ----

    fun getPhotosForPerson(personId: String): Flow<List<Photo>> =
        photoDao.getPhotosForPerson(personId).map { it.map { p -> p.toDomain() } }

    suspend fun savePhoto(photo: Photo) {
        if (photo.isProfilePhoto) {
            photoDao.clearProfilePhoto(photo.personId)
        }
        photoDao.insertPhoto(photo.toEntity())
    }

    suspend fun setProfilePhoto(photo: Photo) {
        photoDao.clearProfilePhoto(photo.personId)
        photoDao.updatePhoto(photo.toEntity().copy(isProfilePhoto = true))
    }

    suspend fun deletePhoto(id: String) {
        photoDao.softDeletePhoto(id)
    }

    // ---- Search ----

    fun searchAll(query: String): Flow<SearchResult> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flow { emit(SearchResult()) }
        }
        val peopleFlow = personDao.searchPeople(query)
        val factsFlow = factDao.searchFacts(query)

        return combine(peopleFlow, factsFlow) { people, facts ->
            val factPairs = facts.mapNotNull { fact ->
                val person = personDao.getPersonById(fact.personId)
                person?.let { fact.toDomain() to it.toDomain() }
            }
            SearchResult(
                people = people.map { it.toDomain() },
                facts = factPairs
            )
        }
    }

    // ---- Sync helpers ----

    suspend fun getUnsyncedPeople() = personDao.getUnsyncedPeople().map { it.toDomain() }
    suspend fun getUnsyncedFacts() = factDao.getUnsyncedFacts().map { it.toDomain() }
    suspend fun getUnsyncedEvents() = eventDao.getUnsyncedEvents().map { it.toDomain() }
    suspend fun getUnsyncedRelationships() = relationshipDao.getUnsyncedRelationships().map { it.toDomain() }

    suspend fun markPersonSynced(id: String) = personDao.markAsSynced(id)
}
