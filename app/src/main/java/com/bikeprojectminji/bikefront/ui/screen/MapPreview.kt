package com.bikeprojectminji.bikefront.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bikeprojectminji.bikefront.config.AppConfig
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import com.bikeprojectminji.bikefront.ridemap.HttpCourseRoutePointsGateway
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val ROUTE_SOURCE_ID = "gaja-route-source"
private const val ROUTE_LAYER_ID = "gaja-route-layer"
private val SEOUL_CITY_HALL = LatLng(37.5665, 126.9780)

enum class MapDisplayMode {
    PREVIEW,
    RIDE,
}

@Composable
fun GajaMapPreview(
    modifier: Modifier = Modifier,
    heightDp: Int = 220,
    courseId: Long? = null,
    mode: MapDisplayMode = MapDisplayMode.PREVIEW,
    locationPermissionGranted: Boolean = false,
    onRoutePointsLoaded: ((List<CourseRoutePointsGateway.RoutePoint>) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val gateway = remember { HttpCourseRoutePointsGateway() }
    val mapView = rememberMapViewWithLifecycle(
        context = context,
        lifecycle = lifecycle,
        mode = mode,
        locationPermissionGranted = locationPermissionGranted,
    )
    var routeState by remember(courseId) { mutableStateOf<RouteState>(RouteState.Idle) }

    LaunchedEffect(courseId) {
        if (courseId == null || courseId <= 0L) {
            routeState = RouteState.Idle
            onRoutePointsLoaded?.invoke(emptyList())
            return@LaunchedEffect
        }
        routeState = RouteState.Loading
        gateway.loadRoutePoints(courseId, object : CourseRoutePointsGateway.Callback {
            override fun onSuccess(result: CourseRoutePointsGateway.RoutePointsResult) {
                routeState = RouteState.Success(result.points)
                onRoutePointsLoaded?.invoke(result.points)
            }

            override fun onFailure(message: String) {
                routeState = RouteState.Error(message)
                onRoutePointsLoaded?.invoke(emptyList())
            }
        })
    }

    LaunchedEffect(routeState, locationPermissionGranted) {
        when (val state = routeState) {
            is RouteState.Success -> applyRouteToMap(mapView, state.points)
            else -> applyRouteToMap(mapView, emptyList())
        }
        updateLocationComponent(mapView, context, locationPermissionGranted)
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .then(if (mode == MapDisplayMode.PREVIEW) Modifier.fillMaxWidth().height(heightDp.dp) else Modifier.fillMaxSize())
            .clip(RoundedCornerShape(if (mode == MapDisplayMode.PREVIEW) 24.dp else 0.dp)),
    )
}

private sealed interface RouteState {
    data object Idle : RouteState
    data object Loading : RouteState
    data class Success(val points: List<CourseRoutePointsGateway.RoutePoint>) : RouteState
    data class Error(val message: String) : RouteState
}

@Composable
private fun rememberMapViewWithLifecycle(
    context: Context,
    lifecycle: Lifecycle,
    mode: MapDisplayMode,
    locationPermissionGranted: Boolean,
): MapView {
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(Bundle())
            getMapAsync { mapLibreMap ->
                mapLibreMap.setStyle(Style.Builder().fromUri(AppConfig.MAP_STYLE_URL)) { style ->
                    if (style.getSource(ROUTE_SOURCE_ID) == null) {
                        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID, FeatureCollection.fromFeatures(emptyArray())))
                    }
                    if (style.getLayer(ROUTE_LAYER_ID) == null) {
                        style.addLayer(
                            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                                lineColor("#FF8224"),
                                lineWidth(5f),
                                lineCap("round"),
                                lineJoin("round"),
                            ),
                        )
                    }
                    mapLibreMap.cameraPosition = CameraPosition.Builder()
                        .target(SEOUL_CITY_HALL)
                        .zoom(if (mode == MapDisplayMode.RIDE) 14.0 else 12.0)
                        .build()
                    updateLocationComponent(this, context, locationPermissionGranted)
                }
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

private fun applyRouteToMap(
    mapView: MapView,
    points: List<CourseRoutePointsGateway.RoutePoint>,
) {
    mapView.getMapAsync { mapLibreMap ->
        mapLibreMap.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID) ?: return@getStyle
            if (points.isEmpty()) {
                source.setGeoJson(FeatureCollection.fromFeatures(emptyArray()))
                return@getStyle
            }
            val latLngs = points.sortedBy { it.pointOrder }.map { LatLng(it.latitude, it.longitude) }
            val lineString = LineString.fromLngLats(latLngs.map { Point.fromLngLat(it.longitude, it.latitude) })
            source.setGeoJson(Feature.fromGeometry(lineString))
            if (latLngs.size >= 2) {
                val bounds = LatLngBounds.Builder().includes(latLngs).build()
                mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 72), 800)
            } else {
                mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs.first(), 14.0), 800)
            }
        }
    }
}

private fun updateLocationComponent(
    mapView: MapView,
    context: Context,
    locationPermissionGranted: Boolean,
) {
    if (!locationPermissionGranted || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    mapView.getMapAsync { mapLibreMap ->
        mapLibreMap.getStyle { style ->
            val locationComponent = mapLibreMap.locationComponent
            val activationOptions = LocationComponentActivationOptions.builder(context, style).build()
            locationComponent.activateLocationComponent(activationOptions)
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.NORMAL
        }
    }
}
