package com.studyhub.presentation.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studyhub.presentation.navigation.Screen
import com.studyhub.presentation.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.large),
        verticalArrangement = Arrangement.Center
    ) {
        Text("StudyHub", style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary)
        Text("Selamat datang kembali",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.extraLarge))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(Spacing.normal))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton({ passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility, null
                    )
                }
            },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(Spacing.normal))
        AnimatedVisibility(visible = uiState.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    uiState.error ?: "",
                    modifier = Modifier.padding(Spacing.small),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(Spacing.large))
        Button(
            onClick = { viewModel.login(email, password) },
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading)
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            else Text("Masuk", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(Spacing.small))
        TextButton(
            onClick = { navController.navigate(Screen.Register.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Belum punya akun? Daftar sekarang")
        }
    }
}
