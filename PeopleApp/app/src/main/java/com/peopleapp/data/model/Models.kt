package com.peopleapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
data class Person(
    val id: String,
    val name: String,
    val nickname: String = "",
    val email: String = "",
    val phone: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) : Parcelable

@Parcelize
data class Fact(
    val id: String,
    val personId: String,
    val category: String,
    val label: String,
    val value: String,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class Event(
    val id: String,
    val personId: String,
    val type: String,
    val label: String,
    val date: Long,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    fun relativeTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - date
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val years = days / 365
        val months = days / 30
        val weeks = days / 7

        return when {
            diff < 0 -> {
                val futureDays = TimeUnit.MILLISECONDS.toDays(-diff)
                val futureYears = futureDays / 365
                val futureMonths = futureDays / 30
                when {
                    futureYears > 0 -> "in $futureYears year${if (futureYears > 1) "s" else ""}"
                    futureMonths > 0 -> "in $futureMonths month${if (futureMonths > 1) "s" else ""}"
                    futureDays > 0 -> "in $futureDays day${if (futureDays > 1) "s" else ""}"
                    else -> "today"
                }
            }
            days == 0L -> "today"
            days == 1L -> "yesterday"
            days < 7 -> "$days days ago"
            weeks < 5 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
            months < 12 -> "$months month${if (months > 1) "s" else ""} ago"
            years == 1L -> "1 year ago"
            else -> "$years years ago"
        }
    }
}

@Parcelize
data class Relationship(
    val id: String,
    val fromPersonId: String,
    val toPersonId: String,
    val type: String,
    val label: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class Photo(
    val id: String,
    val personId: String,
    val localPath: String,
    val isProfilePhoto: Boolean = false,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

data class PersonDetail(
    val person: Person,
    val facts: List<Fact> = emptyList(),
    val events: List<Event> = emptyList(),
    val relationships: List<RelationshipWithPerson> = emptyList(),
    val photos: List<Photo> = emptyList(),
    val profilePhoto: Photo? = null
)

data class RelationshipWithPerson(
    val relationship: Relationship,
    val person: Person
)

data class SearchResult(
    val people: List<Person> = emptyList(),
    val facts: List<Pair<Fact, Person>> = emptyList(),
    val relationships: List<Pair<Relationship, Person>> = emptyList()
)

object FactCategory {
    const val OCCUPATION = "occupation"
    const val HOBBY = "hobby"
    const val WHERE_MET = "where_met"
    const val EDUCATION = "education"
    const val INTEREST = "interest"
    const val CUSTOM = "custom"

    val allCategories = listOf(
        OCCUPATION to "Occupation",
        HOBBY to "Hobby",
        WHERE_MET to "Where We Met",
        EDUCATION to "Education",
        INTEREST to "Interest",
        CUSTOM to "Custom"
    )
}

object EventType {
    const val BIRTHDAY = "birthday"
    const val MET = "met"
    const val LAST_CONTACT = "last_contact"
    const val ANNIVERSARY = "anniversary"
    const val IMPORTANT = "important"
    const val CUSTOM = "custom"

    val allTypes = listOf(
        BIRTHDAY to "Birthday",
        MET to "When We Met",
        LAST_CONTACT to "Last Contact",
        ANNIVERSARY to "Anniversary",
        IMPORTANT to "Important Event",
        CUSTOM to "Custom"
    )
}

object RelationshipType {
    const val FRIEND = "friend"
    const val SPOUSE = "spouse"
    const val PARTNER = "partner"
    const val PARENT = "parent"
    const val CHILD = "child"
    const val SIBLING = "sibling"
    const val COLLEAGUE = "colleague"
    const val MANAGER = "manager"
    const val INTRODUCED_BY = "introduced_by"
    const val MENTOR = "mentor"
    const val ACQUAINTANCE = "acquaintance"
    const val CUSTOM = "custom"

    val allTypes = listOf(
        FRIEND to "Friend",
        SPOUSE to "Spouse",
        PARTNER to "Partner",
        PARENT to "Parent",
        CHILD to "Child",
        SIBLING to "Sibling",
        COLLEAGUE to "Colleague",
        MANAGER to "Manager",
        INTRODUCED_BY to "Introduced By",
        MENTOR to "Mentor",
        ACQUAINTANCE to "Acquaintance",
        CUSTOM to "Custom"
    )
}
