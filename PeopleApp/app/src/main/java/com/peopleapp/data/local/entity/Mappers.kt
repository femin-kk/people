package com.peopleapp.data.local.entity

import com.peopleapp.data.model.*

fun PersonEntity.toDomain() = Person(
    id = id,
    name = name,
    nickname = nickname,
    email = email,
    phone = phone,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced
)

fun Person.toEntity() = PersonEntity(
    id = id,
    name = name,
    nickname = nickname,
    email = email,
    phone = phone,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced
)

fun FactEntity.toDomain() = Fact(
    id = id,
    personId = personId,
    category = category,
    label = label,
    value = value,
    createdAt = createdAt
)

fun Fact.toEntity() = FactEntity(
    id = id,
    personId = personId,
    category = category,
    label = label,
    value = value,
    createdAt = createdAt
)

fun EventEntity.toDomain() = Event(
    id = id,
    personId = personId,
    type = type,
    label = label,
    date = date,
    notes = notes,
    createdAt = createdAt
)

fun Event.toEntity() = EventEntity(
    id = id,
    personId = personId,
    type = type,
    label = label,
    date = date,
    notes = notes,
    createdAt = createdAt
)

fun RelationshipEntity.toDomain() = Relationship(
    id = id,
    fromPersonId = fromPersonId,
    toPersonId = toPersonId,
    type = type,
    label = label,
    notes = notes,
    createdAt = createdAt
)

fun Relationship.toEntity() = RelationshipEntity(
    id = id,
    fromPersonId = fromPersonId,
    toPersonId = toPersonId,
    type = type,
    label = label,
    notes = notes,
    createdAt = createdAt
)

fun PhotoEntity.toDomain() = Photo(
    id = id,
    personId = personId,
    localPath = localPath,
    isProfilePhoto = isProfilePhoto,
    caption = caption,
    createdAt = createdAt
)

fun Photo.toEntity() = PhotoEntity(
    id = id,
    personId = personId,
    localPath = localPath,
    isProfilePhoto = isProfilePhoto,
    caption = caption,
    createdAt = createdAt
)
