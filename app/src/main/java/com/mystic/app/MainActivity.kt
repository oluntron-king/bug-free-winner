package com.mystic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private val Navy = Color(0xFF07111F)
private val Panel = Color(0xFF101E31)
private val ElectricBlue = Color(0xFF2D8CFF)
private val SoftText = Color(0xFF9DB0C8)
private val Bright = Color(0xFFF4F8FF)

private val supabase by lazy {
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}

@Serializable
data class MysticPost(val id: Long, val username: String, val body: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MysticScreen() }
    }
}

@Composable
private fun MysticScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Connect Supabase to start using MYSTIC.") }
    var signedIn by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var posts by remember { mutableStateOf<List<MysticPost>>(emptyList()) }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = ElectricBlue, background = Navy, surface = Panel,
        onBackground = Bright, onSurface = Bright
    )) {
        Surface(Modifier.fillMaxSize(), color = Navy) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(ElectricBlue, CircleShape), contentAlignment = Alignment.Center) {
                        Text("M", color = Bright, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("MYSTIC", color = Bright, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                        Text("YOUR WORLD. YOUR PEOPLE.", color = SoftText, fontSize = 10.sp, letterSpacing = 2.sp)
                    }
                }
                Spacer(Modifier.height(28.dp))
                if (!signedIn) {
                    Text("Find your people.", color = Bright, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Sign in or create your MYSTIC account.", color = SoftText)
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { email = it }, label = { Text("Email") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it }, label = { Text("Password") },
                        singleLine = true, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_PUBLISHABLE_KEY.isBlank()) {
                                status = "Add your Supabase URL and publishable key to local.properties."
                            } else if (email.isBlank() || password.length < 6) {
                                status = "Enter your email and a password with at least 6 characters."
                            } else {
                                busy = true
                                scope.launch {
                                    try {
                                        supabase.auth.signInWith(Email) {
                                            this.email = email.trim()
                                            this.password = password
                                        }
                                        signedIn = true
                                        status = "Signed in. Loading your feed…"
                                        try {
                                            posts = supabase.from("posts").select().decodeList<MysticPost>()
                                            status = if (posts.isEmpty()) "You're in! Your feed is empty for now." else "Feed loaded."
                                        } catch (e: Exception) {
                                            status = "Signed in, but feed could not load. Check the posts table and RLS."
                                        }
                                    } catch (e: Exception) {
                                        status = "Sign-in failed: ${e.message ?: "Check credentials and Supabase settings."}"
                                    } finally { busy = false }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !busy, shape = RoundedCornerShape(14.dp)
                    ) { Text(if (busy) "Please wait…" else "Sign in", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_PUBLISHABLE_KEY.isBlank()) {
                                status = "Add Supabase URL and publishable key to local.properties first."
                            } else if (email.isBlank() || password.length < 6) {
                                status = "Enter your email and a password with at least 6 characters."
                            } else {
                                busy = true
                                scope.launch {
                                    try {
                                        supabase.auth.signUpWith(Email) {
                                            this.email = email.trim()
                                            this.password = password
                                        }
                                        status = "Sign-up submitted. Verify your email if confirmation is enabled, then sign in."
                                    } catch (e: Exception) {
                                        status = "Sign-up failed: ${e.message ?: "Check Supabase settings."}"
                                    } finally { busy = false }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !busy, shape = RoundedCornerShape(14.dp)
                    ) { Text("Create account", color = Bright) }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Your feed", color = Bright, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("A little closer, every day.", color = SoftText)
                        }
                        TextButton(onClick = {
                            scope.launch {
                                try {
                                    supabase.auth.signOut()
                                    signedIn = false
                                    posts = emptyList()
                                    status = "You have signed out."
                                } catch (e: Exception) { status = "Sign-out failed." }
                            }
                        }) { Text("Sign out", color = ElectricBlue) }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (posts.isEmpty()) {
                        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(20.dp)) {
                            Text("Your feed starts here", color = Bright, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Add a row to the Supabase posts table, then tap refresh.", color = SoftText)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = {
                                busy = true
                                scope.launch {
                                    try {
                                        posts = supabase.from("posts").select().decodeList<MysticPost>()
                                        status = "Feed refreshed."
                                    } catch (e: Exception) { status = "Could not refresh feed. Check your table and RLS." }
                                    finally { busy = false }
                                }
                            }, enabled = !busy) { Text("Refresh feed") }
                        }
                    } else {
                        posts.forEach { post ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
                                Text("@${post.username}", color = ElectricBlue, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(post.body, color = Bright)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(status, color = SoftText, fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(12.dp))
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Icon(Icons.Default.Home, "Home", tint = ElectricBlue)
                    Icon(Icons.Default.Search, "Discover", tint = SoftText)
                    Icon(Icons.Default.Send, "Messages", tint = SoftText)
                    Icon(Icons.Default.Person, "Profile", tint = SoftText)
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color(0xFF31445D),
    focusedLabelColor = ElectricBlue, unfocusedLabelColor = SoftText,
    focusedTextColor = Bright, unfocusedTextColor = Bright, cursorColor = ElectricBlue
)
