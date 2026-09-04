package com.amirnourhan.goldbar.data

import android.content.Context
import com.amirnourhan.goldbar.core.*
import org.json.JSONArray
import org.json.JSONObject

class GoldBarStore(context: Context) {
    private val prefs = context.getSharedPreferences("goldbar_state", Context.MODE_PRIVATE)
    fun load(): GoldBarState = prefs.getString("state", null)?.let(::decode) ?: GoldBarState()
    fun save(state: GoldBarState) = prefs.edit().putString("state", encode(state)).apply()
    fun replaceFromReport(state: GoldBarState) = save(state)
    private fun encode(s: GoldBarState) = JSONObject().apply {
        put("entries", JSONArray(s.entries.map { JSONObject().put("id", it.id).put("weight", it.weight).put("assay", it.assay).put("description", it.description).put("createdAt", it.createdAt) }))
        put("increaseAssay", section(s.increaseAssay)); put("assay", section(s.assay)); put("quickCalculation", section(s.quickCalculation)); put("assayCost", section(s.assayCost))
    }.toString()
    private fun decode(text: String): GoldBarState = JSONObject(text).let { o -> GoldBarState(entries = entries(o.optJSONArray("entries") ?: JSONArray()), increaseAssay = section("افزایش عیار", o.optJSONObject("increaseAssay")), assay = section("عیار", o.optJSONObject("assay")), quickCalculation = section("محاسبه سریع", o.optJSONObject("quickCalculation")), assayCost = section("هزینه عیار", o.optJSONObject("assayCost"))) }
    private fun entries(a: JSONArray) = (0 until a.length()).map { i -> a.getJSONObject(i).let { MeltEntry(it.optString("id"), it.optDouble("weight"), it.optDouble("assay"), it.optString("description"), it.optString("createdAt")) } }
    private fun section(s: ReportSection) = JSONObject().put("title", s.title).put("fields", JSONArray(s.fields.map { JSONObject().put("label", it.label).put("value", it.value).put("unit", it.unit) }))
    private fun section(title: String, obj: JSONObject?): ReportSection { val a = obj?.optJSONArray("fields") ?: JSONArray(); return ReportSection(title, (0 until a.length()).map { i -> a.getJSONObject(i).let { SectionField(it.optString("label"), it.optString("value"), it.optString("unit")) } }) }
}
