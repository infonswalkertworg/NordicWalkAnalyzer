package com.nordicwalk.feature.video.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * 檢查所需權限是否已授予
 */
fun Context.hasRequiredPermissions(): Boolean {
    return REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * 檢查相機權限
 */
fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * 檢查錄音權限
 */
fun Context.hasAudioPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}

val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
)

/**
 * Compose 權限請求處理
 */
@Composable
fun rememberPermissionState(
    onPermissionsGranted: () -> Unit = {},
    onPermissionsDenied: () -> Unit = {}
): PermissionState {
    val context = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(context.hasRequiredPermissions())
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermissions = allGranted
        
        if (allGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }
    
    return remember {
        PermissionState(
            hasPermissions = hasPermissions,
            requestPermissions = {
                permissionLauncher.launch(REQUIRED_PERMISSIONS)
            },
            updatePermissionStatus = { granted ->
                hasPermissions = granted
            }
        )
    }
}

/**
 * 權限狀態數據類
 */
data class PermissionState(
    val hasPermissions: Boolean,
    val requestPermissions: () -> Unit,
    val updatePermissionStatus: (Boolean) -> Unit
)
