package com.amirnourhan.goldbar.core

object PersianNumber {
    private val fa = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    fun normalize(input: String): String = input
        .replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9')
        .replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4').replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9')
        .replace("٬", "").replace(",", "").trim()
    fun parse(input: String): Double = normalize(input).toDoubleOrNull() ?: Double.NaN
    fun format(value: Double, digits: Int = 2): String {
        if (!value.isFinite()) return "—"
        val raw = java.lang.String.format(java.util.Locale.US, "%.${digits}f", value).trimEnd('0').trimEnd('.')
        return raw.map { if (it in '0'..'9') fa[it - '0'] else it }.joinToString("")
    }
}
