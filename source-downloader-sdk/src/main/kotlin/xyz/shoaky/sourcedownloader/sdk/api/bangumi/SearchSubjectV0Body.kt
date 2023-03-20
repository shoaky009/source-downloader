package xyz.shoaky.sourcedownloader.sdk.api.bangumi

data class SearchSubjectV0Body(
    val total: Int,
    val data: List<SubjectV0Item>
) : Iterable<SubjectV0Item> {

    override fun iterator(): Iterator<SubjectV0Item> {
        return data.iterator()
    }
}