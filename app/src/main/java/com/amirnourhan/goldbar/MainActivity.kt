package com.amirnourhan.goldbar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amirnourhan.goldbar.core.*
import com.amirnourhan.goldbar.data.GoldBarStore
import com.amirnourhan.goldbar.data.ReportXlsx
import com.amirnourhan.goldbar.theme.Gold
import com.amirnourhan.goldbar.theme.GoldBarTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() { override fun onCreate(b: Bundle?) { super.onCreate(b); setContent { GoldBarTheme { App() } } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun App() {
    val ctx = LocalContext.current
    val store = remember { GoldBarStore(ctx) }
    var state by remember { mutableStateOf(store.load()) }
    fun persist(s: GoldBarState) { state = s; store.save(s) }
    var tab by remember { mutableStateOf("ثبت آبشده") }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri -> uri?.let { ctx.contentResolver.openOutputStream(it)?.use { out -> ReportXlsx.save(state, out) }; Toast.makeText(ctx, "گزارش ذخیره شد", Toast.LENGTH_SHORT).show() } }
    val load = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { ctx.contentResolver.openInputStream(it)?.use { input -> state = ReportXlsx.load(input); store.replaceFromReport(state) }; Toast.makeText(ctx, "گزارش جایگزین شد", Toast.LENGTH_SHORT).show() } }
    Scaffold(topBar = { TopAppBar(title = { Text("GOLD BAR Android R12", fontWeight = FontWeight.Bold) }, actions = { TextButton({ load.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) }) { Text("ورود گزارش") }; TextButton({ save.launch("GoldBar_Report.xlsx") }) { Text("ذخیره گزارش") } }) }) { p ->
        Column(Modifier.padding(p).fillMaxSize().background(MaterialTheme.colorScheme.background).padding(14.dp), Arrangement.spacedBy(12.dp)) {
            Dashboard(state)
            ScrollableTabRow(selectedTabIndex = tabs.indexOf(tab), edgePadding = 0.dp) { tabs.forEach { Tab(tab == it, { tab = it }, text = { Text(it) }) } }
            when (tab) {
                "ثبت آبشده" -> Melts(state) { persist(state.copy(entries = it)) }
                "افزایش عیار" -> Increase(state) { persist(state.copy(increaseAssay = it)) }
                "عیار" -> Assay(state) { persist(state.copy(assay = it)) }
                "محاسبه سریع" -> Quick(state) { persist(state.copy(quickCalculation = it)) }
                "هزینه عیار" -> Cost(state) { persist(state.copy(assayCost = it)) }
                else -> BoxCard { Text("GOLD BAR Android R12"); Text("تبدیل‌شده از نسخه نهایی Windows R12. بخش ترازو، Serial و تنظیمات COM حذف شده‌اند.") }
            }
        }
    }
}
private val tabs = listOf("ثبت آبشده", "افزایش عیار", "عیار", "محاسبه سریع", "هزینه عیار", "درباره")
@Composable private fun BoxCard(content: @Composable ColumnScope.() -> Unit) = ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content) }
@Composable private fun Input(label: String, v: String, f: (String)->Unit) = OutlinedTextField(v, f, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
@Composable private fun Dashboard(s: GoldBarState) { val sum = AssayEngine.summarize(s.entries); Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { Stat("وزن کل", PersianNumber.format(sum.weight), Modifier.weight(1f)); Stat("عیار میانگین", PersianNumber.format(sum.averageAssay), Modifier.weight(1f)); Stat("تعداد", PersianNumber.format(sum.count.toDouble(),0), Modifier.weight(1f)) } }
@Composable private fun Stat(t: String, v: String, m: Modifier) = ElevatedCard(m) { Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(t); Text(v, color = Gold, fontWeight = FontWeight.Bold) } }
@Composable private fun Melts(s: GoldBarState, on: (List<MeltEntry>) -> Unit) { var w by remember { mutableStateOf("") }; var a by remember { mutableStateOf("") }; var d by remember { mutableStateOf("") }; Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { BoxCard { Text("ثبت آبشده دستی — ترازو حذف شده است", color = Gold); Input("وزن گرم", w) { w = it }; Input("عیار", a) { a = it }; Input("توضیحات", d) { d = it }; Button({ val nw=PersianNumber.parse(w); val na=PersianNumber.parse(a); if(nw>0 && na>0) { on(s.entries + MeltEntry(weight=nw, assay=na, description=d, createdAt=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")))); w=""; a=""; d="" } }) { Text("ثبت") } }; LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(s.entries) { e -> BoxCard { Text("${PersianNumber.format(e.weight)} g | عیار ${PersianNumber.format(e.assay,0)}", color = Gold); if(e.description.isNotBlank()) Text(e.description); TextButton({ on(s.entries.filterNot { it.id == e.id }) }) { Text("حذف") } } } } } }
@Composable private fun Increase(s: GoldBarState, on: (ReportSection)->Unit) { val sum=AssayEngine.summarize(s.entries); Section("افزایش عیار", s.increaseAssay, listOf("عیار هدف","عیار شمش"), on) { v -> val r=AssayEngine.increase(sum, PersianNumber.parse(v[0]), PersianNumber.parse(v[1])); listOf(SectionField("شمش مورد نیاز", PersianNumber.format(r.requiredBar), "g")) } }
@Composable private fun Assay(s: GoldBarState, on: (ReportSection)->Unit) { val sum=AssayEngine.summarize(s.entries); Section("عیار", s.assay, listOf("عیار هدف","درصد نقره"), on) { v -> val r=AssayEngine.alloy(sum, PersianNumber.parse(v[0]), PersianNumber.parse(v[1]), sum.weight); listOf(SectionField("کل بار مورد نیاز", PersianNumber.format(r.totalAlloyRequired), "g"), SectionField("نقره مورد نیاز", PersianNumber.format(r.silverRequired), "g"), SectionField("بار نهایی", PersianNumber.format(r.totalAfterAlloy), "g")) } }
@Composable private fun Quick(s: GoldBarState, on: (ReportSection)->Unit) { Section("محاسبه سریع", s.quickCalculation, listOf("وزن پایه"), on) { v -> val r=AssayEngine.split(PersianNumber.parse(v[0])); listOf(SectionField("طلای 995", PersianNumber.format(r.first), "g"), SectionField("طلای 750", PersianNumber.format(r.second), "g")) } }
@Composable private fun Cost(s: GoldBarState, on: (ReportSection)->Unit) { Section("هزینه عیار", s.assayCost, listOf("جمع هزینه عیار"), on) { emptyList() } }
@Composable private fun Section(title: String, old: ReportSection, labels: List<String>, on: (ReportSection)->Unit, calc: (List<String>)->List<SectionField>) { val values = remember(old) { mutableStateListOf(*labels.map { label -> old.fields.firstOrNull { it.label == label }?.value ?: "" }.toTypedArray()) }; val outs = calc(values); BoxCard { Text(title, color = Gold, fontWeight = FontWeight.Bold); labels.forEachIndexed { i,l -> Input(l, values[i]) { values[i] = it } }; outs.forEach { Text("${it.label}: ${it.value} ${it.unit}") }; Button({ on(ReportSection(title, labels.mapIndexed { i,l -> SectionField(l, values[i]) } + outs)) }) { Text("ثبت بخش در گزارش") } } }
