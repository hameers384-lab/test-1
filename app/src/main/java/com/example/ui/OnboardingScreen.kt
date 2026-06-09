package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.WaterViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }

    // Onboarding State
    var selectedGender by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }

    // Validation error states
    var ageError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }

    // Local validation
    fun validateStep1(): Boolean {
        if (selectedGender.isEmpty()) {
            return false
        }
        val age = ageStr.toIntOrNull()
        if (age == null || age !in 1..120) {
            ageError = "Please enter a valid age (1-120)"
            return false
        }
        ageError = null
        return true
    }

    fun validateStep2(): Boolean {
        val weight = weightStr.toDoubleOrNull()
        if (weight == null || weight !in 30.0..300.0) {
            weightError = "Please enter a valid weight (30-300 kg)"
            return false
        }
        weightError = null

        val height = heightStr.toDoubleOrNull()
        if (height == null || height !in 100.0..250.0) {
            heightError = "Please enter a valid height (100-250 cm)"
            return false
        }
        heightError = null
        return true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            // Background Water drop accent
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    WaterDropIcon(
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Set Up Hydration",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "We calculate your custom target to maximize stamina, energy, and cognitive focus.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step Indicator
                    Row(
                        modifier = Modifier.width(160.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(2) { idx ->
                            val active = idx + 1 <= step
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                // Inner content with Slide Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn() with
                                        slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn() with
                                        slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut()
                            }
                        },
                        label = "onboarding_step_transitions"
                    ) { currentStep ->
                        when (currentStep) {
                            1 -> StepOneContent(
                                selectedGender = selectedGender,
                                onGenderSelect = { selectedGender = it },
                                ageStr = ageStr,
                                onAgeChange = { ageStr = it },
                                ageError = ageError
                            )
                            2 -> StepTwoContent(
                                weightStr = weightStr,
                                onWeightChange = { weightStr = it },
                                weightError = weightError,
                                heightStr = heightStr,
                                onHeightChange = { heightStr = it },
                                heightError = heightError
                            )
                        }
                    }
                }

                // Bottom Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .padding(end = 8.dp)
                                .testTag("btn_back"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Back")
                        }
                    }

                    Button(
                        onClick = {
                            if (step == 1) {
                                if (validateStep1()) {
                                    step = 2
                                }
                            } else {
                                if (validateStep2()) {
                                    val age = ageStr.toIntOrNull() ?: 25
                                    val height = heightStr.toDoubleOrNull() ?: 170.0
                                    val weight = weightStr.toDoubleOrNull() ?: 70.0
                                    viewModel.saveOnboardingProfile(
                                        age = age,
                                        heightCm = height,
                                        weightKg = weight,
                                        gender = selectedGender
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .padding(start = if (step > 1) 8.dp else 0.dp)
                            .testTag("btn_next"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (step == 2) "Calculate Goal" else "Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (step == 2) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepOneContent(
    selectedGender: String,
    onGenderSelect: (String) -> Unit,
    ageStr: String,
    onAgeChange: (String) -> Unit,
    ageError: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Gender Selector Cards
        Text(
            text = "Select Gender",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val genders = listOf("Male", "Female", "Other")
            genders.forEach { gender ->
                val isSelected = selectedGender == gender
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clickable { onGenderSelect(gender) }
                        .testTag("gender_card_$gender"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (gender == "Male") "🙋‍♂️" else if (gender == "Female") "🙋‍♀️" else "✨",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gender,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Age Input
        OutlinedTextField(
            value = ageStr,
            onValueChange = {
                if (it.isEmpty() || (it.length <= 3 && it.all { char -> char.isDigit() })) {
                    onAgeChange(it)
                }
            },
            label = { Text("How old are you?") },
            prefix = { Text("Age ") },
            suffix = { Text("years") },
            placeholder = { Text("e.g. 28") },
            isError = ageError != null,
            supportingText = { ageError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_age")
        )
    }
}

@Composable
fun StepTwoContent(
    weightStr: String,
    onWeightChange: (String) -> Unit,
    weightError: String?,
    heightStr: String,
    onHeightChange: (String) -> Unit,
    heightError: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your measurements",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Weight Input
        OutlinedTextField(
            value = weightStr,
            onValueChange = { onWeightChange(it) },
            label = { Text("Weight") },
            prefix = { Text("Weight ") },
            suffix = { Text("kg") },
            placeholder = { Text("e.g. 72") },
            isError = weightError != null,
            supportingText = { weightError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_weight")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Height Input
        OutlinedTextField(
            value = heightStr,
            onValueChange = { onHeightChange(it) },
            label = { Text("Height") },
            prefix = { Text("Height ") },
            suffix = { Text("cm") },
            placeholder = { Text("e.g. 175") },
            isError = heightError != null,
            supportingText = { heightError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_height")
        )
    }
}
