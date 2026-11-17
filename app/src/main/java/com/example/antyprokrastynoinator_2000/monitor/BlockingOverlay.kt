package com.example.antyprokrastynoinator_2000.monitor

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.antyprokrastynoinator_2000.model.Task
import java.time.format.DateTimeFormatter

// Pełnoekranowe okno (jak alarm) rysowane nad innymi aplikacjami.
// Przechwytuje dotyk, więc aplikacji pod spodem nie da się używać.
class BlockingOverlay(
    private val context: Context,
    private val onReturnToTask: () -> Unit
) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val currentTask = mutableStateOf<Task?>(null)
    private var view: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    val isShowing: Boolean get() = view != null

    fun show(task: Task) {
        currentTask.value = task
        if (view != null) return

        val owner = OverlayLifecycleOwner().also { it.start() }
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                currentTask.value?.let { BlockingScreen(it, onReturnToTask) }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(composeView, params)
        view = composeView
        lifecycleOwner = owner
    }

    fun hide() {
        view?.let { windowManager.removeView(it) }
        lifecycleOwner?.destroy()
        view = null
        lifecycleOwner = null
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
private fun BlockingScreen(task: Task, onReturnToTask: () -> Unit) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFB71C1C))
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Czas na zadanie!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = task.taskName,
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${task.start.format(timeFormatter)} – ${task.end.format(timeFormatter)}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Ta aplikacja jest zablokowana do końca zadania. Wróć do pracy!",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onReturnToTask,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFB71C1C)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Wracam do zadania", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private class OverlayLifecycleOwner : SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    fun start() {
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}
