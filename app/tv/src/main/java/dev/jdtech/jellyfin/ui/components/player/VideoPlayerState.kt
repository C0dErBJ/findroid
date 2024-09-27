package dev.jdtech.jellyfin.ui.components.player

import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce

class VideoPlayerState internal constructor(
    @IntRange(from = 0)
    private val hideSeconds: Int,
) {
    private var _controlsVisible by mutableStateOf(true)
    val controlsVisible get() = _controlsVisible

    private var _quickSeekMode by mutableStateOf(false)
    val quickSeekMode get() = _quickSeekMode

    fun showControls(seconds: Int = hideSeconds, quickSeek: Boolean = false) {
        _controlsVisible = true
        _quickSeekMode = quickSeek
        channel.trySend(seconds)
    }

    private val channel = Channel<Int>(CONFLATED)

    @OptIn(FlowPreview::class)
    suspend fun observe() {
        channel.consumeAsFlow()
            .debounce { it.toLong() * 1000 }
            .collect {
                _controlsVisible = false
                _quickSeekMode = false
            }
    }
}

@Composable
fun rememberVideoPlayerState(@IntRange(from = 0) hideSeconds: Int = 2) =
    remember {
        VideoPlayerState(hideSeconds = hideSeconds)
    }
        .also {
            LaunchedEffect(it) { it.observe() }
        }
