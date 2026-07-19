package com.example.data.service.mock

import com.example.domain.service.DownloadStatus
import com.example.domain.service.VideoDownloadService
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class MockVideoDownloadService : VideoDownloadService {
    override fun downloadVideo(videoUrl: String): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Idle)
        delay(300)

        val totalBytes = 5_242_880L // ~5 MB
        var bytesDownloaded = 0L
        val chunkSize = 524_288L // ~500 KB per step
        
        while (bytesDownloaded < totalBytes) {
            if (!currentCoroutineContext().isActive) {
                emit(DownloadStatus.Error(InterruptedException("Download cancelled by caller.")))
                return@flow
            }
            delay(150) // simulate network delay
            bytesDownloaded += chunkSize
            if (bytesDownloaded > totalBytes) bytesDownloaded = totalBytes
            
            val progress = bytesDownloaded.toFloat() / totalBytes
            emit(DownloadStatus.Progress(progress, bytesDownloaded, totalBytes))
        }

        emit(DownloadStatus.Success("/storage/emulated/0/Download/reel_temp_video.mp4"))
    }
}
