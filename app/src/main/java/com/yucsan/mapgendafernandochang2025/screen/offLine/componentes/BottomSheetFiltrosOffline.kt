package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetFiltrosOffline(
    sheetState: SheetState,
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
    modifier: Modifier = Modifier
) {
    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = sheetState
        ),
        sheetPeekHeight = 35.dp,
        sheetContainerColor = Color.Transparent,
        containerColor = Color.Transparent,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.DarkGray,
                height = 6.dp,
                width = 40.dp,
                shape = RoundedCornerShape(50)
            )
        },
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.DarkGray.copy(alpha = 0.9f))
                        .zIndex(-1f)
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable {
                                scope.launch {
                                    if (sheetState.currentValue == SheetValue.PartiallyExpanded) {
                                        sheetState.expand()
                                    } else {
                                        sheetState.partialExpand()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (sheetState.currentValue == SheetValue.PartiallyExpanded)
                                Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir o Colapsar"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔽 Aquí puedes reutilizar el contenido de filtros cuando esté disponible
                    // Ejemplo:
                    // MiniFiltroBottomSheetContent(
                    //     lugarRutaOfflineViewModel = lugarRutaOfflineViewModel,
                    //     onFiltroAplicado = {
                    //         scope.launch { sheetState.partialExpand() }
                    //     }
                    // )
                }
            }
        },
        modifier = modifier
    ) {
        // contenido opcional dentro del scaffold (por ahora vacío)
    }
}
