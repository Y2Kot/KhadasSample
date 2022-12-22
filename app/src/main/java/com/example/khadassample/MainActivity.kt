package com.example.khadassample

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.khadassample.ui.theme.KhadasSampleTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Engine
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.filter.Filter
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.frame.FrameProcessor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cameraScreenViewModel: MainScreenViewModel by viewModels()
        setContent {
            KhadasSampleTheme {
                // A surface container using the 'background' color from the theme
                MainContent(
                    modifier = Modifier.fillMaxSize(),
                    cameraScreenViewModel = cameraScreenViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainContent(modifier: Modifier = Modifier, cameraScreenViewModel: MainScreenViewModel) {
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            CameraPermissionNotGrantedContent(doNotShowRationale = doNotShowRationale,
                onPermissionAgreeClick = { cameraPermissionState.launchPermissionRequest() },
                onPermissionDisagreeClick = { doNotShowRationale = true })
        },
        permissionNotAvailableContent = {
            CameraPermissionNotAvailableContent()
        }
    ) {
        CameraPreviewScreen(modifier = modifier, cameraScreenViewModel)
    }
}

@Composable
fun BitmapImage(
    modifier: Modifier = Modifier,
    bitmap: Bitmap?
) {
    Image(
        modifier = modifier,
        bitmap = bitmap?.asImageBitmap() ?: ImageBitmap(200, 200),
        contentDescription = "Frame processor images"
    )
}

@Composable
fun CameraPermissionNotGrantedContent(
    modifier: Modifier = Modifier,
    doNotShowRationale: Boolean = false,
    onPermissionAgreeClick: () -> Unit = {},
    onPermissionDisagreeClick: () -> Unit = {}
) {
    if (doNotShowRationale) {
        Text(modifier = modifier, text = stringResource(R.string.feature_not_available))
    } else {
        Column(modifier = modifier.padding(48.dp)) {
            Text(stringResource(R.string.grant_permission_rationale))
            Row {
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = onPermissionAgreeClick
                ) {
                    Text(stringResource(R.string.ok))
                }
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = onPermissionDisagreeClick
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun CameraPermissionNotAvailableContent(
    modifier: Modifier = Modifier,
    navigateToSettingsScreen: () -> Unit = {}
) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.grant_in_settings))
        Button(onClick = navigateToSettingsScreen) {
            Text(stringResource(R.string.open_settings))
        }
    }
}

data class CameraState(val facing: Facing = Facing.BACK)

@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel
) {
    val images = viewModel.frames.collectAsState()
    var cameraState by remember { mutableStateOf(CameraState()) }
    Column {
        Text(text = "Camera:")
        CameraViewWrapper(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            cameraListener = viewModel.cameraListener,
            frameProcessor = viewModel.frameProcessor,
            cameraFacing = cameraState.facing
        )
        Text(text = "Frame processor:")
        BitmapImage(
            bitmap = images.value,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Button(onClick = {
            cameraState = if (cameraState.facing == Facing.BACK) {
                cameraState.copy(facing = Facing.FRONT)
            } else {
                cameraState.copy(facing = Facing.BACK)
            }
        }) {
            Text(text = "Switch camera")
        }
    }
}

@Composable
fun CameraViewWrapper(
    modifier: Modifier = Modifier,
    filter: Filter = Filters.NONE.newInstance(),
    cameraFacing: Facing = Facing.BACK,
    cameraListener: CameraListener = object : CameraListener() {},
    frameProcessor: FrameProcessor = FrameProcessor { },
) {
    val currentLifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            CameraView(context).apply {
                setExperimental(true)
                engine = Engine.CAMERA2
                keepScreenOn = true
                audio = Audio.OFF
                facing = cameraFacing
                mode = Mode.PICTURE
                addCameraListener(cameraListener)
                setLifecycleOwner(currentLifecycleOwner)
                setFilter(filter)
                addFrameProcessor(frameProcessor)
            }
        },
        update = {
            it.facing = cameraFacing
        }
    )
}
