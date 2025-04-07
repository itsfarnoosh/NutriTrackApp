/**
 * Assignment 1: NutriTrack App
 * Author: Farnoush Mehraban Ghezelhesar
 * Student Id: 33391629
 */

package com.example.a1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a1.ui.theme.A1Theme

/**
 * MainActivity is the launcher activity of the NutriTrack app.
 * It sets the content to show the WelcomeScreen.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets the UI content using Jetpack Compose and applies the app's theme
        setContent {
            A1Theme {
                // Show WelcomeScreen and handle login button click
                WelcomeScreen { navigateToLogin() }
            }
        }
    }

    //Navigates to the LoginActivity when the login button is clicked.
    private fun navigateToLogin() {
        // Creates an Intent to start LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
    }
}


/**
 * Composable function that displays the Welcome screen UI.
 *
 * @param onLoginClick A callback lambda triggered when the Login button is clicked.
 */
@Composable
fun WelcomeScreen(onLoginClick: () -> Unit) {

    val context = LocalContext.current

    // Create an AnnotatedString with a clickable URL
    val annotatedLinkString = buildAnnotatedString {
        val linkText = "https://www.monash.edu/medicine/scs/nutrition/clinics/nutrition"
        val tag = "URL"

        pushStringAnnotation(tag = tag, annotation = linkText)
        withStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
    }
    // Arranges the content vertically, centered both horizontally and vertically
    Column(
        modifier = Modifier.fillMaxSize()   // Occupies the full available size

            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),                // Adds padding around the content
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display NutriTrack logo
        Image(
            painter = painterResource(id = R.drawable.logo), // Loads the logo from resources
            contentDescription = "NutriTrack Logo",          // Accessible content description
            modifier = Modifier.size(100.dp)                 // Image size
        )

        Spacer(modifier = Modifier.height(16.dp))       // Adds space between elements

        // Display app name
        Text(
            text = "NutriTrack",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display disclaimer text
        Text(
            text = "This app provides general health and nutrition information for educational purposes only. It is not intended as medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional before making any changes to your diet, exercise, or health regimen.\n\nUse this app at your own risk.",
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )


        ClickableText(
            // The text to display, which includes an annotation for the URL
            text = annotatedLinkString,

            // Apply padding around the clickable text
            modifier = Modifier.padding(8.dp),

            // Callback when the user clicks on the text
            onClick = { offset ->

                // Get the annotations (like URLs) at the clicked offset in the string
                annotatedLinkString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    // If there's at least one matching annotation
                    .firstOrNull()?.let { stringAnnotation ->
                        // Create an Intent to open the URL in the browser
                        // a method that takes a string and converts it into a Uri object.
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(stringAnnotation.item))

                        // Start the activity using the current context
                        context.startActivity(intent)
                    }
            }
        )



        Spacer(modifier = Modifier.height(24.dp))

        // Login button, triggers navigation to LoginActivity
        Button(onClick = onLoginClick) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display student details
        Text(
            text = "Designed with Love️ by Farnoush Mehraban (33391629)",
            fontSize = 12.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}