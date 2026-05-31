package com.tarantino.xonarx.presentation.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tarantino.xonarx.domain.model.Bookmark

@Composable
fun NewTabDashboard(
    favorites: List<Bookmark>,
    onFavoriteClick: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    onQrScanClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onRecentTabsClick: () -> Unit,
    onNotesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A premium dashboard with a subtle gradient background or image
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Branding or Search Prompt
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Where to next?",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Quick Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionItem(icon = Icons.Default.Mic, label = "Voice Search", onClick = onVoiceSearchClick, modifier = Modifier.weight(1f))
                QuickActionItem(icon = Icons.Default.QrCodeScanner, label = "QR Scan", onClick = onQrScanClick, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionItem(icon = Icons.Default.Folder, label = "New Group", onClick = onNewGroupClick, modifier = Modifier.weight(1f))
                QuickActionItem(icon = Icons.Default.History, label = "Recent Tabs", onClick = onRecentTabsClick, modifier = Modifier.weight(1f))
                QuickActionItem(icon = Icons.Default.Edit, label = "Notes", onClick = onNotesClick, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Favorites Tiles
            if (favorites.isNotEmpty()) {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(favorites.size) { index ->
                        val fav = favorites[index]
                        FavoriteTile(bookmark = fav, onClick = { onFavoriteClick(fav.url) })
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Text("No favorites yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.height(64.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FavoriteTile(
    bookmark: Bookmark,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bookmark.title.take(1).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = bookmark.title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
