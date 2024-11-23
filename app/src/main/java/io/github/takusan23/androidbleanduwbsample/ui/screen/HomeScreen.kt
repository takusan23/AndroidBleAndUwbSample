package io.github.takusan23.androidbleanduwbsample.ui.screen

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private val REQUIRED_PERMISSION = listOf(
    android.Manifest.permission.BLUETOOTH,
    android.Manifest.permission.BLUETOOTH_CONNECT,
    android.Manifest.permission.BLUETOOTH_SCAN,
    android.Manifest.permission.BLUETOOTH_ADVERTISE,
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.UWB_RANGING
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onControllerClick: () -> Unit,
    onControleeClick: () -> Unit
) {
    val context = LocalContext.current
    val isGranted = remember {
        mutableStateOf(REQUIRED_PERMISSION.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED })
    }

    val permissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { isGranted.value = it.all { it.value } }
    )

    LaunchedEffect(key1 = Unit) {
        // 権限をリクエスト
        permissionRequest.launch(REQUIRED_PERMISSION.toTypedArray())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Home") })
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // 権限が付与されるまでボタンを出さない
            if (!isGranted.value) {
                Text(text = "権限が付与されていません")
                return@Scaffold
            }

            // 画面遷移用
            Button(onClick = onControllerClick) {
                Text(text = "Controller (Host)")
            }
            Button(onClick = onControleeClick) {
                Text(text = "Controlee (Guest)")
            }
        }
    }
}