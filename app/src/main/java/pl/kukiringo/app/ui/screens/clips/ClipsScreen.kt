package pl.kukiringo.app.ui.screens.clips

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.kukiringo.app.data.model.Clip
import pl.kukiringo.app.data.repository.AppContainer
import pl.kukiringo.app.ui.theme.KukirinBlack
import pl.kukiringo.app.ui.theme.KukirinGrey
import pl.kukiringo.app.ui.theme.KukirinOrange
import pl.kukiringo.app.ui.theme.KukirinOrangeDark
import pl.kukiringo.app.ui.theme.KukirinSurfaceHigh
import pl.kukiringo.app.ui.theme.KukirinWhite
import java.io.File

/** kukiTok — pionowy feed krótkich klipów z przejazdów. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClipsScreen() {
    val repository = AppContainer.repository
    val clips by repository.clips.collectAsState()
    val pagerState = rememberPagerState(pageCount = { clips.size })
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }

    // Kopiuje film do pamięci aplikacji i dodaje go do feedu.
    fun importVideo(uri: Uri) {
        saving = true
        scope.launch {
            val file = withContext(Dispatchers.IO) { copyToClips(context, uri) }
            saving = false
            if (file != null) {
                repository.addClip(file)
                pagerState.scrollToPage(0)
            } else {
                Toast.makeText(context, "Nie udało się zapisać klipu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val recordLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) importVideo(uri)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) importVideo(uri)
    }

    Box(modifier = Modifier.fillMaxSize().background(KukirinBlack)) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val clip = clips[page]
            ClipPage(
                clip = clip,
                index = page,
                isActive = pagerState.currentPage == page,
                onLike = { repository.toggleLikeClip(clip.id) },
            )
        }
        Text(
            text = "kukiTok",
            style = MaterialTheme.typography.titleLarge,
            color = KukirinWhite,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 14.dp),
        )
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallFloatingActionButton(
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                },
                containerColor = KukirinSurfaceHigh,
                contentColor = KukirinWhite,
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = "Dodaj z galerii")
            }
            ExtendedFloatingActionButton(
                onClick = {
                    val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        .putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)
                    try {
                        recordLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Brak aplikacji aparatu", Toast.LENGTH_SHORT).show()
                    }
                },
                icon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
                text = { Text("Nagraj klip") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }
        if (saving) {
            CircularProgressIndicator(
                color = KukirinOrange,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

private fun copyToClips(context: Context, uri: Uri): File? = try {
    val dir = File(context.filesDir, "clips").apply { mkdirs() }
    val file = File(dir, "klip_${System.currentTimeMillis()}.mp4")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    if (file.length() > 0) file else null.also { file.delete() }
} catch (e: Exception) {
    null
}

private val clipBackgrounds = listOf(
    listOf(KukirinOrangeDark, KukirinBlack),
    listOf(KukirinSurfaceHigh, KukirinOrange),
    listOf(Color(0xFF3A1A08), KukirinBlack),
)

@Composable
private fun ClipPage(clip: Clip, index: Int, isActive: Boolean, onLike: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(clipBackgrounds[index % clipBackgrounds.size])),
    ) {
        if (clip.videoPath != null) {
            ClipVideo(path = clip.videoPath, playing = isActive, modifier = Modifier.align(Alignment.Center).fillMaxSize())
        } else {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = KukirinWhite.copy(alpha = 0.85f),
                modifier = Modifier.align(Alignment.Center).size(96.dp),
            )
        }
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

/** Odtwarzacz klipu (zapętlony); gra tylko klip widoczny na ekranie. */
@Composable
private fun ClipVideo(path: String, playing: Boolean, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoPath(path)
                setOnPreparedListener { player -> player.isLooping = true }
            }
        },
        update = { view -> if (playing) view.start() else view.pause() },
        onRelease = { view -> view.stopPlayback() },
    )
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
