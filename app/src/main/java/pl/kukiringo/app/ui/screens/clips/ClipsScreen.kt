package pl.kukiringo.app.ui.screens.clips

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.kukiringo.app.data.model.Clip
import pl.kukiringo.app.data.repository.AppContainer
import pl.kukiringo.app.ui.theme.KukirinBlack
import pl.kukiringo.app.ui.theme.KukirinGrey
import pl.kukiringo.app.ui.theme.KukirinOrange
import pl.kukiringo.app.ui.theme.KukirinOrangeDark
import pl.kukiringo.app.ui.theme.KukirinSurfaceHigh
import pl.kukiringo.app.ui.theme.KukirinWhite

/** kukirinTok — pionowy feed krótkich klipów z przejazdów. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClipsScreen() {
    val repository = AppContainer.repository
    val clips by repository.clips.collectAsState()
    val pagerState = rememberPagerState(pageCount = { clips.size })
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            ClipPage(
                clip = clips[page],
                index = page,
                onLike = { repository.toggleLikeClip(clips[page].id) },
            )
        }
        Text(
            text = "kukirinTok",
            style = MaterialTheme.typography.titleLarge,
            color = KukirinWhite,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
        )
        ExtendedFloatingActionButton(
            onClick = { Toast.makeText(context, "Nagrywanie klipów — wkrótce!", Toast.LENGTH_SHORT).show() },
            icon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
            text = { Text("Nagraj klip") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 12.dp),
        )
    }
}

private val clipBackgrounds = listOf(
    listOf(KukirinOrangeDark, KukirinBlack),
    listOf(KukirinSurfaceHigh, KukirinOrange),
    listOf(Color(0xFF3A1A08), KukirinBlack),
)

@Composable
private fun ClipPage(clip: Clip, index: Int, onLike: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(clipBackgrounds[index % clipBackgrounds.size])),
    ) {
        // Tu wejdzie odtwarzacz wideo (Media3 ExoPlayer).
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Odtwórz",
            tint = KukirinWhite.copy(alpha = 0.85f),
            modifier = Modifier.align(Alignment.Center).size(96.dp),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 96.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(clip.author, style = MaterialTheme.typography.titleMedium, color = KukirinWhite)
            Text(clip.caption, style = MaterialTheme.typography.bodyLarge, color = KukirinWhite)
            Text("📍 ${clip.location}", style = MaterialTheme.typography.bodySmall, color = KukirinGrey)
        }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionButton(
                icon = if (clip.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                label = "${clip.likes}",
                tint = if (clip.likedByMe) KukirinOrange else KukirinWhite,
                onClick = onLike,
            )
            ActionButton(icon = Icons.Filled.ChatBubble, label = "${clip.comments}", onClick = {})
            ActionButton(icon = Icons.Filled.Share, label = "Udostępnij", onClick = {})
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color = KukirinWhite) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(32.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = KukirinWhite)
    }
}
