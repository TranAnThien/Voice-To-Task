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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.InputField
import com.project.voicetotask.presentation.components.PrimaryButton
import com.project.voicetotask.ui.theme.VoiceToTaskTheme

@Composable
fun ForgotPasswordScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    onContactSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            ForgotPasswordTopBar(onBackToLoginClick = onBackToLoginClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            ForgotPasswordHeader()
            Spacer(modifier = Modifier.height(32.dp))
            ForgotPasswordForm(
                uiState = uiState,
                onEmailChange = onEmailChange,
                onSubmitClick = onSubmitClick
            )
            Spacer(modifier = Modifier.weight(1f))
            ForgotPasswordFooter(
                onBackToLoginClick = onBackToLoginClick,
                onContactSupportClick = onContactSupportClick
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordTopBar(onBackToLoginClick: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackToLoginClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}

@Composable
private fun ForgotPasswordHeader() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(id = R.string.forgot_password_title),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(id = R.string.forgot_password_desc),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ForgotPasswordForm(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    InputField(
        value = uiState.email,
        onValueChange = onEmailChange,
        label = stringResource(id = R.string.email),
        leadingIcon = Icons.Default.Email
    )
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(
        text = stringResource(id = R.string.send_request),
        onClick = onSubmitClick,
        enabled = uiState.email.isNotEmpty()
    )
}

@Composable
private fun ForgotPasswordFooter(
    onBackToLoginClick: () -> Unit,
    onContactSupportClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.back_to_login),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBackToLoginClick)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.having_trouble) + " ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.contact_support),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onContactSupportClick)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    VoiceToTaskTheme {
        ForgotPasswordScreen(
            uiState = AuthUiState(),
            onEmailChange = {},
            onSubmitClick = {},
            onBackToLoginClick = {},
            onContactSupportClick = {}
        )
    }
}
