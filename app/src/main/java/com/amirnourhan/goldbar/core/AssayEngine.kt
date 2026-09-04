package com.amirnourhan.goldbar.core

import kotlin.math.pow
import kotlin.math.truncate

object AssayEngine {
    fun summarize(entries: Iterable<MeltEntry>): AssaySummary {
        var weight = 0.0
        var weighted = 0.0
        var count = 0
        for (item in entries) {
            if (item.weight <= 0.0 || item.assay <= 0.0 || item.assay > 1000.0) continue
            weight += item.weight
            weighted += item.weight * item.assay
            count++
        }
        return AssaySummary(weight, weighted, if (weight > 0.0) weighted / weight else Double.NaN, count)
    }
    fun increase(summary: AssaySummary, targetAssay: Double, barAssay: Double): IncreaseAssayResult {
        val difference = targetAssay - summary.averageAssay
        val denominator = barAssay - targetAssay
        val raw = if (denominator == 0.0) Double.NaN else summary.weight * difference / denominator
        return IncreaseAssayResult(difference, denominator, if (raw.isFinite()) roundDownTowardZero(raw, 1) else Double.NaN)
    }
    fun alloy(summary: AssaySummary, targetAssay: Double, silverPercent: Double, globalWeight: Double): AlloyResult {
        if (summary.weight <= 0.0 || !summary.averageAssay.isFinite() || targetAssay == 0.0) return AlloyResult(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN)
        val total = summary.weight * summary.averageAssay / targetAssay - summary.weight
        val silver = silverPercent / 100.0 * total
        val nonSilver = total - silver
        val four = globalWeight * 0.004
        return AlloyResult(total, silver, nonSilver, four, total - silver - four, summary.weight + total)
    }
    fun split(value: Double, percent995: Double = 36.79, percent750: Double = 63.21): Pair<Double, Double> = value * percent995 / 100.0 to value * percent750 / 100.0
    fun correctionForDrop(baseWeight: Double, baseAssay: Double, assayDrop: Double): Double = if (baseAssay == assayDrop) Double.NaN else baseWeight * baseAssay / (baseAssay - assayDrop) - baseWeight
    fun roundDownTowardZero(value: Double, digits: Int): Double { val factor = 10.0.pow(digits); return truncate(value * factor) / factor }
}
