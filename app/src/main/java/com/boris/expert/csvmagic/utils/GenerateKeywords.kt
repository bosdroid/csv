package com.boris.expert.csvmagic.utils

import androidx.annotation.Nullable
import com.google.ads.googleads.lib.GoogleAdsClient
import com.google.ads.googleads.v10.enums.KeywordPlanNetworkEnum
import com.google.ads.googleads.v10.services.GenerateKeywordIdeasRequest
import com.google.ads.googleads.v10.services.KeywordPlanIdeaServiceClient
import com.google.ads.googleads.v10.utils.ResourceNames


object GenerateKeywords {

    fun runExample(
        googleAdsClient: GoogleAdsClient,
        customerId: Long,
        languageId: Long,
        locationIds: List<Long>,
        keywords: List<String>,
        @Nullable pageUrl: String?
    ) {
        googleAdsClient.latestVersion.createKeywordPlanIdeaServiceClient()
            .use { keywordPlanServiceClient ->
                val requestBuilder: GenerateKeywordIdeasRequest.Builder =
                    GenerateKeywordIdeasRequest.newBuilder()
                        .setCustomerId(customerId.toString()) // Sets the language resource using the provided language ID.
                        .setLanguage(ResourceNames.languageConstant(languageId)) // Sets the network. To restrict to only Google Search, change the parameter below to
                        // KeywordPlanNetwork.GOOGLE_SEARCH.
                        .setKeywordPlanNetwork(KeywordPlanNetworkEnum.KeywordPlanNetwork.GOOGLE_SEARCH_AND_PARTNERS)

                // Adds the resource name of each location ID to the request.
                for (locationId in locationIds) {
                    requestBuilder.addGeoTargetConstants(ResourceNames.geoTargetConstant(locationId))
                }

                // Makes sure that keywords and/or page URL were specified. The request must have exactly one
                // of urlSeed, keywordSeed, or keywordAndUrlSeed set.
                require(!(keywords.isEmpty() && pageUrl == null)) { "At least one of keywords or page URL is required, but neither was specified." }
                if (keywords.isEmpty()) {
                    // Only page URL was specified, so use a UrlSeed.
                    requestBuilder.getUrlSeedBuilder().setUrl(pageUrl)
                } else if (pageUrl == null) {
                    // Only keywords were specified, so use a KeywordSeed.
                    requestBuilder.getKeywordSeedBuilder().addAllKeywords(keywords)
                } else {
                    // Both page URL and keywords were specified, so use a KeywordAndUrlSeed.
                    requestBuilder.getKeywordAndUrlSeedBuilder().setUrl(pageUrl)
                        .addAllKeywords(keywords)
                }

                // Sends the keyword ideas request.
                val response: KeywordPlanIdeaServiceClient.GenerateKeywordIdeasPagedResponse =
                    keywordPlanServiceClient.generateKeywordIdeas(requestBuilder.build())
                // Prints each result in the response.
                for (result in response.iterateAll()) {
                    System.out.printf(
                        "Keyword idea text '%s' has %d average monthly searches and '%s' competition.%n",
                        result.getText(),
                        result.getKeywordIdeaMetrics().getAvgMonthlySearches(),
                        result.getKeywordIdeaMetrics().getCompetition()
                    )
                }
            }
    }

}