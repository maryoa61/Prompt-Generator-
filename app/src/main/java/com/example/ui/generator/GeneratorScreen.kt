package com.example.ui.generator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.PromptStyle
import com.example.ui.theme.DarkNavyContainer
import com.example.ui.theme.DarkNavySubtext
import com.example.ui.theme.DarkNavyText
import com.example.ui.theme.M3Outline
import com.example.ui.theme.M3OutlineVariant
import com.example.ui.theme.OnSecondaryPill
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SecondaryPill

@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Prompt Builder Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Prompt Builder",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PRODUCTION V1.0.4",
                            style = MaterialTheme.typography.labelSmall,
                            color = PurplePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Style Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("style_selector_row")
            ) {
                items(PromptStyle.entries) { style ->
                    val isSelected = state.selectedStyle == style
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) PurpleContainer else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PurplePrimary.copy(alpha = 0.4f) else M3OutlineVariant,
                                shape = CircleShape
                            )
                            .clickable { viewModel.onStyleSelected(style) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("style_chip_${style.name}")
                    ) {
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) PurpleOnContainer else OnSecondaryPill
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Source Input Box
            OutlinedTextField(
                value = state.inputText,
                onValueChange = { viewModel.onInputChanged(it) },
                label = {
                    Text(
                        text = "Source Keywords & Requirements",
                        style = MaterialTheme.typography.labelLarge,
                        color = PurplePrimary
                    )
                },
                placeholder = { Text("Paste technical requirements, ideas, or topics...") },
                trailingIcon = {
                    if (state.inputText.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onInputChanged("") },
                            modifier = Modifier.testTag("clear_input_button")
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear input")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("source_input_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = M3Outline,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Generate Button
            Button(
                onClick = { viewModel.generatePrompt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_prompt_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERATE PROMPT",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generated Output Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("output_preview_card")
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 18.dp,
                            top = 18.dp,
                            end = 18.dp,
                            bottom = 24.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OUTPUT PREVIEW",
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkNavyText
                            )
                            Row {
                                IconButton(
                                    onClick = { viewModel.savePromptToHistory() },
                                    modifier = Modifier.testTag("save_prompt_button")
                                ) {
                                    Icon(
                                        imageVector = if (state.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Save prompt",
                                        tint = DarkNavyText
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.copyToClipboard(context) },
                                    modifier = Modifier.testTag("copy_prompt_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy prompt",
                                        tint = DarkNavyText
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.sharePrompt(context) },
                                    modifier = Modifier.testTag("share_prompt_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share prompt",
                                        tint = DarkNavyText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutputSection(label = "ROLE", content = state.generatedRole)
                        OutputSection(label = "TASK", content = state.generatedTask)
                        OutputSection(label = "CONTEXT", content = state.generatedContext)
                        OutputSection(label = "CONSTRAINTS", content = state.generatedConstraints)
                        OutputSection(label = "OUTPUT FORMAT", content = state.generatedOutputFormat)

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Floating Action Button overlaid at bottom-end of prompt display area
                FloatingActionButton(
                    onClick = { viewModel.copyToClipboard(context) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .testTag("copy_prompt_fab"),
                    containerColor = PurplePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy prompt to clipboard",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy Prompt",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Preset Samples Header
            Text(
                text = "TRY A SAMPLE PRESET",
                style = MaterialTheme.typography.labelMedium,
                color = M3Outline,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Preset Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                item {
                    SampleChip("Android Architecture", PromptStyle.SOFTWARE_DEV) {
                        viewModel.loadSamplePreset(
                            "Kotlin, Jetpack Compose, Clean Architecture, Repository Pattern, Room DB, Hilt DI",
                            PromptStyle.SOFTWARE_DEV
                        )
                    }
                }
                item {
                    SampleChip("Sci-Fi Story Plot", PromptStyle.CREATIVE_WRITING) {
                        viewModel.loadSamplePreset(
                            "Cyberpunk metropolis, rogue AI consciousness, memory trader, atmospheric neon noir",
                            PromptStyle.CREATIVE_WRITING
                        )
                    }
                }
                item {
                    SampleChip("SaaS Landing Page Copy", PromptStyle.BUSINESS_MARKETING) {
                        viewModel.loadSamplePreset(
                            "AI productivity tool for busy developers, high conversion headline, features breakdown, CTA",
                            PromptStyle.BUSINESS_MARKETING
                        )
                    }
                }
                item {
                    SampleChip("SQL Query Optimization", PromptStyle.DATA_ANALYSIS) {
                        viewModel.loadSamplePreset(
                            "PostgreSQL query performance tuning, index strategies, execution plan analysis, 1M rows",
                            PromptStyle.DATA_ANALYSIS
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun OutputSection(label: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelLarge,
                color = DarkNavySubtext.copy(alpha = 0.6f)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkNavyText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SampleChip(
    title: String,
    style: PromptStyle,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SecondaryPill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = OnSecondaryPill
        )
    }
}
