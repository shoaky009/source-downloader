package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

@Disabled("第三方API")
class BangumiTest {

    private val client = BangumiApiClient

    @Test
    fun searchSubject() {
        val request = SearchSubjectRequest("机动战士高达 水星的魔女")
        val execute = client.execute(request)
        println(Jackson.toJsonString(execute.body()))
    }
}