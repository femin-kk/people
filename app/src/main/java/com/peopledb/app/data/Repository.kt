package com.peopledb.app.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    private val personDao = db.personDao()
    private val tagDao = db.tagDao()
    private val personTagDao = db.personTagDao()
    private val personPlaceDao = db.personPlaceDao()
    private val relationshipDao = db.relationshipDao()
    private val noteDao = db.noteDao()
    private val photoDao = db.photoDao()

    // --- People ---
    fun observeAllPeople(): Flow<List<PersonSummary>> = personDao.observeAll()
    fun searchPeopleByName(query: String): Flow<List<PersonSummary>> = personDao.searchByName(query)
    fun observePerson(id: Long): Flow<Person?> = personDao.observeById(id)
    suspend fun getPerson(id: Long): Person? = personDao.getById(id)
    suspend fun addPerson(person: Person): Long = personDao.insert(person)
    suspend fun updatePerson(person: Person) = personDao.update(person)
    suspend fun deletePerson(person: Person) = personDao.delete(person)
    fun getPeopleByTag(tagId: Long): Flow<List<PersonSummary>> = personDao.getPeopleByTag(tagId)
    fun getPeopleByPlaceTag(tagId: Long): Flow<List<PersonSummary>> = personDao.getPeopleByPlaceTag(tagId)

    // --- Tags ---
    fun observeAllTags(): Flow<List<Tag>> = tagDao.observeAll()
    fun observeTagsWithCount(isPlace: Boolean): Flow<List<TagWithCount>> = tagDao.observeTagsWithCount(isPlace)
    fun searchTagsWithCount(query: String): Flow<List<TagWithCount>> = tagDao.searchTagsWithCount(query)
    fun observeTagsForPerson(personId: Long): Flow<List<Tag>> = personTagDao.observeTagsForPerson(personId)

    /** Finds an existing tag by name/type or creates it. */
    suspend fun getOrCreateTag(name: String, isPlace: Boolean): Tag {
        val trimmed = name.trim()
        tagDao.findByName(trimmed, isPlace)?.let { return it }
        val id = tagDao.insert(Tag(name = trimmed, isPlace = isPlace))
        return Tag(id = id, name = trimmed, isPlace = isPlace)
    }

    suspend fun attachGenericTag(personId: Long, tagId: Long) {
        personTagDao.insert(PersonTag(personId = personId, tagId = tagId))
    }

    suspend fun detachGenericTag(personTag: PersonTag) = personTagDao.delete(personTag)

    suspend fun deleteTagIfUnused(tag: Tag) {
        val generic = tagDao.genericUsageCount(tag.id)
        val place = tagDao.placeUsageCount(tag.id)
        if (generic == 0 && place == 0) {
            tagDao.delete(tag)
        }
    }

    // --- Places (person <-> place tag with year range) ---
    fun observePlacesForPerson(personId: Long): Flow<List<PersonPlaceDetail>> = personPlaceDao.observePlacesForPerson(personId)

    suspend fun addPlaceToPerson(personId: Long, placeName: String, fromYear: Int?, toYear: Int?) {
        val tag = getOrCreateTag(placeName, isPlace = true)
        personPlaceDao.insert(PersonPlace(personId = personId, placeTagId = tag.id, fromYear = fromYear, toYear = toYear))
    }

    suspend fun updatePersonPlace(personPlace: PersonPlace) = personPlaceDao.update(personPlace)

    suspend fun removePersonPlace(id: Long) {
        val pp = personPlaceDao.getById(id) ?: return
        personPlaceDao.delete(pp)
        val tag = Tag(id = pp.placeTagId, name = "", isPlace = true)
        deleteTagIfUnused(tag)
    }

    // --- Relationships ---
    fun observeRelationshipsForPerson(personId: Long): Flow<List<RelationshipDetail>> = relationshipDao.observeForPerson(personId)

    /**
     * Adds a relationship and its automatic reciprocal (e.g. "Son" on one side creates
     * "Father"/"Mother"/"Parent" on the other, based on [role]). [label] is the fallback text
     * stored on the forward row — used verbatim only when [role] is null (a custom relationship).
     * [impliedGenderForRelated] (if non-null) sets the related person's gender when it's unset
     * (e.g. picking "Son" implies male). [ownGenderIfProvided] (if non-null) sets this person's
     * own gender when it's unset — needed so the reciprocal side reads correctly.
     */
    suspend fun addRelationship(
        personId: Long,
        relatedPersonId: Long,
        role: RelationshipRole?,
        label: String,
        impliedGenderForRelated: String? = null,
        ownGenderIfProvided: String? = null
    ) {
        if (impliedGenderForRelated != null) {
            personDao.getById(relatedPersonId)?.let { related ->
                if (related.gender == null) personDao.update(related.copy(gender = impliedGenderForRelated))
            }
        }
        if (ownGenderIfProvided != null) {
            personDao.getById(personId)?.let { me ->
                if (me.gender == null) personDao.update(me.copy(gender = ownGenderIfProvided))
            }
        }

        relationshipDao.insert(Relationship(personId = personId, relatedPersonId = relatedPersonId, type = label, role = role?.name))
        val reciprocalRole = role?.reciprocal()
        val reciprocalLabel = reciprocalRole?.genericLabel ?: label
        relationshipDao.insert(
            Relationship(personId = relatedPersonId, relatedPersonId = personId, type = reciprocalLabel, role = reciprocalRole?.name)
        )
    }

    /** Deletes a relationship row and its mirrored row on the other person's side, if present. */
    suspend fun deleteRelationship(relationshipId: Long) {
        val r = relationshipDao.getById(relationshipId) ?: return
        relationshipDao.delete(r)
        relationshipDao.findReciprocal(r.personId, r.relatedPersonId)?.let { relationshipDao.delete(it) }
    }

    // --- Notes ---
    fun observeNotesForPerson(personId: Long): Flow<List<Note>> = noteDao.observeForPerson(personId)
    suspend fun addNote(personId: Long, text: String, eventAt: Long? = null) =
        noteDao.insert(Note(personId = personId, text = text, eventAt = eventAt))
    suspend fun updateNote(note: Note) = noteDao.update(note)
    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    // --- Photos ---
    fun observePhotosForPerson(personId: Long): Flow<List<Photo>> = photoDao.observeForPerson(personId)
    suspend fun addPhoto(personId: Long, filePath: String): Long = photoDao.insert(Photo(personId = personId, filePath = filePath))
    suspend fun deletePhoto(photo: Photo) = photoDao.delete(photo)
}
