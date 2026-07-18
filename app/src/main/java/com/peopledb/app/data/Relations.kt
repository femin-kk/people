package com.peopledb.app.data

// A place tag together with the from/to years for a specific person.
data class PersonPlaceDetail(
    val personPlaceId: Long,
    val tagId: Long,
    val placeName: String,
    val fromYear: Int?,
    val toYear: Int?
)

// A relationship together with the display name of the related person.
data class RelationshipDetail(
    val relationshipId: Long,
    val relatedPersonId: Long,
    val relatedPersonName: String,
    val relatedPersonPhotoPath: String?,
    val type: String
)

// A tag with a count of how many people are attached to it (via person_tags or person_places).
data class TagWithCount(
    val id: Long,
    val name: String,
    val isPlace: Boolean,
    val personCount: Int
)

data class PersonSummary(
    val id: Long,
    val name: String,
    val birthdayEpochDay: Long?,
    val birthdayYearKnown: Boolean,
    val primaryPhotoPath: String?
)
