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
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.farafonoff.arduinobot.ui.theme.ArduinoBotTheme
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val timerFlow = simple()
        setContent {
            val context = LocalContext.current
            connectAccelerometer(context)
            val devices by connectDevice(context).collectAsState(emptyList())
            ArduinoBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        DevicesList(devices)
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
            val x = event?.values?.get(0)
            val y = event?.values?.get(1)
            val z = event?.values?.get(2)
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
private fun DevicesList(devices: List<String>) {
    devices.ifEmpty { listOf("No devices attached") }.map { d -> Text(d) }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArduinoBotTheme {
        Column {
            DevicesList(emptyList())
        }
    }
}