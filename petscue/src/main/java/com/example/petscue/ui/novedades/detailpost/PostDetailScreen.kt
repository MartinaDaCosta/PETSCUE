@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.petscue.ui.novedades.detailpost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

private val BluePrimary = Color(0xFF4A90E2)
private val BlueSoft    = Color(0xFFEAF3FF)

@Composable
fun PostDetailScreen(
    onBack    : () -> Unit,
    viewModel : PostDetailViewModel = hiltViewModel()
) {
    val uiState          by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier       = Modifier
            .fillMaxSize()
            .background(BlueSoft)
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = BlueSoft,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint               = BluePrimary
                        )
                    }
                    Text(
                        text       = "Publicación",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        bottomBar = {
            ReplyComposer(
                text          = uiState.replyText,
                replyingTo    = uiState.replyingTo,
                isSending     = uiState.isSending,
                onTextChange  = viewModel::updateReplyText,
                onCancelReply = { viewModel.setReplyingTo(null) },
                onSend        = { viewModel.sendReply() }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier          = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment  = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }

            uiState.post == null -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontró la publicación")
                }
            }

            else -> {
                val rootReplies     = uiState.replies.filter { it.parentReplyId == null }
                val groupedChildren = uiState.replies
                    .filter { it.parentReplyId != null }
                    .groupBy { it.parentReplyId }

                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding      = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        PostDetailCard(post = uiState.post!!)
                    }

                    item {
                        Text(
                            text       = "Respuestas",
                            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = BluePrimary
                        )
                    }

                    if (rootReplies.isEmpty()) {
                        item {
                            Text(
                                text     = "Todavía no hay respuestas. Sé la primera persona en comentar.",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(rootReplies, key = { it.id }) { reply ->
                            ReplyThreadItem(
                                reply         = reply,
                                childReplies  = groupedChildren[reply.id].orEmpty(),
                                currentUserId = uiState.currentUserId,
                                onReplyClick  = { selectedReply ->
                                    viewModel.setReplyingTo(selectedReply)
                                },
                                onDeleteReply = { selectedReply ->
                                    viewModel.deleteReply(selectedReply)
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}