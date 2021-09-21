package com.github.farafonoff.arduinobot

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.farafonoff.arduinobot.ui.theme.ArduinoBotTheme
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.math.PI
import kotlin.math.sqrt

var computedAngle = 0.0

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val timerFlow = timer(30)
        setContent {
            val context = LocalContext.current
            connectAccelerometer(context)
            val devices by connectDevice(context).collectAsState(emptyList())
            val angles by timerFlow.map { computedAngle }.collectAsState(initial = 0)
            ArduinoBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        DevicesList(devices)
                        Text("$angles")
                    }
                }
            }
        }
    }
}

fun connectDevice(context: android.content.Context): Flow<List<String>> = flow {
    while (true) {
        val devices = justGetDevices(context)
        emit(devices.map { device -> "${device.device.deviceName}:${device.device.manufacturerName}"})
        val device = devices.firstOrNull()
        if (device!=null) {

        }
        delay(1000)
    }
}.flowOn(Dispatchers.Default)

fun connectAccelerometer(context: android.content.Context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    val triggerEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val x = event?.values?.get(0) ?: 0.0f
            val y = event?.values?.get(1) ?: 0.0f
            val z = event?.values?.get(2) ?: 0.0f
            val l1 = x*x+y*y;
            val l2 = l1 + z*z;
            val cos = l1 / sqrt(l1.toDouble()) / sqrt(l2.toDouble())
            val angle = kotlin.math.acos(cos)
            computedAngle = PI/2 - angle
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
    sensorManager.registerListener(triggerEventListener, sensor, SENSOR_DELAY_FASTEST)
}

fun justGetDevices(context: android.content.Context): List<UsbSerialDriver> {
    val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
    val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
    if (availableDrivers.isEmpty()) {
        return emptyList()
    }
    return availableDrivers
}

@Composable
fun DevicesList(devices: List<String>) {
    devices.ifEmpty { listOf("No devices attached") }.map { d -> Text(d) }
}

@Composable
fun ProportionalCoef(value: Double, onValueChange: (Double) -> Unit, computedValue:Double = 0.0) {
    Column {
        Spacer(modifier = Modifier.padding(16.dp))
        Text("Пропорциональный")
        Text("Расчет: $computedValue")
        Row {
            var enabled by remember { mutableStateOf(true)}
            Checkbox(checked = enabled, onCheckedChange = { enabled = !enabled})
            Button(onClick = { onValueChange(value/10) }) {
                Text("/10")
            }
            Text("$value", style = TextStyle(textAlign = TextAlign.Center), modifier = Modifier
                .padding(16.dp))
            Button(onClick = { onValueChange(value*10) }) {
                Text("*10")
            }
        }

    }
}

fun timer(updatesPerSecond: Int): Flow<Unit> = flow {
    val interval = 1000L/updatesPerSecond
    while(true) {
        delay(interval)
        emit(Unit)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArduinoBotTheme {
        Column {
            DevicesList(emptyList())
            ProportionalCoef(value = 0.0, onValueChange = {})
        }
    }
}