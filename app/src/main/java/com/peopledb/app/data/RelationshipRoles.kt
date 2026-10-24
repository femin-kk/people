package com.peopledb.app.data

/**
 * A structured relationship "kind". Stored on a Relationship row (as [Relationship.role]'s name)
 * instead of a fixed label, so the displayed word (e.g. "Son" vs "Daughter" vs "Child") can be
 * recomputed at display time from the *related* person's current gender. That's what makes
 * labels auto-update when someone's gender is changed later - nothing needs to be rewritten.
 */
enum class RelationshipRole(val maleLabel: String, val femaleLabel: String, val genericLabel: String) {
    CHILD("Son", "Daughter", "Child"),
    PARENT("Father", "Mother", "Parent"),
    SIBLING("Brother", "Sister", "Sibling"),
    GRANDCHILD("Grandson", "Granddaughter", "Grandchild"),
    GRANDPARENT("Grandfather", "Grandmother", "Grandparent"),
    NIBLING("Nephew", "Niece", "Niece/Nephew"),
    AUNT_UNCLE("Uncle", "Aunt", "Aunt/Uncle"),
    SPOUSE("Husband", "Wife", "Spouse"),
    COUSIN("Cousin", "Cousin", "Cousin"),
    PARTNER("Partner", "Partner", "Partner"),
    FRIEND("Friend", "Friend", "Friend"),
    COLLEAGUE("Colleague", "Colleague", "Colleague"),
    NEIGHBOR("Neighbour", "Neighbour", "Neighbour"),
    // Ungendered but asymmetric: picking "Junior" on one side shows "Senior" on the other.
    JUNIOR("Junior", "Junior", "Junior"),
    SENIOR("Senior", "Senior", "Senior");

    /** The role the *other* person holds, looking back at this one (e.g. CHILD <-> PARENT). */
    fun reciprocal(): RelationshipRole = when (this) {
        CHILD -> PARENT
        PARENT -> CHILD
        SIBLING -> SIBLING
        GRANDCHILD -> GRANDPARENT
        GRANDPARENT -> GRANDCHILD
        NIBLING -> AUNT_UNCLE
        AUNT_UNCLE -> NIBLING
        SPOUSE -> SPOUSE
        COUSIN -> COUSIN
        PARTNER -> PARTNER
        FRIEND -> FRIEND
        COLLEAGUE -> COLLEAGUE
        NEIGHBOR -> NEIGHBOR
        JUNIOR -> SENIOR
        SENIOR -> JUNIOR
    }

    /** True if this role actually reads differently depending on gender (vs. e.g. Cousin/Friend). */
    fun isGendered(): Boolean = maleLabel != genericLabel || femaleLabel != genericLabel

    /** The word to display, given the gender ("male"/"female"/null) of the person holding this role. */
    fun labelFor(gender: String?): String = when (gender) {
        "male" -> maleLabel
        "female" -> femaleLabel
        else -> genericLabel
    }
}

/** One tappable option in the relationship picker. */
data class RelationshipTemplate(
    val label: String,
    val role: RelationshipRole,
    // If non-null, picking this template implies the *related* person's gender (e.g. "Son" -> male).
    val impliesGender: String?
)

object RelationshipTemplates {
    val groups: List<Pair<String, List<RelationshipTemplate>>> = listOf(
        "Parent & child" to listOf(
            RelationshipTemplate("Son", RelationshipRole.CHILD, "male"),
            RelationshipTemplate("Daughter", RelationshipRole.CHILD, "female"),
            RelationshipTemplate("Child", RelationshipRole.CHILD, null),
            RelationshipTemplate("Father", RelationshipRole.PARENT, "male"),
            RelationshipTemplate("Mother", RelationshipRole.PARENT, "female"),
            RelationshipTemplate("Parent", RelationshipRole.PARENT, null)
        ),
        "Siblings" to listOf(
            RelationshipTemplate("Brother", RelationshipRole.SIBLING, "male"),
            RelationshipTemplate("Sister", RelationshipRole.SIBLING, "female"),
            RelationshipTemplate("Sibling", RelationshipRole.SIBLING, null)
        ),
        "Grandparents & grandchildren" to listOf(
            RelationshipTemplate("Grandson", RelationshipRole.GRANDCHILD, "male"),
            RelationshipTemplate("Granddaughter", RelationshipRole.GRANDCHILD, "female"),
            RelationshipTemplate("Grandchild", RelationshipRole.GRANDCHILD, null),
            RelationshipTemplate("Grandfather", RelationshipRole.GRANDPARENT, "male"),
            RelationshipTemplate("Grandmother", RelationshipRole.GRANDPARENT, "female"),
            RelationshipTemplate("Grandparent", RelationshipRole.GRANDPARENT, null)
        ),
        "Aunts, uncles & niblings" to listOf(
            RelationshipTemplate("Uncle", RelationshipRole.AUNT_UNCLE, "male"),
            RelationshipTemplate("Aunt", RelationshipRole.AUNT_UNCLE, "female"),
            RelationshipTemplate("Nephew", RelationshipRole.NIBLING, "male"),
            RelationshipTemplate("Niece", RelationshipRole.NIBLING, "female")
        ),
        "Partners" to listOf(
            RelationshipTemplate("Husband", RelationshipRole.SPOUSE, "male"),
            RelationshipTemplate("Wife", RelationshipRole.SPOUSE, "female"),
            RelationshipTemplate("Spouse", RelationshipRole.SPOUSE, null),
            RelationshipTemplate("Partner", RelationshipRole.PARTNER, null)
        ),
        "Other" to listOf(
            RelationshipTemplate("Cousin", RelationshipRole.COUSIN, null),
            RelationshipTemplate("Friend", RelationshipRole.FRIEND, null),
            RelationshipTemplate("Colleague", RelationshipRole.COLLEAGUE, null),
            RelationshipTemplate("Neighbour", RelationshipRole.NEIGHBOR, null),
            RelationshipTemplate("Junior", RelationshipRole.JUNIOR, null),
            RelationshipTemplate("Senior", RelationshipRole.SENIOR, null)
        )
    )
}
