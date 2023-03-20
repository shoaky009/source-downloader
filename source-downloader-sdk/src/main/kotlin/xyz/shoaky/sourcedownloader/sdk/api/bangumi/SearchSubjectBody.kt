package xyz.shoaky.sourcedownloader.sdk.api.bangumi

class SearchSubjectBody(
    val results: Int,
    val list: List<SubjectItem>
) : Iterable<SubjectItem> {
    override fun iterator(): Iterator<SubjectItem> {
        return list.iterator()
    }
}