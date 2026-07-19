package com.example.domain.service

import kotlinx.coroutines.flow.Flow

interface VideoDownloadService {
    /**
     * Downloads a video from [videoUrl] and streams download progress (0.0 to 1.0).
     */
    fun downloadVideo(videoUrl: String): Flow<DownloadStatus>
}

sealed interface DownloadStatus {
    object Idle : DownloadStatus
    data class Progress(val percentage: Float, val bytesDownloaded: Long, val totalBytes: Long) : DownloadStatus
    data class Success(val localFilePath: String) : DownloadStatus
    data class Error(val exception: Throwable) : DownloadStatus
}
