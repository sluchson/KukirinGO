package pl.kukiringo.app.ui.screens.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import pl.kukiringo.app.data.model.Kukistop
import pl.kukiringo.app.data.repository.AppContainer
import pl.kukiringo.app.ui.components.KukiCard
import pl.kukiringo.app.ui.components.ScreenHeader
import pl.kukiringo.app.ui.theme.KukirinBlack
import pl.kukiringo.app.ui.theme.KukirinOrange
import pl.kukiringo.app.ui.theme.KukirinOutline
import pl.kukiringo.app.ui.theme.KukirinSurface
import pl.kukiringo.app.ui.theme.KukirinWhite

@Composable
fun MapScreen() {
    val kukistops by AppContainer.repository.kukistops.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(eyebrow = "Mapa", title = "Kukistopy w okolicy") }
        item { MapPlaceholder() }
        item {
            Text(
                text = "Podgląd mapy — wkrótce prawdziwa mapa Rzeszowa z Twoją pozycją.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(kukistops, key = { it.id }) { stop -> KukistopRow(stop) }
    }
}

/** Rysowana „mapa” do czasu podpięcia OpenStreetMap (osmdroid). */
@Composable
private fun MapPlaceholder() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(KukirinSurface),
    ) {
        val w = size.width
        val h = size.height
        for (i in 1..5) {
            val y = h * i / 6f
            drawLine(KukirinOutline, Offset(0f, y), Offset(w, y - h * 0.08f), strokeWidth = 6f)
        }
        for (i in 1..4) {
            val x = w * i / 5f
            drawLine(KukirinOutline, Offset(x, 0f), Offset(x + w * 0.05f, h), strokeWidth = 6f)
        }
        val route = Path().apply {
            moveTo(w * 0.08f, h * 0.85f)
            cubicTo(w * 0.25f, h * 0.55f, w * 0.35f, h * 0.75f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.62f, h * 0.3f, w * 0.8f, h * 0.45f, w * 0.92f, h * 0.15f)
        }
        drawPath(route, color = KukirinOrange, style = Stroke(width = 12f, cap = StrokeCap.Round))
        listOf(
            Offset(w * 0.5f, h * 0.5f),
            Offset(w * 0.92f, h * 0.15f),
            Offset(w * 0.28f, h * 0.28f),
            Offset(w * 0.72f, h * 0.78f),
        ).forEach { point ->
            drawCircle(KukirinOrange, radius = 20f, center = point)
            drawCircle(KukirinBlack, radius = 8f, center = point)
        }
        drawCircle(KukirinWhite, radius = 16f, center = Offset(w * 0.08f, h * 0.85f))
    }
}

@Composable
private fun KukistopRow(stop: Kukistop) {
    KukiCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stop.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    stop.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("%.1f km".format(stop.distanceKm), style = MaterialTheme.typography.labelLarge)
                if (stop.visited) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "zaliczony",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
