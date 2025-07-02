package com.yucsan.mapgendafernandochang2025.screen.onLine.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun JustifiedFilterChips(
    chips: List<String>,
    seleccionadas: SnapshotStateList<String>,
    conteo: Map<String, Int>,
    color: Color,
    onClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val chipWidth = 108.dp // Ajusta según el tamaño estimado de tus chips

    val chipsPerRow = (screenWidth / chipWidth).coerceAtLeast(1f).toInt()

    val rows = chips.chunked(chipsPerRow)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowChips ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowChips.forEach { subcategoria ->
                    FilterChip(
                        selected = seleccionadas.contains(subcategoria),
                        onClick = { onClick(subcategoria) },
                        label = {
                            Text("$subcategoria (${conteo[subcategoria] ?: 0})")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            containerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, color)
                    )
                }
                repeat(chipsPerRow - rowChips.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
