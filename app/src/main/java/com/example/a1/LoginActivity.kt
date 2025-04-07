/**
 * Assignment 1: NutriTrack App
 * Author: Farnoush Mehraban Ghezelhesar
 * Student Id: 33391629
 */

package com.example.a1

// Required Android and Compose libraries
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a1.ui.theme.A1Theme
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * LoginActivity - Screen where the user selects their User ID and enters their phone number to continue.
 */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sets the UI content of the Login screen using Compose and the app theme
        setContent {
            A1Theme {
                LoginScreen(this)
            }
        }
    }
}

/**
 * Composable function displaying the Login UI.
 * Allows selecting a User ID from a dropdown and entering a phone number.
 */
@Composable
fun LoginScreen(context: Context) {
    // Load all user IDs from the CSV file into memory
    val userIds = remember { loadAllUserIds(context) }

    // UI state variables
    var selectedUserId by remember { mutableStateOf("") }    // Holds the selected User ID
    var expanded by remember { mutableStateOf(false) }       // Controls the dropdown expansion
    var phoneNumber by remember { mutableStateOf("") }       // Stores the entered phone number
    var errorMessage by remember { mutableStateOf("") }      // Displays validation errors

    // Layout container for the login form
    Column(
        modifier = Modifier
            .fillMaxSize()                 // Fill the full screen
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),              // Add padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Login title
        Text(text = "Log in", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown label
        Text(text = "Select User ID", fontSize = 14.sp)

        // Button to show dropdown
        Button(
            onClick = { expanded = true },          // Open dropdown on click
            modifier = Modifier.fillMaxWidth()
        ) {
            // Show either placeholder or selected ID
            Text(text = if (selectedUserId.isEmpty()) "Choose ID" else "ID: $selectedUserId")
        }

        // Dropdown menu to show all user IDs
        DropdownMenu(
            expanded = expanded,                    // Controlled by expanded variable
            onDismissRequest = { expanded = false },// Close dropdown on outside tap
            modifier = Modifier.fillMaxWidth()
        ) {
            // For each ID, create a clickable menu item
            userIds.forEach { id ->
                DropdownMenuItem(
                    text = { Text(text = id) },
                    onClick = {
                        selectedUserId = id         // Set selected ID
                        expanded = false            // Close dropdown
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Phone number input field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },   // Update state on change
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show error message if present
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to continue to next screen after validation
        Button(
            onClick = {
                val isValid = validateUser(context, selectedUserId, phoneNumber)
                if (isValid) {
                    // Clear error if valid
                    errorMessage = ""

                    // Save user ID and phone number in SharedPreferences
                    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("user_id", selectedUserId)
                        putString("phone_number", phoneNumber)
                        apply()
                    }

                    // Navigate to the FoodIntakeActivity
                    context.startActivity(Intent(context, FoodIntakeActivity::class.java))
                } else {
                    // Show error message if invalid
                    errorMessage = "Invalid ID or Phone Number. Please try again."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Continue")
        }
    }
}

/**
 * Validates user by checking the entered ID and phone number against the CSV file.
 *
 * @param context - application context to access assets
 * @param inputId - selected User ID from dropdown
 * @param inputPhone - entered phone number
 * @return true if a matching record is found, false otherwise
 */
fun validateUser(context: Context, inputId: String, inputPhone: String): Boolean {
    val assetManager = context.assets
    val inputStream = assetManager.open("user_data.csv")
    val reader = BufferedReader(InputStreamReader(inputStream))

    // Read all lines and check for a matching phone + user ID pair
    reader.useLines { lines ->
        lines.drop(1).forEach { line -> // Skip CSV header
            val columns = line.split(",")
            if (columns.size > 1) {
                val phone = columns[0].trim()
                val userId = columns[1].trim()
                if (phone == inputPhone && userId == inputId) {
                    return true // Match found
                }
            }
        }
    }
    return false // No match found
}

/**
 * Loads all user IDs from the CSV file.
 *
 * @param context - application context to access assets
 * @return List of user IDs (as Strings)
 */
fun loadAllUserIds(context: Context): List<String> {
    val ids = mutableSetOf<String>()
    val assetManager = context.assets
    val inputStream = assetManager.open("user_data.csv")
    val reader = BufferedReader(InputStreamReader(inputStream))

    // Read each line and extract user ID from column 2
    reader.useLines { lines ->
        lines.drop(1).forEach { line -> // Skip header
            val columns = line.split(",")
            if (columns.size > 1) {
                ids.add(columns[1].trim()) // Extract and add user ID
            }
        }
    }
    return ids.toList() // Convert to list and return
}
