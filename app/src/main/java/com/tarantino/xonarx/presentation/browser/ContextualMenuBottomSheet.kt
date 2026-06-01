package com.tarantino.xonarx.presentation.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextualMenuBottomSheet(
    target: ContextualActionTarget,
    onDismissRequest: () -> Unit,
    onActionClick: (ContextualAction) -> Unit
) {
    val actions = buildActionsForTarget(target)
    
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = getTitleForTarget(target),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Divider(modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn {
                items(actions) { action ->
                    ListItem(
                        headlineContent = { Text(action.label) },
                        leadingContent = { Icon(action.icon, contentDescription = action.label) },
                        modifier = Modifier.clickable {
                            onActionClick(action)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

private fun getTitleForTarget(target: ContextualActionTarget): String {
    return when (target) {
        is ContextualActionTarget.Link -> target.url
        is ContextualActionTarget.Image -> target.imageUrl
        is ContextualActionTarget.ImageLink -> target.url
        is ContextualActionTarget.Text -> target.text
    }
}

sealed class ContextualAction(val label: String, val icon: ImageVector) {
    object OpenInNewTab : ContextualAction("Open in new tab", Icons.Default.OpenInNew)
    object OpenInNewTabInGroup : ContextualAction("Open in new tab in group", Icons.Default.LibraryAdd)
    object CopyLink : ContextualAction("Copy link", Icons.Default.ContentCopy)
    object ShareLink : ContextualAction("Share link", Icons.Default.Share)
    object DownloadLink : ContextualAction("Download link", Icons.Default.Download)
    object OpenInExternalApp : ContextualAction("Open in external app", Icons.Default.OpenInBrowser)
    object Incognito : ContextualAction("Open in incognito tab", Icons.Default.Security)
    object AddToFavorites : ContextualAction("Add to favorites", Icons.Default.FavoriteBorder)

    object OpenImageInNewTab : ContextualAction("Open image in new tab", Icons.Default.Image)
    object SaveImage : ContextualAction("Save image", Icons.Default.Download)
    object CopyImageAddress : ContextualAction("Copy image address", Icons.Default.ContentCopy)
    object SearchImageWithLens : ContextualAction("Search image with Google Lens", Icons.Default.Search)
    object ShareImage : ContextualAction("Share image", Icons.Default.Share)
    
    // Additional if needed:
    object TranslatePage : ContextualAction("Translate page", Icons.Default.Translate)
}

private fun buildActionsForTarget(target: ContextualActionTarget): List<ContextualAction> {
    return when (target) {
        is ContextualActionTarget.Link -> listOf(
            ContextualAction.OpenInNewTab,
            ContextualAction.OpenInNewTabInGroup,
            ContextualAction.Incognito,
            ContextualAction.CopyLink,
            ContextualAction.ShareLink,
            ContextualAction.DownloadLink,
            ContextualAction.OpenInExternalApp,
            ContextualAction.AddToFavorites
        )
        is ContextualActionTarget.Image -> listOf(
            ContextualAction.OpenImageInNewTab,
            ContextualAction.SaveImage,
            ContextualAction.CopyImageAddress,
            ContextualAction.SearchImageWithLens,
            ContextualAction.ShareImage
        )
        is ContextualActionTarget.ImageLink -> listOf(
            ContextualAction.OpenInNewTab,
            ContextualAction.OpenInNewTabInGroup,
            ContextualAction.Incognito,
            ContextualAction.OpenImageInNewTab,
            ContextualAction.CopyLink,
            ContextualAction.CopyImageAddress,
            ContextualAction.SaveImage,
            ContextualAction.ShareLink,
            ContextualAction.DownloadLink
        )
        is ContextualActionTarget.Text -> listOf(
            // Optional
        )
    }
}
