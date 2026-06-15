package com.project.voicetotask.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.InputField
import com.project.voicetotask.presentation.components.PrimaryButton
import com.project.voicetotask.ui.theme.VoiceToTaskTheme

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAcceptedTermsChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            RegisterHeader()
            Spacer(modifier = Modifier.height(32.dp))
            RegisterForm(
                uiState = uiState,
                onFullNameChange = onFullNameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onAcceptedTermsChange = onAcceptedTermsChange,
                onRegisterClick = onRegisterClick
            )
            Spacer(modifier = Modifier.height(32.dp))
            RegisterFooter(onLoginClick = onLoginClick)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RegisterHeader() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        // Image logo would go here
    }
    Spacer(modifier = Modifier.height(32.dp))
    Text(
        text = stringResource(id = R.string.create_account),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(id = R.string.register_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun RegisterForm(
    uiState: AuthUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAcceptedTermsChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    InputField(
        value = uiState.fullName,
        onValueChange = onFullNameChange,
        label = stringResource(id = R.string.full_name),
        leadingIcon = Icons.Default.Person
    )
    Spacer(modifier = Modifier.height(16.dp))
    InputField(
        value = uiState.email,
        onValueChange = onEmailChange,
        label = stringResource(id = R.string.email),
        leadingIcon = Icons.Default.Email
    )
    Spacer(modifier = Modifier.height(16.dp))
    InputField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        label = stringResource(id = R.string.password),
        leadingIcon = Icons.Default.Lock,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.CheckCircle else Icons.Filled.Lock
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = null)
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    InputField(
        value = uiState.confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = stringResource(id = R.string.confirm_password),
        leadingIcon = Icons.Default.Lock,
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (confirmPasswordVisible) Icons.Filled.CheckCircle else Icons.Filled.Lock
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(imageVector = image, contentDescription = null)
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = uiState.acceptedTerms,
            onCheckedChange = onAcceptedTermsChange
        )
        Text(
            text = stringResource(id = R.string.i_agree_to) + " ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = R.string.terms_and_policies),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { /* open policies */ }
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(
        text = stringResource(id = R.string.sign_up),
        onClick = onRegisterClick,
        enabled = uiState.fullName.isNotEmpty() &&
                uiState.email.isNotEmpty() &&
                uiState.password.isNotEmpty() &&
                uiState.password == uiState.confirmPassword &&
                uiState.acceptedTerms
    )
}

@Composable
private fun RegisterFooter(onLoginClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.already_have_account),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = " ${stringResource(id = R.string.nav_home)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onLoginClick)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    VoiceToTaskTheme {
        RegisterScreen(
            uiState = AuthUiState(),
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onAcceptedTermsChange = {},
            onRegisterClick = {},
            onLoginClick = {}
        )
    }
}
