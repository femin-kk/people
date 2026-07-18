package com.peopledb.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getById(id: Long): Person?

    @Query("SELECT * FROM people WHERE id = :id")
    fun observeById(id: Long): Flow<Person?>

    @Query("SELECT id, name, birthdayEpochDay, birthdayYearKnown, primaryPhotoPath FROM people ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PersonSummary>>

    @Query("""
        SELECT id, name, birthdayEpochDay, birthdayYearKnown, primaryPhotoPath FROM people
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun searchByName(query: String): Flow<List<PersonSummary>>

    @Query("""
        SELECT DISTINCT p.id, p.name, p.birthdayEpochDay, p.birthdayYearKnown, p.primaryPhotoPath
        FROM people p
        INNER JOIN person_tags pt ON pt.personId = p.id
        WHERE pt.tagId = :tagId
        ORDER BY p.name COLLATE NOCASE ASC
    """)
    fun getPeopleByTag(tagId: Long): Flow<List<PersonSummary>>

    @Query("""
        SELECT DISTINCT p.id, p.name, p.birthdayEpochDay, p.birthdayYearKnown, p.primaryPhotoPath
        FROM people p
        INNER JOIN person_places pp ON pp.personId = p.id
        WHERE pp.placeTagId = :tagId
        ORDER BY p.name COLLATE NOCASE ASC
    """)
    fun getPeopleByPlaceTag(tagId: Long): Flow<List<PersonSummary>>
}

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Query("SELECT * FROM tags WHERE name = :name AND isPlace = :isPlace LIMIT 1")
    suspend fun findByName(name: String, isPlace: Boolean): Tag?

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Tag>>

    @Query("""
        SELECT t.id as id, t.name as name, t.isPlace as isPlace,
        (
          CASE WHEN t.isPlace = 1
            THEN (SELECT COUNT(DISTINCT personId) FROM person_places WHERE placeTagId = t.id)
            ELSE (SELECT COUNT(DISTINCT personId) FROM person_tags WHERE tagId = t.id)
          END
        ) as personCount
        FROM tags t
        WHERE t.isPlace = :isPlace
        ORDER BY t.name COLLATE NOCASE ASC
    """)
    fun observeTagsWithCount(isPlace: Boolean): Flow<List<TagWithCount>>

    @Query("""
        SELECT t.id as id, t.name as name, t.isPlace as isPlace,
        (
          CASE WHEN t.isPlace = 1
            THEN (SELECT COUNT(DISTINCT personId) FROM person_places WHERE placeTagId = t.id)
            ELSE (SELECT COUNT(DISTINCT personId) FROM person_tags WHERE tagId = t.id)
          END
        ) as personCount
        FROM tags t
        WHERE t.name LIKE '%' || :query || '%'
        ORDER BY t.isPlace DESC, t.name COLLATE NOCASE ASC
    """)
    fun searchTagsWithCount(query: String): Flow<List<TagWithCount>>

    @Query("SELECT COUNT(*) FROM person_tags WHERE tagId = :tagId")
    suspend fun genericUsageCount(tagId: Long): Int

    @Query("SELECT COUNT(*) FROM person_places WHERE placeTagId = :tagId")
    suspend fun placeUsageCount(tagId: Long): Int
}

@Dao
interface PersonTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(personTag: PersonTag): Long

    @Delete
    suspend fun delete(personTag: PersonTag)

    @Query("SELECT * FROM person_tags WHERE personId = :personId AND tagId = :tagId LIMIT 1")
    suspend fun find(personId: Long, tagId: Long): PersonTag?

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN person_tags pt ON pt.tagId = t.id
        WHERE pt.personId = :personId
        ORDER BY t.name COLLATE NOCASE ASC
    """)
    fun observeTagsForPerson(personId: Long): Flow<List<Tag>>
}

@Dao
interface PersonPlaceDao {
    @Insert
    suspend fun insert(personPlace: PersonPlace): Long

    @Update
    suspend fun update(personPlace: PersonPlace)

    @Delete
    suspend fun delete(personPlace: PersonPlace)

    @Query("""
        SELECT pp.id as personPlaceId, t.id as tagId, t.name as placeName, pp.fromYear as fromYear, pp.toYear as toYear
        FROM person_places pp
        INNER JOIN tags t ON t.id = pp.placeTagId
        WHERE pp.personId = :personId
        ORDER BY pp.fromYear DESC
    """)
    fun observePlacesForPerson(personId: Long): Flow<List<PersonPlaceDetail>>

    @Query("SELECT * FROM person_places WHERE id = :id")
    suspend fun getById(id: Long): PersonPlace?
}

@Dao
interface RelationshipDao {
    @Insert
    suspend fun insert(relationship: Relationship): Long

    @Update
    suspend fun update(relationship: Relationship)

    @Delete
    suspend fun delete(relationship: Relationship)

    @Query("""
        SELECT r.id as relationshipId, p.id as relatedPersonId, p.name as relatedPersonName,
               p.primaryPhotoPath as relatedPersonPhotoPath, r.type as type
        FROM relationships r
        INNER JOIN people p ON p.id = r.relatedPersonId
        WHERE r.personId = :personId
        ORDER BY p.name COLLATE NOCASE ASC
    """)
    fun observeForPerson(personId: Long): Flow<List<RelationshipDetail>>

    @Query("SELECT * FROM relationships WHERE id = :id")
    suspend fun getById(id: Long): Relationship?
}

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE personId = :personId ORDER BY COALESCE(eventAt, createdAt) DESC")
    fun observeForPerson(personId: Long): Flow<List<Note>>
}

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: Photo): Long

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM photos WHERE personId = :personId ORDER BY addedAt DESC")
    fun observeForPerson(personId: Long): Flow<List<Photo>>
}
