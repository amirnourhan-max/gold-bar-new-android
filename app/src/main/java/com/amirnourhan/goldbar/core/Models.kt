package com.amirnourhan.goldbar.core

import java.util.UUID

data class MeltEntry(val id: String = UUID.randomUUID().toString(), val weight: Double, val assay: Double, val description: String = "", val createdAt: String = "")
data class AssaySummary(val weight: Double, val weightedSum: Double, val averageAssay: Double, val count: Int)
data class IncreaseAssayResult(val assayDifference: Double, val denominator: Double, val requiredBar: Double)
data class AlloyResult(val totalAlloyRequired: Double, val silverRequired: Double, val nonSilverRequired: Double, val fourPerThousand: Double, val finalOtherAlloy: Double, val totalAfterAlloy: Double)
data class SectionField(val label: String, val value: String, val unit: String = "")
data class ReportSection(val title: String, val fields: List<SectionField> = emptyList())
data class GoldBarState(
    val entries: List<MeltEntry> = emptyList(),
    val increaseAssay: ReportSection = ReportSection("افزایش عیار"),
    val assay: ReportSection = ReportSection("عیار"),
    val quickCalculation: ReportSection = ReportSection("محاسبه سریع"),
    val assayCost: ReportSection = ReportSection("هزینه عیار")
)
