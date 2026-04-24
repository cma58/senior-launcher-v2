package com.inclusion.seniorlauncher.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed

/**
 * Debounce window for click events. 500ms is the sweet spot: catches
 * accidental double-taps from tremoring hands without making the UI feel
 * unresponsive.
 */
private const val DEBOUNCE_MS = 500L

/**
 * Tracks last click timestamp per-call-site so multiple buttons don't
 * interfere with each other.
 */
private class ClickState {
    var lastClickAt: Long = 0L
}

/**
 * Debounced click modifier.
 *
 * Two layers of protection:
 *  1. Lifecycle gate — only fires when the host is RESUMED. Prevents taps
 *     that arrive during transitions (e.g. after a screen just popped) from
 *     activating the wrong handler.
 *  2. Time gate — drops clicks within [DEBOUNCE_MS] of the previous one.
 *
 * Use this instead of Modifier.clickable everywhere in the launcher.
 */
fun Modifier.debouncedClickable(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit
): Modifier = composed {
    val state = remember { ClickState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val interactionSource = remember { MutableInteractionSource() }

    this.clickable(
        interactionSource = interactionSource,
        indication = null, // we add explicit ripple at component level for larger feedback
        enabled = enabled,
        role = role,
        onClickLabel = onClickLabel,
        onClick = dropUnlessResumed(lifecycleOwner) {
            val now = System.currentTimeMillis()
            if (now - state.lastClickAt >= DEBOUNCE_MS) {
                state.lastClickAt = now
                onClick()
            }
        }
    )
}

/**
 * Overload for composable callers that have a LifecycleOwner in scope but
 * not through LocalLifecycleOwner (e.g. tests).
 */
@Composable
fun rememberDebouncedClick(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onClick: () -> Unit
): () -> Unit {
    val state = remember { ClickState() }
    return dropUnlessResumed(lifecycleOwner) {
        val now = System.currentTimeMillis()
        if (now - state.lastClickAt >= DEBOUNCE_MS) {
            state.lastClickAt = now
            onClick()
        }
    }
}
