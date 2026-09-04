package com.amirnourhan.goldbar.data

import com.amirnourhan.goldbar.core.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ReportXlsx {
    fun save(state: GoldBarState, out: OutputStream) = ZipOutputStream(out).use { z ->
        z.writeText("[Content_Types].xml", contentTypes())
        z.writeText("_rels/.rels", rootRels())
        z.writeText("xl/workbook.xml", workbook())
        z.writeText("xl/_rels/workbook.xml.rels", workbookRels())
        z.writeText("xl/styles.xml", styles())
        z.writeText("xl/worksheets/sheet1.xml", melts(state.entries))
        z.writeText("xl/worksheets/sheet2.xml", section("افزایش عیار", state.increaseAssay))
        z.writeText("xl/worksheets/sheet3.xml", section("عیار", state.assay))
        z.writeText("xl/worksheets/sheet4.xml", section("محاسبه سریع", state.quickCalculation))
        z.writeText("xl/worksheets/sheet5.xml", section("هزینه عیار", state.assayCost))
    }

    fun load(input: InputStream): GoldBarState {
        val sheets = mutableMapOf<String, String>()
        ZipInputStream(input).use { z ->
            while (true) { val e = z.nextEntry ?: break; if (e.name.startsWith("xl/worksheets/sheet")) sheets[e.name] = z.readBytes().decodeToString() }
        }
        return GoldBarState(parseMelts(sheets["xl/worksheets/sheet1.xml"].orEmpty()), parseSection("افزایش عیار", sheets["xl/worksheets/sheet2.xml"].orEmpty()), parseSection("عیار", sheets["xl/worksheets/sheet3.xml"].orEmpty()), parseSection("محاسبه سریع", sheets["xl/worksheets/sheet4.xml"].orEmpty()), parseSection("هزینه عیار", sheets["xl/worksheets/sheet5.xml"].orEmpty()))
    }

    private fun parseMelts(xml: String) = rows(xml).drop(2).mapNotNull { c ->
        val w = c.getOrNull(1)?.toDoubleOrNull() ?: return@mapNotNull null
        val a = c.getOrNull(2)?.toDoubleOrNull() ?: return@mapNotNull null
        MeltEntry(weight = w, assay = a, description = c.getOrNull(3).orEmpty(), createdAt = c.getOrNull(4).orEmpty())
    }
    private fun parseSection(title: String, xml: String) = ReportSection(title, rows(xml).drop(2).mapNotNull { c -> if (c.getOrNull(0).isNullOrBlank() && c.getOrNull(1).isNullOrBlank()) null else SectionField(c.getOrNull(0).orEmpty(), c.getOrNull(1).orEmpty(), c.getOrNull(2).orEmpty()) })
    private fun rows(xml: String): List<List<String>> {
        if (xml.isBlank()) return emptyList()
        val p = XmlPullParserFactory.newInstance().newPullParser(); p.setInput(xml.reader())
        val all = mutableListOf<MutableList<String>>(); var row: MutableList<String>? = null; var capture = false; var text = ""
        while (p.eventType != XmlPullParser.END_DOCUMENT) {
            when (p.eventType) { XmlPullParser.START_TAG -> when (p.name) { "row" -> row = mutableListOf(); "v", "t" -> { capture = true; text = "" } }; XmlPullParser.TEXT -> if (capture) text += p.text; XmlPullParser.END_TAG -> when (p.name) { "v", "t" -> { row?.add(text); capture = false }; "row" -> { row?.let(all::add); row = null } } }
            p.next()
        }
        return all
    }
    private fun ZipOutputStream.writeText(path: String, content: String) { putNextEntry(ZipEntry(path)); write(content.toByteArray(Charsets.UTF_8)); closeEntry() }
    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    private fun t(ref: String, v: String, style: Int = 0) = "<c r=\"$ref\" t=\"inlineStr\" s=\"$style\"><is><t xml:space=\"preserve\">${esc(v)}</t></is></c>"
    private fun n(ref: String, v: Double) = "<c r=\"$ref\"><v>$v</v></c>"
    private fun start() = "<?xml version=\"1.0\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetViews><sheetView workbookViewId=\"0\" rightToLeft=\"1\"/></sheetViews><sheetData>"
    private fun melts(items: List<MeltEntry>): String = buildString { append(start()); append("<row r=\"1\">${t("A1","GOLD BAR - گزارش آبشده‌ها",2)}</row><row r=\"4\">${t("A4","ردیف",1)}${t("B4","وزن (g)",1)}${t("C4","عیار (‰)",1)}${t("D4","توضیحات",1)}${t("E4","تاریخ ثبت",1)}</row>"); items.forEachIndexed { i, e -> val r=i+5; append("<row r=\"$r\">${n("A$r",(i+1).toDouble())}${n("B$r",e.weight)}${n("C$r",e.assay)}${t("D$r",e.description)}${t("E$r",e.createdAt)}</row>") }; append("</sheetData></worksheet>") }
    private fun section(title: String, s: ReportSection): String = buildString { append(start()); append("<row r=\"1\">${t("A1","GOLD BAR - $title",2)}</row><row r=\"4\">${t("A4","عنوان",1)}${t("B4","مقدار",1)}${t("C4","واحد",1)}</row>"); s.fields.forEachIndexed { i, f -> val r=i+5; append("<row r=\"$r\">${t("A$r",f.label)}${t("B$r",f.value)}${t("C$r",f.unit)}</row>") }; append("</sheetData></worksheet>") }
    private fun contentTypes() = """<?xml version="1.0"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet5.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>"""
    private fun rootRels() = """<?xml version="1.0"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""
    private fun workbook() = """<?xml version="1.0"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="آبشده‌ها" sheetId="1" r:id="rId1"/><sheet name="افزایش عیار" sheetId="2" r:id="rId2"/><sheet name="عیار" sheetId="3" r:id="rId3"/><sheet name="محاسبه سریع" sheetId="4" r:id="rId4"/><sheet name="هزینه عیار" sheetId="5" r:id="rId5"/></sheets></workbook>"""
    private fun workbookRels() = """<?xml version="1.0"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/><Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/><Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/><Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet5.xml"/><Relationship Id="rId6" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>"""
    private fun styles() = """<?xml version="1.0"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="1"><font><sz val="11"/></font></fonts><fills count="1"><fill><patternFill patternType="none"/></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs></styleSheet>"""
}
