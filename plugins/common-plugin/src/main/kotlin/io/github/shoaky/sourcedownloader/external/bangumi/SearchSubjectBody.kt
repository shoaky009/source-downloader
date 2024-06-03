package io.github.shoaky.sourcedownloader.external.bangumi

data class SearchSubjectBody(
    val results: Int = 0,
    val list: List<SubjectItem> = emptyList()
) : Iterable<SubjectItem> {
    override fun iterator(): Iterator<SubjectItem> {
        return list.iterator()
    }
}