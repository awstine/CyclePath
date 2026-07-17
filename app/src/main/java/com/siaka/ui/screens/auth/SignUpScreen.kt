package com.siaka.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onTermsClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignUpSuccess()
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

            // Title
            Text(
                text = "Join the Peloton",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = PrimaryDark
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sign up to start your cycling journey.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Input Fields
            AuthTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
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

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Terms & Conditions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = Color.LightGray
                    )
                )
                
                val annotatedTerms = buildAnnotatedString {
                    append("I agree to the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Primary)) {
                        append("Terms and Conditions")
                    }
                }
                
                Text(
                    text = annotatedTerms,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    modifier = Modifier.clickable { onTermsClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Create Account Button
            Button(
                onClick = { 
                    if (password == confirmPassword) {
                        viewModel.signUp(email, password, fullName)
                    } else {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = uiState !is AuthUiState.Loading && agreeToTerms,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Create Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            AuthDivider(text = "or register with")

            // Social Register
            SocialAuthButton(
                text = "Google",
                onClick = { /* TODO: Google Register */ }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Footer
            val annotatedString = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Primary)) {
                    append("Log In")
                }
            }
            
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onLoginClick() }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SiakaTheme {
        SignUpScreen(
            onSignUpSuccess = {},
            onLoginClick = {},
            onTermsClick = {}
        )
    }
}
