/**
 * Assignment 1: NutriTrack App
 * Author: Farnoush Mehraban Ghezelhesar
 * Student Id: 33391629
 */
package com.example.a1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.io.BufferedReader
import java.io.InputStreamReader
import com.example.a1.ui.theme.A1Theme



/**
 * HomeActivity
 * It initializes the UI with a bottom navigation bar and loads the user's food quality score.
 * The activity handles navigation between different composable screens using Jetpack Navigation.
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calculate the user's food quality score (e.g., from SharedPreferences or analysis)
        val score = getFoodQualityScore(this)

        // Access shared preferences to store and retrieve user-specific data
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Store the total score in shared preferences for later access
        sharedPref.edit().putFloat("total_score", score).apply()

        // Save the score to a historical log (likely for tracking past performance)
        saveScoreToHistory(this, score)

        // Set the main content view using Jetpack Compose
        setContent {
            A1Theme {
                // Create a navigation controller to manage screen transitions
                val navController: NavHostController = rememberNavController()

                // Get the start destination from the intent, defaulting to "home"
                val startDest = intent.getStringExtra("navigateTo") ?: "home"

                // Scaffold provides a layout structure with slots for top bar, bottom bar, and content
                Scaffold(
                    bottomBar = {
                        // Custom bottom navigation bar component
                        MyBottomNavigationBar(navController)
                    }
                ) { innerPadding ->
                    // Set up the navigation graph for managing destinations
                    NavHost(
                        navController = navController,
                        startDestination = startDest,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Home screen showing the current score
                        composable("home") {
                            HomeScreen(score = score, navController = navController)
                        }

                        // Insights screen displaying a breakdown of scores by category
                        composable("insights") {
                            val context = LocalContext.current
                            val userId = sharedPref.getString("user_id", null)
                            val totalScore = sharedPref.getFloat("total_score", 0f)
                            val categoryScores = calculateScoresFromCSV(context, userId)

                            InsightsScreen(
                                categoryScores = categoryScores,
                                totalScore = totalScore
                            )
                        }

                        // Score history screen with a back button navigating to home
                        composable("score_history") {
                            ScoreHistoryScreen(onBack = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            })
                        }

                        // Placeholder screen for future Nutricoach feature
                        composable("nutricoach") {
                            ComingSoonScreen("Nutricoach")
                        }

                        // Placeholder screen for future Settings feature
                        composable("settings") {
                            ComingSoonScreen("Settings")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Saves the given food quality score to the user's score history in SharedPreferences.
 *
 * @param context The context used to access SharedPreferences.
 * @param score The food quality score to be saved.
 */
fun saveScoreToHistory(context: Context, score: Float) {
    // Access the shared preferences file named "user_prefs"
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Retrieve the user ID from shared preferences; if not found, exit early
    val userId = prefs.getString("user_id", null) ?: return

    // Create a unique key for storing the score history, based on the hash of the user ID
    val key = "score_history_${userId.hashCode()}"

    // Retrieve the existing score history (a comma-separated string), or an empty string if none exists
    val existing = prefs.getString(key, "") ?: ""

    // Append the new score to the existing history, using a comma as a separator
    val updated = if (existing.isEmpty()) "$score" else "$existing,$score"

    // Save the updated score history back into shared preferences
    prefs.edit().putString(key, updated).apply()
}


/**
 * HomeScreen displays the main dashboard of the app.
 * It shows a personalized greeting, user's food quality score, and options to edit preferences or view score history.
 *
 * @param score The current food quality score to display.
 * @param navController Used for navigating between composable screens.
 */
@Composable
fun HomeScreen(score: Float?, navController: NavHostController) {
    val context = LocalContext.current

    // Access SharedPreferences to retrieve and store user data
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Get the user's name from SharedPreferences and track if the name input modal should be shown
    val userId = prefs.getString("user_id", null) ?: ""
    val userNameKey = "user_name_$userId" // Unique key for each user's name
    var userName by remember { mutableStateOf(prefs.getString(userNameKey, "") ?: "") }
    var showNameModal by remember { mutableStateOf(userName.isEmpty()) }

    // Show the name input modal if the user's name has not been stored
    if (showNameModal) {
        NameInputModal(
            onDismiss = { showNameModal = false },
            onNameSubmit = { name ->
                userName = name
                prefs.edit().putString("user_name", name).apply()
                showNameModal = false
            }
        )
    }

    // Main screen layout using a vertical scrollable column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Display a greeting with the user's name
        Text("Hello, $userName", fontSize = 24.sp, color = Color(0xFFFFA500))

        Spacer(modifier = Modifier.height(16.dp))

        // Display a food image
        Image(
            painter = painterResource(id = R.drawable.food),
            contentDescription = "Food Image",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Description text for the Edit button
        Text("By pressing on Edit you change your preferences", fontSize = 16.sp, color = Color.Gray)

        // Button to navigate to FoodIntakeActivity where preferences can be edited
        Button(
            onClick = { context.startActivity(Intent(context, FoodIntakeActivity::class.java)) },
            modifier = Modifier.width(100.dp)
        ) {
            Text("Edit")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section for displaying the current score
        Text("My Score", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Text("Your Food Quality Score", fontSize = 16.sp)

        // Display the score or "N/A" if null, color-coded based on score
        Text(
            "${score?.toString() ?: "N/A"}/100",
            fontSize = 32.sp,
            color = if ((score ?: 0f) >= 40f) Color.Green else Color.Red
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Button to navigate to the Score History screen using the navController
        Button(
            onClick = {
                navController.navigate("score_history")
            },
            modifier = Modifier.padding(top = 5.dp)
        ) {
            Text("Check your previous scores")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Educational section about what the food quality score means
        Text("What is the Food Quality Score?", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.\n" +
                    "This personalized measurement considers various food groups including vegetables, fruits, whole grains, and proteins to give you practical insights for making healthier food.",
            fontSize = 14.sp
        )
    }
}


/**
 * A screen displaying all previous food quality scores using LazyColumn.
 *
 * @param onBack A callback function that handles navigation back to the Home screen.
 */
@Composable
fun ScoreHistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Access shared preferences to retrieve user data
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Retrieve the user ID from shared preferences
    val userId = prefs.getString("user_id", null)

    // Construct the key used to access the user's score history
    val key = "score_history_${userId.hashCode()}"

    // Retrieve the score history string, split by commas, and convert each entry to a Float
    val scoreList = prefs.getString(key, "")
        ?.split(",")                       // Split the string into individual scores
        ?.mapNotNull { it.toFloatOrNull() } // Convert each score to Float safely
        ?: emptyList()                      // If null or empty, return an empty list

    // Layout container for the whole screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title for the screen
        Text("Your Previous Scores", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // List of scores using LazyColumn for performance
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(scoreList.size) { index ->
                Text(
                    text = "Score ${index + 1}: ${scoreList[index]}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Button to navigate back to the home screen
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Home")
        }
    }
}



/**
 * Placeholder screen for features not yet implemented.
 */
@Composable
fun ComingSoonScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name - Coming Soon!", fontSize = 24.sp)
    }
}

/**
 * Modal dialog to get user's name on first Home screen load.
 */
@Composable
fun NameInputModal(onDismiss: () -> Unit, onNameSubmit: (String) -> Unit) {
    var nameInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Your Name") },
        text = {
            TextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Your Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nameInput.isNotEmpty()) {
                        onNameSubmit(nameInput)
                    } else {
                        Toast.makeText(context, "Name can't be empty", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Reads from user_data.csv and calculates the food quality score based on the user's selected food categories and sex.
 *
 * The function retrieves the user ID and sex from SharedPreferences, reads the corresponding scores from a CSV file,
 * and sums up the scores for the selected food categories using sex-specific columns.
 *
 * @param context The application context used to access assets and shared preferences.
 * @return A Float value representing the total food quality score, or 0 if data is incomplete or missing.
 */
fun getFoodQualityScore(context: Context): Float {
    // Retrieve user preferences from SharedPreferences
    val userPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val foodPref = context.getSharedPreferences("FoodIntakePreferences", Context.MODE_PRIVATE)

    // Get user ID; return 0 if not found
    val userId = userPref.getString("user_id", null) ?: return 0f

    // Get user's sex; default to "Male" if not specified
    val sex = userPref.getString("sex", "Male") ?: "Male"

    // Get the selected food categories as a list (e.g., ["Vegetables", "Fruits", ...])
    val selectedCategories = foodPref.getString("selectedCategories", "")
        ?.split(",")
        ?.map { it.trim() }
        ?: emptyList()

    // Mapping food categories to their respective CSV column names for males and females
    val scoreMap = mapOf(
        "Vegetables" to Pair("VegetablesHEIFAscoreMale", "VegetablesHEIFAscoreFemale"),
        "Fruits" to Pair("FruitHEIFAscoreMale", "FruitHEIFAscoreFemale"),
        "Grains" to Pair("GrainsandcerealsHEIFAscoreMale", "GrainsandcerealsHEIFAscoreFemale"),
        "Wholegrain" to Pair("WholegrainsHEIFAscoreMale", "WholegrainsHEIFAscoreFemale"),
        "Meat" to Pair("MeatandalternativesHEIFAscoreMale", "MeatandalternativesHEIFAscoreFemale"),
        "Dairy" to Pair("DairyandalternativesHEIFAscoreMale", "DairyandalternativesHEIFAscoreFemale"),
        "Alcoholic beverages " to Pair("AlcoholHEIFAscoreMale", "AlcoholHEIFAscoreFemale"),
        "sweets" to Pair("SugarHEIFAscoreMale", "SugarHEIFAscoreFemale")
    )

    // Open and read the CSV file from the app's assets
    val inputStream = context.assets.open("user_data.csv")
    val reader = BufferedReader(InputStreamReader(inputStream))
    val lines = reader.readLines()

    // First line of CSV is the header, which contains column names
    val header = lines.first().split(",")

    // Find the line corresponding to the current user (column index 1 is assumed to be user ID)
    val userLine = lines.drop(1).firstOrNull { it.split(",")[1].trim() == userId } ?: return 0f
    val userValues = userLine.split(",")

    var totalScore = 0f

    // Calculate total score by summing the relevant columns based on sex and selected categories
    selectedCategories.forEach { category ->
        scoreMap[category]?.let { (maleCol, femaleCol) ->
            // Select the appropriate column based on the user's sex
            val colName = if (sex.equals("Male", ignoreCase = true)) maleCol else femaleCol

            // Find the column index in the CSV header
            val index = header.indexOf(colName)

            // If the index is valid and exists in the user's data line, add the score to the total
            if (index != -1 && index < userValues.size) {
                totalScore += userValues[index].toFloatOrNull() ?: 0f
            }
        }
    }

    // Return the final calculated score
    return totalScore
}


/**
 * Returns a map of category-wise scores for a given user from the CSV file.
 *
 * @param context The context used to access the app's assets.
 * @param userId The ID of the user whose scores are to be calculated.
 * @return A map containing category names as keys and their corresponding score as Float values.
 */
private fun calculateScoresFromCSV(context: Context, userId: String?): Map<String, Float> {
    // If userId is null, return an empty map immediately
    if (userId == null) return emptyMap()

    // Open the CSV file from the assets folder
    val inputStream = context.assets.open("user_data.csv")
    val reader = BufferedReader(InputStreamReader(inputStream))

    // Read all lines from the file
    val lines = reader.readLines()

    // Split the first line (header) to get column names
    val header = lines.firstOrNull()?.split(",") ?: return emptyMap()

    // Find the index of the User_ID and Sex columns
    val idIndex = header.indexOf("User_ID")
    val sexIndex = header.indexOf("Sex")

    // Define all nutrition-related categories with their maximum possible scores
    val categories = mapOf(
        "Discretionary" to 10,
        "Meatandalternatives" to 10,
        "Dairyandalternatives" to 10,
        "Sodium" to 10,
        "Sugar" to 10,
        "Alcohol" to 5,
        "Fats" to 5,
        "Water" to 5,
        "Grainsandcereals" to 5,
        "Wholegrains" to 5,
        "Fruits" to 5,
        "Vegetables" to 5
    )

    // Create a map from category name to a list of column indices in the header that match the category
    val columnIndices = categories.keys.associateWith { category ->
        header.mapIndexedNotNull { index, column ->
            if (column.contains(category, ignoreCase = true)) index else null
        }
    }

    // Iterate through each line (excluding the header)
    for (line in lines.drop(1)) {
        val columns = line.split(",")

        // Check if current row matches the given userId
        if (columns.size > idIndex && columns[idIndex].trim() == userId) {
            val sex = columns[sexIndex].trim() // Get user's sex
            val scores = mutableMapOf<String, Float>() // Final score map to return

            // For each category, filter columns relevant to user's sex and sum their scores
            for ((category, indices) in columnIndices) {
                // Only use columns ending with Male/Female depending on user's sex
                val relevantIndices = indices.filter { index ->
                    (sex == "Male" && header[index].endsWith("Male")) ||
                            (sex == "Female" && header[index].endsWith("Female"))
                }

                // Calculate the sum of all relevant column values for the category
                val categoryScore = relevantIndices.sumOf { columns[it].toDoubleOrNull() ?: 0.0 }

                // Store the category score in the result map
                scores[category] = categoryScore.toFloat()
            }

            // Return the scores as soon as the user's row is found and processed
            return scores
        }
    }

    // If no matching user was found, return an empty map
    return emptyMap()
}


/**
 * Custom bottom navigation bar for switching between screens.
 *
 * @param navController The NavController used to navigate between destinations.
 */
@Composable
fun MyBottomNavigationBar(navController: NavHostController) {
    // List of route names corresponding to each screen
    val items = listOf("home", "insights", "nutricoach", "settings")

    // Icons to be displayed for each tab (currently using Email icon for two tabs as placeholder)
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Email,
        Icons.Default.Email,
        Icons.Default.Settings
    )

    // Labels to be displayed under each icon
    val labels = listOf("Home", "Insights", "Nutricoach", "Settings")

    // State to keep track of currently selected tab index
    var selectedItem by remember { mutableStateOf(0) }

    // Main navigation bar container
    NavigationBar {
        // Loop through all navigation items
        items.forEachIndexed { index, item ->
            // Create a navigation item for each screen
            NavigationBarItem(
                icon = {
                    // Set the icon for this tab
                    Icon(icons[index], contentDescription = labels[index])
                },
                label = {
                    // Set the label under the icon
                    Text(labels[index])
                },
                selected = selectedItem == index, // Highlight if currently selected
                onClick = {
                    // Update the selected tab index
                    selectedItem = index

                    // Navigate to the corresponding route
                    navController.navigate(item) {
                        // Avoid building up a large backstack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true // Avoid multiple copies of the same destination
                        restoreState = true     // Restore state if the destination was previously in backstack
                    }
                }
            )
        }
    }
}


/**
 * Insights Screen – Composable to show score insights for each food category and allow sharing the result.
 *
 * @param modifier Modifier to customize the layout externally.
 * @param categoryScores A map of food categories to their respective scores.
 * @param totalScore The user's overall food quality score.
 */
@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    categoryScores: Map<String, Float>,
    totalScore: Float
) {
    // Get current context (used for sharing and showing Toast)
    val context = LocalContext.current

    // Main column layout for the screen
    Column(
        modifier = modifier
            .fillMaxSize() // Occupies the full screen
            .padding(8.dp) // Adds padding around the content
            .verticalScroll(rememberScrollState()), // Enables vertical scrolling
        horizontalAlignment = Alignment.CenterHorizontally // Center align items horizontally
    ) {
        // Title for the screen
        Text(
            text = "Insights: Food Score",
            fontSize = 18.sp,

        )

        Spacer(modifier = Modifier.height(2.dp)) // Spacing below the title

        // Display each food category with its corresponding score
        categoryScores.forEach { (category, score) ->
            // Set max score based on category
            val maxScore = when (category) {
                "Discretionary", "Meatandalternatives", "Dairyandalternatives", "Sodium", "Sugar" -> 10
                else -> 5
            }

            // Display the slider for the food category
            FoodCategorySlider(category, score, maxScore)
        }

        Spacer(modifier = Modifier.height(2.dp)) // Space before total score

        // Show total score label
        Text(
            text = "Total Food Quality Score",
            fontSize = 14.sp
        )

        // Show the numeric total score with conditional color
        Text(
            text = "$totalScore/100",
            fontSize = 20.sp,
            color = if (totalScore >= 50) Color.Green else Color.Red
        )
        // New total score slider
        TotalScoreSlider(totalScore)

        Spacer(modifier = Modifier.height(5.dp)) // Space before buttons

        // Row for Share and Improve buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly // Even spacing between buttons
        ) {
            // Share button
            Button(
                onClick = {
                    // Generate report string
                    val report = buildReport(categoryScores, totalScore)

                    // Create an intent to share the report
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, report)
                    }

                    // Start activity to share via available apps
                    context.startActivity(Intent.createChooser(shareIntent, "Share score via"))
                },
                modifier = Modifier
                    .weight(1f) // Each button takes equal space
                    .padding(horizontal = 4.dp)
            ) {
                Text(text = "Share", fontSize = 12.sp)
            }

            // Improve button (shows a toast for now)
            Button(
                onClick = {
                    Toast.makeText(context, "Improve my diet clicked", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(text = "Improve", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Final space at the bottom
    }
}



/**
 * Displays a labeled slider representing a food category score.
 *
 * @param category   The name of the food category (e.g., "Fruits", "Dairy").
 * @param score      The current score value for the category.
 * @param maxScore   The maximum possible score for this category.
 */
@Composable
fun FoodCategorySlider(category: String, score: Float, maxScore: Int) {
    // Container that lays out the content vertically with padding
    Column(
        modifier = Modifier
            .fillMaxWidth()                 // Takes full horizontal width
            .padding(horizontal = 8.dp)     // Horizontal padding on both sides
    ) {
        // First row: displays category name and score text
        Row(
            modifier = Modifier.fillMaxWidth(),                    // Row takes full width
            horizontalArrangement = Arrangement.SpaceBetween       // Space between category and score
        ) {
            // Display the category name (e.g., "Fruits")
            Text(
                text = category,
                fontSize = 12.sp
            )
            // Display the score as "X/maxScore" in gray color
            Text(
                text = "${score.toInt()}/$maxScore",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Slider representing the category score
        Slider(
            value = score,                              // Current score value
            onValueChange = {},                         // read-only slider
            valueRange = 0f..maxScore.toFloat(),        // Range from 0 to maxScore
            steps = maxScore,                           // Number of tick steps
            modifier = Modifier
                .fillMaxWidth()                         // Slider spans full width
                .height(24.dp)                          // Slider height
        )
    }
}

/**
 * A horizontal slider showing the user's total food quality score out of 100.
 */
@Composable
fun TotalScoreSlider(totalScore: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Score", fontSize = 12.sp)
            Text("${totalScore.toInt()}/100", fontSize = 12.sp, color = Color.Gray)
        }

        Slider(
            value = totalScore,
            onValueChange = {},
            valueRange = 0f..100f,
            steps = 100,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
    }
}


/**
 * Builds a readable text report of the food quality scores.
 */
fun buildReport(scores: Map<String, Float>, totalScore: Float): String {
    val builder = StringBuilder()
    builder.append("\uD83C\uDF7D️ NutriTrack Food Quality Report\n")
    builder.append("===============================\n\n")

    scores.forEach { (category, score) ->
        val max = when (category) {
            "Discretionary", "Meatandalternatives", "Dairyandalternatives", "Sodium", "Sugar" -> 10
            else -> 5
        }
        builder.append("$category: ${score.toInt()} / $max\n")
    }

    builder.append("\n\uD83C\uDF1F Total Food Quality Score: ${totalScore.toInt()} / 100\n")
    builder.append("\n\uD83D\uDCD8 This report reflects your food intake breakdown based on your preferences.\n")
    builder.append("Keep striving for balanced nutrition! \uD83D\uDCAA")

    return builder.toString()
}

