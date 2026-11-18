package com.example.antyprokrastynoinator_2000.ui.monitor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlue
import com.example.antyprokrastynoinator_2000.ui.theme.IceBlueLight
import com.example.antyprokrastynoinator_2000.ui.theme.NavyPrimary

@Composable
fun MonitoringPermissionsCard(
    hasUsageAccess: Boolean,
    canDrawOverlays: Boolean,
    onGrantUsageAccess: () -> Unit,
    onGrantOverlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        border = BorderStroke(1.5.dp, NavyPrimary),
        colors = CardDefaults.cardColors(containerColor = IceBlueLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Blokowanie rozpraszaczy jest wyłączone",
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Aby w trakcie zadania blokować inne aplikacje, nadaj uprawnienia:",
                color = NavyPrimary
            )
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = NavyPrimary,
                contentColor = IceBlue
            )
            if (!hasUsageAccess) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onGrantUsageAccess,
                    colors = buttonColors,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Dostęp do danych o użyciu", fontWeight = FontWeight.Bold) }
            }
            if (!canDrawOverlays) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onGrantOverlay,
                    colors = buttonColors,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Wyświetlanie nad innymi aplikacjami", fontWeight = FontWeight.Bold) }
            }
        }
    }
}