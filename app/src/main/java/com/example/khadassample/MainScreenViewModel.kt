package com.example.khadassample

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.frame.FrameProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainScreenViewModel: ViewModel() {
    private val tag = MainScreenViewModel::class.simpleName
    private val _frames = MutableStateFlow<Bitmap?>(null)
    val frames: StateFlow<Bitmap?> = _frames
    val frameProcessor = FrameProcessor { frame ->
        val image = frame.getData<Image>()
        val bm = image.toBitmap()
        _frames.value = bm
        Log.d(tag, "Got new frame: $bm")
    }

    val cameraListener = object : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
            super.onCameraOpened(options)
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
        }
    }
}
