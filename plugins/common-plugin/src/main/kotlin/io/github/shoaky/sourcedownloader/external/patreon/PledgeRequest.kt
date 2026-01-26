package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.JoinStringSerializer

class PledgeRequest(
    @param:JsonSerialize(using = JoinStringSerializer::class)
    val include: List<String> = listOf("campaign"),
    @param:JsonProperty("fields[campaign]")
    @param:JsonSerialize(using = JoinStringSerializer::class)
    val campaignFields: List<String> = listOf("name"),
    @param:JsonProperty("fields[reward]")
    @param:JsonSerialize(using = JoinStringSerializer::class)
    val rewardFields: List<String> = listOf("id"),
) : PatreonRequest<PledgeResponse>() {

    override val path: String = "/api/pledges"
    override val responseBodyType: TypeReference<PledgeResponse> = jacksonTypeRef()

}

data class PledgeResponse(
    val data: List<Pledge> = emptyList(),
) {

    fun campaignIds(): List<Long> {
        return data.mapNotNull { it.relationships["campaign"]?.data?.id }
    }
}

data class Pledge(
    val id: String,
    val type: String,
    val attributes: PledgeAttrs,
    val relationships: Map<String, Shit>,
)

data class PledgeAttrs(
    @param:JsonProperty("amount_cents")
    val amountCents: Int,
    val currency: String,
)

data class Shit(
    val data: Campaign
)

data class Campaign(
    val id: Long,
    val type: String,
)