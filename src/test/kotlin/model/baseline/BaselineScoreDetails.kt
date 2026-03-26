package model.baseline

import kotlinx.serialization.Serializable
import model.healthdata.Range

@Serializable
data class BaselineScoreDetailResponse(
    val data: BaseLineScoreData?=null,
    val message: String?=null,
    val status: String?=null
)

@Serializable
data class BaseLineScoreData(
    val age: Int?=null,
    val biological_age: Int?=null,
    val contributors: Contributors?=null,
    val impact_analysis: List<ImpactAnalysi>?=null,
    val score_details: ScoreDetails?=null,
    val trend_history: List<TrendHistory>?=null
)

@Serializable
data class Contributors(
    val negative: List<Negative>?=null,
    val positive: List<Positive>?=null
)

@Serializable
data class ImpactAnalysi(
    val current_value: Double?=null,
    val display_name: String?=null,
    val impact_score: Double?=null,
    val inference: String?=null,
    val metric_id: String?=null,
    val next_target_range: String?=null,
    val rank: Int?=null,
    val rating_rank: Int?=null,
    val target_rating_rank: Int?=null,
    val tier: String?=null,
    val unit: String?=null,
    val weight: Int?=null
)


@Serializable
data class ScoreDetails(
    val baseline_score_description: String?=null,
    val inference: String?=null,
    val normalized_baseline_score: Double?=null,
    val original_baseline_score: Double?=null,
    val ranges: List<Range>?=null
)

@Serializable
data class TrendHistory(
    val calculated_at: String?=null,
    val value: Double?=null
)

@Serializable
data class Positive(
    val current_value: Double?=null,
    val display_name: String?=null,
    val inference: String?=null,
    val metric_id: String?=null,
    val rank: Int?=null,
    val rating_rank: Int?=null,
    val tier_label: String?=null,
    val unit: String?=null
)

@Serializable
data class Negative(
    val current_value: Double?=null,
    val display_name: String?=null,
    val inference: String?=null,
    val metric_id: String?=null,
    val rank: Int?=null,
    val rating_rank: Int?=null,
    val tier_label: String?=null,
    val unit: String?=null
)

