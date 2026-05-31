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
import com.tarantino.xonarx.domain.model.HistoryItem
import com.tarantino.xonarx.presentation.theme.futuristic.AnimatedGradientBackdrop
import com.tarantino.xonarx.presentation.theme.futuristic.DepthCard
import com.tarantino.xonarx.presentation.theme.futuristic.FrostedGlassSurface
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@Composable
fun NewTabDashboard(
    favorites: List<Bookmark>,
    frequentlyVisited: List<HistoryItem>,
    onFavoriteClick: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    onQrScanClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onRecentTabsClick: () -> Unit,
    onNotesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // fallback
    ) {
        AnimatedGradientBackdrop(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
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

            Spacer(modifier = Modifier.height(32.dp))

            // Frequently Visited
            if (frequentlyVisited.isNotEmpty()) {
                Text(
                    text = "Frequently Visited",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    items(frequentlyVisited) { historyItem ->
                        FrequentSiteTile(historyItem = historyItem, onClick = { onFavoriteClick(historyItem.url) })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Favorites Tiles
            if (favorites.isNotEmpty()) {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
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
fun FrequentSiteTile(
    historyItem: HistoryItem,
    onClick: () -> Unit
) {
    DepthCard(onClick = onClick, modifier = Modifier.width(80.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            FrostedGlassSurface(
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                blurRadius = 16.dp,
                modifier = Modifier.size(56.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (historyItem.title?.take(1) ?: historyItem.url.replace("https://","").replace("http://","").take(1)).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = historyItem.title ?: historyItem.url.replace("https://","").replace("http://","").substringBefore("/"),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground
            )
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
    DepthCard(onClick = onClick, modifier = modifier) {
        FrostedGlassSurface(
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            blurRadius = 16.dp,
            modifier = Modifier.fillMaxWidth().height(72.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun FavoriteTile(
    bookmark: Bookmark,
    onClick: () -> Unit
) {
    DepthCard(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FrostedGlassSurface(
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                blurRadius = 24.dp,
                modifier = Modifier.size(64.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = bookmark.title.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = bookmark.title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
