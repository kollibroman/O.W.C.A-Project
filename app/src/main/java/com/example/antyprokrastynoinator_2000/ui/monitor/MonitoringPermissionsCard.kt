package com.example.antyprokrastynoinator_2000.ui.monitor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Karta z prośbą o uprawnienia potrzebne do blokowania innych aplikacji.
@Composable
fun MonitoringPermissionsCard(
    hasUsageAccess: Boolean,
    canDrawOverlays: Boolean,
    onGrantUsageAccess: () -> Unit,
    onGrantOverlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Blokowanie rozpraszaczy jest wyłączone",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Aby w trakcie zadania blokować inne aplikacje, nadaj uprawnienia:",
                color = Color.Black
            )
            val buttonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008575))
            if (!hasUsageAccess) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onGrantUsageAccess,
                    colors = buttonColors,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Dostęp do danych o użyciu") }
            }
            if (!canDrawOverlays) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onGrantOverlay,
                    colors = buttonColors,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Wyświetlanie nad innymi aplikacjami") }
            }
        }
    }
}
