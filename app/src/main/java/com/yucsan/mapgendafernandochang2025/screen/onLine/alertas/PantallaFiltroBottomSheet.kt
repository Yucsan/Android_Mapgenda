package com.yucsan.mapgendafernandochang2025.screens.mapa.alertas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFiltroBottomSheet() {
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 60.dp,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable {
                            scope.launch {
                                if (sheetState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                                    sheetState.bottomSheetState.expand()
                                } else {
                                    sheetState.bottomSheetState.partialExpand()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (sheetState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded)
                            Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expandir o Colapsar"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Hola! Esto es un BottomSheet de prueba 👋")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Contenido principal de la pantalla")
        }
    }
}
