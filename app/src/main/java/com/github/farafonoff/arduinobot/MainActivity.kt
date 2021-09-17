package com.github.farafonoff.arduinobot

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import com.github.farafonoff.arduinobot.ui.theme.ArduinoBotTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import com.hoho.android.usbserial.driver.UsbSerialProber

import com.hoho.android.usbserial.driver.UsbSerialDriver

import androidx.core.content.ContextCompat.getSystemService

import android.hardware.usb.UsbManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val timerFlow = simple()
        setContent {
            ArduinoBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        val context = LocalContext.current
                        //val value by timerFlow.collectAsState(0)
                        var greetings by rememberSaveable { mutableStateOf(1) }
                        val devices by getDevice(context).collectAsState(emptyList())
                        //Text(text = "Timer $value")
                        devices.ifEmpty { listOf("No devices attached") }.map { d -> Text(d) }
                        /*(1..greetings).map { i -> Greeting(name = "Android$i") }
                        Button(onClick = { greetings = ++greetings }) {
                            Text("Add greeting")
                        }*/
                    }
                }
            }
        }
    }
}

fun simple(): Flow<Int> = flow { // sequence builder
    var i = 1
    while (true) {
        delay(100) // pretend we are computing it
        ++i
        emit(i) // yield next value
    }
}

fun getDevice(context: android.content.Context): Flow<List<String>> = flow {
    while (true) {
        emit(justGetDevices(context))
        delay(1000)
    }
}

fun justGetDevices(context: android.content.Context): List<String> {
    val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
    val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
    if (availableDrivers.isEmpty()) {
        return emptyList()
    }
    return availableDrivers.map { device -> device.device.toString()}
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArduinoBotTheme {
        Column {
            Greeting("Android")
            Button(onClick = { /*TODO*/ }, content = { Text("Add greeting")})
        }
    }
}