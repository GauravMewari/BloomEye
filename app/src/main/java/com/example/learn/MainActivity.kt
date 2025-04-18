package com.example.learn


//import android.graphics.fonts.FontFamily
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.google.android.gms.common.config.GservicesValue.value
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.*
import kotlin.collections.getValue


// This is Data Class For hydroData
data class HydroData(
    val humidity: Int = 0,
    val pH: Double = 0.0,
    val temp: Double = 0.0
)

// Another Class for Controlling the Electrical Equipments
data class DeviceState(
    val led: Int = 0,
    val second: Int = 0
)


// Our Main Activity just like Main Function
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.hashCode())
        )*/
        window.statusBarColor = android.graphics.Color.BLACK
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            MaterialTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White // Keeping a White So that Status bar can show
                ){
                    AllFunctions()
                }
            }

        }
    }
}



@Preview(showSystemUi = true)

@Composable
fun AllFunctions(){
var myDeviceState by remember {mutableStateOf(DeviceState())}
    FirebaseDataDisplay(
        onDeviceStateChange = {newDeviceState ->
            myDeviceState = newDeviceState
        }
    ){
        hydroData,deviceState ->
           Column{
               ThreeCards(hydroData = hydroData)
               ToggleButton(deviceState =
               myDeviceState, onDeviceStateChange = {
                   newDeviceState ->
                   myDeviceState = newDeviceState
               })
           }
    }
    TopRightImage()
    SmartMonitoring()
    SmartDisplay()
    ThreeCardsForCharts()
    SmartControl()
}


@Composable

    fun FirebaseDataDisplay(
    onDeviceStateChange: (DeviceState) -> Unit,
    content: @Composable (HydroData, DeviceState) -> Unit) {


        var hydroData by remember { mutableStateOf(HydroData()) }
        var deviceState by remember {mutableStateOf (DeviceState())}

        LaunchedEffect(Unit) {

            val database = Firebase.database
            val myRef = database.getReference("/") // Reference to the root

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = dataSnapshot.child("hydro").getValue(HydroData::class.java)

                    if (data != null) {
                        hydroData = data
                    }
                    val newLedState = dataSnapshot.child("led").child("state").getValue(Int::class.java)?: 0
                    val newSecondState = dataSnapshot.child("second").child("state").getValue(Int::class.java) ?: 0

                    deviceState = DeviceState(led = newLedState,second = newSecondState)
                    onDeviceStateChange(deviceState)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("FirebaseData", "Failed to read value.", error.toException())
                }
            })
        }
content(hydroData, deviceState)
    }



/*
@Composable
fun ToggleButton(deviceState: DeviceState, onDeviceStateChange: (DeviceState) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 360.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            val newLedState = if (deviceState.led == 1) 0 else 1
            val database = Firebase.database
            val myRef = database.getReference("/")
            myRef.child("led").child("state").setValue(newLedState).addOnCompleteListener{
                if(it.isSuccessful) {
                    val newDeviceState = deviceState.copy(led = newLedState)
                    onDeviceStateChange(newDeviceState)
                } else{
                    Log.e("Toggle Button","Failed to update led state",it.exception)
                }
            }
        }) {
            Text(text = " ${if (deviceState.led == 1) "OFF" else "ON"}")
        }

        Button(onClick = {
            val newSecondState = if (deviceState.second == 1) 0 else 1

            val database = Firebase.database
            val myRef = database.getReference("/")
            myRef.child("second").child("state").setValue(newSecondState).addOnCompleteListener{
            if(it.isSuccessful){
                val newDeviceState = deviceState.copy(second = newSecondState)
                onDeviceStateChange(newDeviceState)
            }else{
                Log.e("ToggleButton", "Failed to update second state", it.exception)
            }
            }
           }) {
            Text(text = " ${if (deviceState.second == 1) "OFF" else "ON"}")
        }
    }
}

*/



@Composable
fun TopRightImage() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(end = 24.dp), // Add padding around the image
            contentAlignment = Alignment.TopEnd // Align content to top-right
        ) {
            Image(
                painter = painterResource(id = R.drawable.bloomeye),
                contentDescription = "Bloom Eye Logo",
                modifier = Modifier.size(70.dp) // Set the size of the image
            )
        }
    }
}





// First Text for Smart Monitoring
@Composable
fun SmartMonitoring(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 10.dp)
    ) {
        Text(
            text = "Smart Monitoring:",
            color = Color.Blue,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Normal,
        )
    }
}
// Smart Displaying  text Function
@Composable
fun SmartDisplay(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 200.dp, start = 10.dp)
    ) {
        Text(
            text = "Charts For Display :",
            color = Color.Blue,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = cavetFontFamily
        )
    }
}

// Smart Controlling Text Function
@Composable
fun SmartControl(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 500.dp, start = 10.dp)
    ) {
        Text(
            text = "Smart Control :",
            color = Color.Green,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Normal,
        )
    }
}





@Composable
fun FullWidthCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E8), // Light green
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
           // horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
              //  fontStyle = FontStyle.
            )

            Text(text = value, fontSize = 24.sp)
        }
    }
}

@Composable
fun ThreeCards(hydroData: HydroData) {
    val lazyListState = rememberLazyListState()

    val cardTitles = listOf("pH :", "Temperature :","Humidity :")
    val cardValues = listOf(
        hydroData.pH.toString(),
        "${hydroData.temp}°C", // Added °C symbol
        "${hydroData.humidity}%"      // Added % symbol


        /*
        hydroData.temp.toString(),
        "${hydroData.temp}°C", // Adding the °C Symbol
        hydroData.humidity.toString()

         */
    )


    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 85.dp),
        state = lazyListState,
        //userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(cardTitles.size) { index ->
            Column(modifier = Modifier.fillParentMaxWidth()) {
                FullWidthCard(title = cardTitles[index],
                value = cardValues[index])
            }
        }
    }
}





@Composable
fun DisplayCard(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFB8C8), // Light green
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = CardDefaults.outlinedShape
    ) {
        Column(
            modifier = Modifier
                .padding(top = 150.dp)
                .fillMaxWidth()
                ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun ThreeCardsForCharts() {
    val lazyListState = rememberLazyListState()

    val cardTitles = listOf("---pH---", "---Temperature---","---Humidity---")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 250.dp)
            .fillMaxHeight(1.0f),
        state = lazyListState,
        //userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(cardTitles.size) { index ->
            Column(modifier = Modifier
                .fillParentMaxWidth()
                .fillParentMaxHeight()
                .fillMaxSize()) {
                DisplayCard(title = cardTitles[index])
            }
        }
    }
}

/*
@Composable
fun ToggleButton1() {
    var isOn by remember { mutableStateOf(false) }
    val backgroundColor = if (isOn) Color.Green else Color.Red
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 550.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(backgroundColor, CircleShape)
                .clickable { isOn = !isOn }
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isOn) "ON" else "OFF", color = Color.White)
        }
    }
}
@Composable
fun ToggleButton2() {

    var isOn by remember { mutableStateOf(false) }
    val backgroundColor = if (isOn) Color.Green else Color.Red
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 550.dp, end = 10.dp),
        horizontalAlignment = Alignment.End,

        ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(backgroundColor, CircleShape)
                .clickable { isOn = !isOn },
            // .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isOn) "ON" else "OFF", color = Color.White)
        }
    }

}
*/


val cavetFontFamily = FontFamily(
    Font(R.font.caveat,FontWeight.Normal)
)


