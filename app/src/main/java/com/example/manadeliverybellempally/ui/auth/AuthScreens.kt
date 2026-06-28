package com.example.manadeliverybellempally.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.R
import com.example.manadeliverybellempally.data.model.UserRole
import com.example.manadeliverybellempally.data.repository.AuthState
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaButton
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authState: AuthState,
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    var splashDone by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(2000)
        splashDone = true
    }
    
    LaunchedEffect(splashDone, authState) {
        if (splashDone) {
            when (authState) {
                is AuthState.Authenticated -> {
                    val role = viewModel.getRoleForUser(authState.uid)
                    onNavigateToHome(role)
                }
                else -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ManaRedStrong, ManaBgSecondary, ManaBgPrimary)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Mana Delivery Logo",
                modifier = Modifier
                    .size(260.dp)
                    .padding(16.dp)
            )
            
            Spacer(Modifier.height(12.dp))
            Text(
                "JUST CALL. WE DELIVER.",
                style = MaterialTheme.typography.labelMedium,
                color = ManaGold.copy(alpha = 0.8f),
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = ManaGold,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Composable
fun LoginScreen(
    authState: AuthState,
    onSendOtp: (String) -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) errorMessage = authState.message
    }

    Box(modifier = modifier.fillMaxSize().background(ManaBgPrimary)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Brush.verticalGradient(colors = listOf(ManaRedStrong.copy(alpha = 0.4f), Color.Transparent))))

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(60.dp))
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Brand Logo",
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(32.dp)).background(ManaRedStrong.copy(alpha = 0.2f)).border(1.dp, ManaBorder, RoundedCornerShape(32.dp))
            )

            Spacer(Modifier.height(32.dp))
            Text("Welcome Back", style = MaterialTheme.typography.headlineLarge, color = ManaTextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Login with your mobile number", style = MaterialTheme.typography.bodyLarge, color = ManaTextSecondary)

            Spacer(Modifier.height(48.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) { phoneNumber = it; errorMessage = null } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mobile Number") },
                prefix = { Text("+91  ", style = MaterialTheme.typography.bodyLarge, color = ManaGold, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Rounded.Phone, null, tint = ManaGold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (phoneNumber.length == 10) onSendOtp(phoneNumber) else errorMessage = "Please enter a valid 10-digit number" }),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, unfocusedBorderColor = ManaBorder, focusedContainerColor = ManaBgInput, unfocusedContainerColor = ManaBgInput, cursorColor = ManaGold, focusedLabelColor = ManaGold),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it, color = ManaError) } },
                enabled = !isLoading,
            )

            Spacer(Modifier.height(32.dp))
            ManaButton(
                text = if (isLoading) "Sending OTP..." else "Send OTP",
                onClick = { if (phoneNumber.length == 10) onSendOtp(phoneNumber) else errorMessage = "Please enter a valid 10-digit number" },
                enabled = phoneNumber.length == 10 && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Sms,
            )

            AnimatedVisibility(visible = authState is AuthState.Error) {
                if (authState is AuthState.Error) {
                    Spacer(Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ManaError.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = ManaError, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(authState.message, style = MaterialTheme.typography.bodyMedium, color = ManaError)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = ManaTextSecondary)
                TextButton(onClick = onNavigateToSignup) { Text("Sign Up", color = ManaGold, fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(32.dp))
            // Admin Access for Developer
            TextButton(
                onClick = { 
                    // This number will be recognized as ADMIN for easy access
                    onSendOtp("9999999999") 
                }
            ) {
                Text(
                    "LOGIN AS ADMIN", 
                    color = ManaGold.copy(alpha = 0.5f), 
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OtpScreen(
    phoneNumber: String,
    authState: AuthState,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var otpCode by remember { mutableStateOf("") }
    var resendTimer by remember { mutableIntStateOf(30) }
    val isLoading = authState is AuthState.Loading
    val errorMessage = if (authState is AuthState.Error) authState.message else null

    LaunchedEffect(resendTimer) { if (resendTimer > 0) { delay(1000); resendTimer-- } }

    Box(modifier = modifier.fillMaxSize().background(ManaBgPrimary)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = ManaTextPrimary) }
            }

            Spacer(Modifier.height(32.dp))
            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(ManaGold.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Lock, null, tint = ManaGold, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text("Verify OTP", style = MaterialTheme.typography.headlineMedium, color = ManaTextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Enter the 6-digit code sent to\n+91 $phoneNumber", style = MaterialTheme.typography.bodyLarge, color = ManaTextSecondary, textAlign = TextAlign.Center)

            Spacer(Modifier.height(40.dp))
            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { otpCode = it; if (it.length == 6) onVerifyOtp(it) } },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center, letterSpacing = 8.sp, color = ManaTextPrimary),
                placeholder = { Text("000000", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (otpCode.length == 6) onVerifyOtp(otpCode) }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, unfocusedBorderColor = ManaBorder, focusedContainerColor = ManaBgInput, unfocusedContainerColor = ManaBgInput)
            )

            Spacer(Modifier.height(24.dp))
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = ManaGold, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Verifying...", color = ManaTextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = ManaError.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = ManaError, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(errorMessage, style = MaterialTheme.typography.bodyMedium, color = ManaError)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            if (resendTimer > 0) {
                Text("Resend OTP in ${resendTimer}s", style = MaterialTheme.typography.bodyMedium, color = ManaTextTertiary)
            } else {
                TextButton(onClick = { resendTimer = 30; onResendOtp() }) {
                    Text("Resend OTP", style = MaterialTheme.typography.labelLarge, color = ManaGold)
                }
            }
        }
    }
}

@Composable
fun SignupScreen(
    authState: AuthState,
    onSignup: (name: String, role: UserRole, phone: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var storeName by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("") }
    val isLoading = authState is AuthState.Loading
    val errorMessage = if (authState is AuthState.Error) authState.message else null

    Box(modifier = modifier.fillMaxSize().background(ManaBgPrimary)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Mana Delivery Logo",
                modifier = Modifier.size(110.dp).clip(RoundedCornerShape(28.dp)).background(ManaRedStrong.copy(alpha = 0.2f)).border(1.5.dp, ManaBorder, RoundedCornerShape(28.dp))
            )

            Spacer(Modifier.height(32.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineLarge, color = ManaTextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Join ManaDelivery Bellempally", style = MaterialTheme.typography.bodyLarge, color = ManaTextSecondary)

            Spacer(Modifier.height(36.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Rounded.Person, null, tint = ManaGold) },
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, unfocusedBorderColor = ManaBorder, focusedContainerColor = ManaBgInput, unfocusedContainerColor = ManaBgInput, cursorColor = ManaGold, focusedLabelColor = ManaGold),
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mobile Number") },
                prefix = { Text("+91  ", color = ManaGold, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Rounded.Phone, null, tint = ManaGold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, unfocusedBorderColor = ManaBorder, focusedContainerColor = ManaBgInput, unfocusedContainerColor = ManaBgInput, cursorColor = ManaGold, focusedLabelColor = ManaGold),
            )

            Spacer(Modifier.height(24.dp))
            Text("SELECT YOUR ROLE", style = MaterialTheme.typography.labelLarge, color = ManaTextSecondary, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(12.dp))

            val roles = listOf(
                Triple(UserRole.CUSTOMER, "Customer", Icons.Rounded.ShoppingCart),
                Triple(UserRole.VENDOR, "Vendor / Store", Icons.Rounded.Store),
                Triple(UserRole.RIDER, "Delivery Rider", Icons.Rounded.DeliveryDining),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                roles.forEach { (role, label, icon) ->
                    val isSelected = selectedRole == role
                    val roleColor = when (role) {
                        UserRole.CUSTOMER -> RoleCustomer
                        UserRole.VENDOR -> RoleVendor
                        UserRole.RIDER -> RoleRider
                        UserRole.ADMIN -> RoleAdmin
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).border(1.dp, if (isSelected) roleColor else ManaBorder, RoundedCornerShape(20.dp)).background(if (isSelected) roleColor.copy(alpha = 0.08f) else ManaBgInput).padding(16.dp).clickable { selectedRole = role },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = isSelected, onClick = { selectedRole = role }, colors = RadioButtonDefaults.colors(selectedColor = roleColor, unselectedColor = ManaTextTertiary))
                        Spacer(Modifier.width(12.dp))
                        Icon(icon, null, tint = if (isSelected) roleColor else ManaTextSecondary)
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) ManaTextPrimary else ManaTextSecondary)
                    }
                }
            }

            AnimatedVisibility(visible = selectedRole == UserRole.VENDOR) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Store Name") },
                        leadingIcon = { Icon(Icons.Rounded.Storefront, null, tint = RoleVendor) },
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoleVendor, unfocusedBorderColor = ManaBorder, focusedContainerColor = ManaBgInput, unfocusedContainerColor = ManaBgInput, cursorColor = RoleVendor, focusedLabelColor = RoleVendor),
                    )
                }
            }

            AnimatedVisibility(visible = selectedRole == UserRole.RIDER) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = { vehicleType = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Vehicle Type (Bike/Cycle/Auto)") },
                        leadingIcon = { Icon(Icons.Rounded.TwoWheeler, null, tint = RoleRider) },
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoleRider, unfocusedBorderColor = ManaBorder, focusedContainerColor = ManaBgInput, unfocusedContainerColor = ManaBgInput, cursorColor = RoleRider, focusedLabelColor = RoleRider),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = ManaError.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = ManaError, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(errorMessage, style = MaterialTheme.typography.bodyMedium, color = ManaError)
                        }
                    }
                }
            }

            ManaButton(
                text = if (isLoading) "Sending OTP..." else "Create Account",
                onClick = { onSignup(name, selectedRole, phone) },
                enabled = name.isNotBlank() && phone.length == 10 && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.PersonAdd,
            )

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = ManaTextSecondary)
                TextButton(onClick = onNavigateToLogin) { Text("Log In", color = ManaGold, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
