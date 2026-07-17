package com.siaka.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siaka.ui.theme.DarkNavy
import com.siaka.ui.theme.Primary
import com.siaka.ui.theme.PrimaryDark
import com.siaka.ui.theme.SiakaTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        } else if (uiState is AuthUiState.Error) {
            Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Text(
                text = "Siaka",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = PrimaryDark,
                    letterSpacing = (-1).sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Text
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sign in to continue your journey",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Input Fields
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Forgot Password
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    ),
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { viewModel.login(email, password) },
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDark
                )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Log In",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            AuthDivider(text = "or sign in with")

            // Social Login
            SocialAuthButton(
                text = "Google",
                onClick = { /* TODO: Google Login */ }
            )

            //Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Register Footer
            val annotatedString = buildAnnotatedString {
                append("Don't have an account? ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Primary)) {
                    append("Register")
                }
            }
            
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onRegisterClick() }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SiakaTheme {
        LoginScreen(
            onLoginSuccess = {},
            onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}
