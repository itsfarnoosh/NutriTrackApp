/**
 * Assignment 1: NutriTrack App
 * Author: Farnoush Mehraban Ghezelhesar
 * Student Id: 33391629
 */
package com.example.a1

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a1.ui.theme.A1Theme
import java.util.*

class FoodIntakeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve user info
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)
        val phoneNumber = sharedPref.getString("phone_number", null)
        setContent {
            A1Theme {
                FoodIntakeScreen()
            }
        }
    }
}

data class Persona(val name: String, val description: String, val image: Int)

/**
 * Composable function that displays the Food Intake Questionnaire screen.
 *
 * This screen allows users to:
 * - Select which food categories they consume using checkboxes.
 * - Choose a health persona that represents their dietary behavior.
 * - Set their meal, sleep, and wake-up times using time pickers.
 * - View additional information about personas in a modal dialog.
 * - Save their preferences to SharedPreferences for later retrieval.
 *
 * On the first composition, it reads previously saved values from SharedPreferences
 * and pre-fills the UI with those values using a `LaunchedEffect`.
 *
 * When the user presses the "Save" button, it stores the updated preferences
 * and navigates back to the `HomeActivity`.
 */
@Composable
fun FoodIntakeScreen() {

    var showModal by remember { mutableStateOf(false) }
    var currentPersona: Persona? by remember { mutableStateOf(null) }


    // This allows access to resources, SharedPreferences, launching activities, etc.,
    // similar to 'this' in an Activity
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("FoodIntakePreferences", Context.MODE_PRIVATE)

    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var selectedPersona by remember { mutableStateOf("") }
    var mealTime by remember { mutableStateOf("") }
    var sleepTime by remember { mutableStateOf("") }
    var wakeUpTime by remember { mutableStateOf("") }
    //LaunchedEffect is a side-effect API in Jetpack Compose used to launch a coroutine when a certain key changes or when a composable first enters the composition.
    // Launch a coroutine when the composable enters the composition (i.e., is first displayed on screen)
    LaunchedEffect(Unit) {

        // Retrieve the previously saved food categories as a comma separated string from SharedPreferences
        val categoriesStr = sharedPreferences.getString("selectedCategories", "") ?: ""

        // Split the string into a list, trim whitespace, remove any empty entries, and convert to a Set
        // This ensures we have a clean set of selected food categories to show as pre-checked checkboxes
        selectedCategories = categoriesStr
            .split(",")                            // Split by comma
            .map { it.trim() }                     // Trim extra spaces from each item
            .filter { it.isNotEmpty() }            // Remove any blank entries
            .toSet()                               // Convert to a Set for efficient lookup and state handling

        // Retrieve and set the previously saved
        selectedPersona = sharedPreferences.getString("selectedPersona", "") ?: ""
        mealTime = sharedPreferences.getString("mealTime", "") ?: ""
        sleepTime = sharedPreferences.getString("sleepTime", "") ?: ""
        wakeUpTime = sharedPreferences.getString("wakeUpTime", "") ?: ""
    }



    val personas = listOf(
        Persona("Health Devotee", "I’m passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy.", R.drawable.persona_1),
        Persona("Mindful Eater", "I’m health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media.", R.drawable.persona_2),
        Persona("Wellness Striver", "\tI aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I’ve tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I’ll give it a go.", R.drawable.persona_3),
        Persona("Balance Seeker", "\tI try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn’t have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips.", R.drawable.persona_4),
        Persona("Health Procrastinator", "Health Procrastinator\n" +
                "I’m contemplating healthy eating but it’s not a priority for me right now. I know the basics about what it means to be healthy, but it doesn’t seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life.", R.drawable.persona_5),
        Persona("Food Carefree", "I’m not bothered about healthy eating. I don’t really see the point and I don’t think about it. I don’t really notice healthy eating tips or recipes and I don’t care what I eat.", R.drawable.persona_6)
    )

    // Function to save data in SharedPreferences
    fun saveData() {
        val sharedPreferences = context.getSharedPreferences("FoodIntakePreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save selected categories (as a comma-separated string)
        editor.putString("selectedCategories", selectedCategories.joinToString(","))
        // Save selected persona
        editor.putString("selectedPersona", selectedPersona)
        // Save timings
        editor.putString("mealTime", mealTime)
        editor.putString("sleepTime", sleepTime)
        editor.putString("wakeUpTime", wakeUpTime)

        // Apply changes
        editor.apply()

        // Log the saved data for checking
        Log.d("FoodIntake", "Saved selectedCategories: ${selectedCategories.joinToString(",")}")
        Log.d("FoodIntake", "Saved selectedPersona: $selectedPersona")
        Log.d("FoodIntake", "Saved mealTime: $mealTime")
        Log.d("FoodIntake", "Saved sleepTime: $sleepTime")
        Log.d("FoodIntake", "Saved wakeUpTime: $wakeUpTime")

        // Start HomeActivity after saving
        val intent = Intent(context, HomeActivity::class.java)
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(text = "Food Intake Questionnaire", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Tick all the food categories you can eat:")
        val categories = listOf(
            "Fruits", "Vegetables", "Grains",
            "Meat", "Alcoholic beverages", "Sweets",
            "Dairy"," Wholegrain"
        )

// Split categories into 3 equal columns manually
        val column1 = listOf("Fruits", "Vegetables", "Grains")
        val column2 = listOf("Meat", "Dairy" , "Sweets")
        val column3 = listOf("Alcoholic beverages", "Wholegrain")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // First Column
            Column {
                column1.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = {
                                selectedCategories = if (it)
                                    selectedCategories + category
                                else
                                    selectedCategories - category
                            }
                        )
                        Text(category)
                    }
                }
            }

            // Second Column
            Column {
                column2.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = {
                                selectedCategories = if (it)
                                    selectedCategories + category
                                else
                                    selectedCategories - category
                            }
                        )
                        Text(category)
                    }
                }
            }

            // Third Column
            Column {
                column3.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = {
                                selectedCategories = if (it)
                                    selectedCategories + category
                                else
                                    selectedCategories - category
                            }
                        )
                        Text(category)
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Your Persona")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            personas.chunked(3).forEach { rowItems ->
                Column {
                    rowItems.forEach { persona ->
                        Button(onClick = {
                            currentPersona = persona
                            showModal = true
                        }, modifier = Modifier.padding(0.dp)) {
                            Text(text = persona.name, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Persona Modal
        if (showModal && currentPersona != null) {
            PersonaInfoModal(
                personaName = currentPersona!!.name,
                personaDescription = currentPersona!!.description,
                personaImage = currentPersona!!.image,
                onDismiss = { showModal = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Select best-fitting persona")

        var expanded by remember { mutableStateOf(false) }
        val personaOptions = personas.map { it.name }

        Box {
            OutlinedTextField(
                value = selectedPersona,
                onValueChange = {},
                label = { Text("Choose your persona") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand Dropdown")
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                personaOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedPersona = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Timings")
        TimePickerField("Biggest meal time", mealTime) { mealTime = it }
        TimePickerField("Sleep time", sleepTime) { sleepTime = it }
        TimePickerField("Wake up time", wakeUpTime) { wakeUpTime = it }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                saveData() // Call save function when Save button is pressed
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Save")
        }
    }
}

/**
 * A reusable Composable that displays a button to open a time picker dialog
 * and shows the selected time or a placeholder if no time has been selected.
 *
 * @param label The label to display before the selected time (e.g., "Sleep time")
 * @param time The currently selected time as a string (e.g., "08:30")
 * @param onTimeSelected A callback function that is triggered when a time is picked,
 *                       passing the selected time string back to the parent.
 */
@Composable
fun TimePickerField(label: String, time: String, onTimeSelected: (String) -> Unit) {

    // Get the current context to be used for showing the TimePickerDialog
    val context = LocalContext.current

    // Create a Calendar instance to initialize the time picker with the current time
    val calendar = Calendar.getInstance()

    // Define the TimePickerDialog that will show a time picker popup when triggered
    val timePickerDialog = TimePickerDialog(
        context,
        // Callback function when the user sets the time
        { _, hour, minute ->
            // Format the time
            onTimeSelected(String.format("%02d:%02d", hour, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY), // Initial hour (current hour)
        calendar.get(Calendar.MINUTE),      // Initial minute (current minute)
        true // Use 24-hour format
    )

    // A button styled as an outlined box that, when clicked, shows the time picker dialog
    OutlinedButton(
        onClick = { timePickerDialog.show() }, // Show the time picker when clicked
        modifier = Modifier.fillMaxWidth()     // Make the button span full width
    ) {
        // Display the label and either the selected time or a default placeholder
        Text(text = "$label: ${if (time.isEmpty()) "Select Time" else time}")
    }
}


/**
 * A Composable function to display a modal dialog with persona information.
 *
 * @param personaName Name of the persona (shown as title).
 * @param personaDescription A brief description of the persona.
 * @param personaImage The image resource ID representing the persona.
 * @param onDismiss A callback function to dismiss the modal.
 */
@Composable
fun PersonaInfoModal(
    personaName: String,
    personaDescription: String,
    personaImage: Int,
    onDismiss: () -> Unit
) {
    // AlertDialog is used to show a modal popup dialog
    AlertDialog(
        // Callback when user taps outside or presses back
        onDismissRequest = onDismiss,

        // Title of the dialog (in this case, just shows the persona name)
        title = {
            Column {
                Text(text = personaName, fontSize = 20.sp)
            }
        },

        // Main content inside the modal
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
                modifier = Modifier.fillMaxWidth()
            ) {
                // Display the image associated with the persona
                Image(
                    painter = painterResource(id = personaImage),
                    contentDescription = personaName, // Used for accessibility
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(12.dp)) // Add space between image and description
                // Show the persona's description
                Text(
                    text = personaDescription,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp) // Add padding around the text
                )
            }
        },

        // Confirmation button at the bottom of the dialog
        confirmButton = {
            // Center the dismiss button horizontally
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = "Dismiss") // Button text
                }
            }
        }
    )
}
