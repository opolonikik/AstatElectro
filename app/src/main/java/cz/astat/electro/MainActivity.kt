package cz.astat.electro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AstatElectroApp() }
    }
}

private val Bg = Color(0xFF08111F)
private val Card = Color(0xFF111E30)
private val Cyan = Color(0xFF34D6FF)
private val Green = Color(0xFF5DE08A)
private val Red = Color(0xFFFF5C6C)

@Composable
fun AstatElectroApp() {
    var windOn by remember { mutableStateOf(true) }
    var hydroOn by remember { mutableStateOf(false) }
    var solarOn by remember { mutableStateOf(true) }
    var emergency by remember { mutableStateOf(false) }
    var windSpeed by remember { mutableFloatStateOf(55f) }
    val breakers = remember { mutableStateListOf(true, true, false, true) }

    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Bg, surface = Card)) {
        Column(
            Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ASTAT ELECTRO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 25.sp)
                    Text("OFFLINE SIMULATOR • ESP32 DISCONNECTED", color = Cyan, fontSize = 11.sp)
                }
                Text(if (emergency) "⚠ TRIP" else "● NORMAL", color = if (emergency) Red else Green, fontWeight = FontWeight.Bold)
            }

            TelemetryCard(windOn, hydroOn, solarOn, windSpeed)
            StationCard("🌬 ВЭС", windOn, { windOn = it }, "15 генераторов")
            Text("Обороты ВЭС: ${windSpeed.toInt()}%", color = Color.White)
            Slider(value = windSpeed, onValueChange = { windSpeed = it }, valueRange = 0f..100f, enabled = !emergency)
            StationCard("💧 ГЭС", hydroOn, { hydroOn = it }, "Турбина / заслонка")
            StationCard("☀ СЭС", solarOn, { solarOn = it }, "10 солнечных панелей")

            Text("ПОДСТАНЦИЯ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            breakers.forEachIndexed { i, closed ->
                Card(colors = CardDefaults.cardColors(containerColor = Card), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Разъединитель ${i + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(if (closed) "CLOSED" else "OPEN", color = if (closed) Green else Red)
                        }
                        Switch(checked = closed, onCheckedChange = { breakers[i] = it }, enabled = !emergency)
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Card), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("🔋 НАКОПИТЕЛЬ", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("74%  •  3.91 V  •  1.24 A", color = Green, fontSize = 20.sp)
                    LinearProgressIndicator(progress = { 0.74f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }

            Button(
                onClick = {
                    emergency = !emergency
                    if (emergency) {
                        windOn = false; hydroOn = false
                        for (i in breakers.indices) breakers[i] = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (emergency) Green else Red),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text(if (emergency) "СБРОСИТЬ АВАРИЮ" else "⚠ АВАРИЙНЫЙ STOP", fontWeight = FontWeight.Black, fontSize = 17.sp)
            }

            Text("Test build 0.1 • данные генерируются локально", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TelemetryCard(wind: Boolean, hydro: Boolean, solar: Boolean, speed: Float) {
    val power = (if (wind) speed * 0.32f else 0f) + (if (hydro) 18f else 0f) + (if (solar) 8.4f else 0f)
    Card(colors = CardDefaults.cardColors(containerColor = Card), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("ЭНЕРГОСИСТЕМА", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("⚡ ${"%.1f".format(power)} W", color = Cyan, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Сеть 12.1 V   •   Частота (сим.) 50.0 Hz", color = Color.LightGray)
        }
    }
}

@Composable
private fun StationCard(title: String, enabled: Boolean, onChange: (Boolean) -> Unit, subtitle: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Card), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text(subtitle, color = Color.LightGray)
                Text(if (enabled) "ONLINE" else "OFFLINE", color = if (enabled) Green else Red, fontWeight = FontWeight.Bold)
            }
            Switch(checked = enabled, onCheckedChange = onChange)
        }
    }
}
