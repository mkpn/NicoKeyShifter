package com.nicokeyshifter

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    val videoSourceUrl = MutableStateFlow<Uri?>(null)
    val currentKeyValue = MutableStateFlow(0)
    val currentKeyText = MutableStateFlow("Key:±0")

    fun updateVideoSourceUrl(uri: Uri) {
        viewModelScope.launch {
            videoSourceUrl.emit(uri)
        }
    }

    fun pitchUp() {
        viewModelScope.launch {
            currentKeyValue.emit(++currentKeyValue.value)
            updateCurrentKey()
        }
    }

    fun pitchDown() {
        viewModelScope.launch {
            currentKeyValue.emit(--currentKeyValue.value)
            updateCurrentKey()
        }
    }

    private suspend fun updateCurrentKey() {
        val newText = when {
            currentKeyValue.value == 0 -> "Key:±0"
            currentKeyValue.value > 0 -> "Key:+${currentKeyValue.value}"
            else -> "Key:${currentKeyValue.value}"
        }
        currentKeyText.emit(newText)
    }
}