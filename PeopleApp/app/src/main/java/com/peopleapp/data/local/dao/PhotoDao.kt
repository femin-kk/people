package com.peopleapp.data.local.dao

import androidx.room.*
import com.peopleapp.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE personId = :personId AND isDeleted = 0 ORDER BY isProfilePhoto DESC, createdAt DESC")
    fun getPhotosForPerson(personId: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE personId = :personId AND isProfilePhoto = 1 AND isDeleted = 0 LIMIT 1")
    suspend fun getProfilePhoto(personId: String): PhotoEntity?

    @Query("SELECT * FROM photos WHERE personId = :personId AND isProfilePhoto = 1 AND isDeleted = 0 LIMIT 1")
    fun getProfilePhotoFlow(personId: String): Flow<PhotoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Query("UPDATE photos SET isProfilePhoto = 0 WHERE personId = :personId")
    suspend fun clearProfilePhoto(personId: String)

    @Query("UPDATE photos SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeletePhoto(id: String)

    @Query("SELECT * FROM photos WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedPhotos(): List<PhotoEntity>
}
