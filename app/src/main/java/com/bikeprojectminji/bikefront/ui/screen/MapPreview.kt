package com.bikeprojectminji.bikefront.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val DEFAULT_STYLE_URL = "https://demotiles.maplibre.org/style.json"
private val SEOUL_CITY_HALL = LatLng(37.5665, 126.9780)

@Composable
fun GajaMapPreview(
    modifier: Modifier = Modifier,
    heightDp: Int = 220,
) {
    val mapView = rememberMapViewWithLifecycle()
    AndroidView(
        factory = { mapView },
        modifier = modifier
            .fillMaxSize()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(24.dp)),
    )
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(Bundle())
            getMapAsync { mapLibreMap ->
                mapLibreMap.setStyle(Style.Builder().fromUri(DEFAULT_STYLE_URL))
                mapLibreMap.cameraPosition = CameraPosition.Builder()
                    .target(SEOUL_CITY_HALL)
                    .zoom(12.0)
                    .build()
            }
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}
