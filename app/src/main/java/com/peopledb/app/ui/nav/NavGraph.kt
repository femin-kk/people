package com.peopledb.app.ui.nav

object Routes {
    const val PEOPLE_LIST = "people_list"
    const val TAGS = "tags"
    const val SETTINGS = "settings"

    const val ADD_PERSON = "add_person"
    const val EDIT_PERSON = "edit_person/{personId}"
    fun editPerson(personId: Long) = "edit_person/$personId"

    const val PERSON_DETAIL = "person_detail/{personId}"
    fun personDetail(personId: Long) = "person_detail/$personId"

    const val TAG_PEOPLE = "tag_people/{tagId}/{isPlace}/{tagName}"
    fun tagPeople(tagId: Long, isPlace: Boolean, tagName: String): String {
        val encoded = java.net.URLEncoder.encode(tagName, "UTF-8")
        return "tag_people/$tagId/$isPlace/$encoded"
    }
}
