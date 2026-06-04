package com.bikeprojectminji.bikefront.curator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.GajaSectionCard
import com.bikeprojectminji.bikefront.ui.screen.GajaStatusBadge
import com.bikeprojectminji.bikefront.ui.screen.SecondaryActionButton
import com.bikeprojectminji.bikefront.ui.screen.SectionHeader
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

@Composable
fun CuratorOnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val store = CuratorTravelPreferenceStore(context)
    val initialPreference = store.read()
    var step by rememberSaveable { mutableStateOf(0) }
    var ridePurpose by rememberSaveable { mutableStateOf(initialPreference.ridePurpose.name) }
    var routePriority by rememberSaveable { mutableStateOf(initialPreference.routePriority.name) }
    var distanceComfort by rememberSaveable { mutableStateOf(initialPreference.distanceComfort.name) }
    var avoidConditionCsv by rememberSaveable {
        mutableStateOf(initialPreference.avoidConditions.sortedBy { it.ordinal }.joinToString(",") { it.name })
    }
    var helperMessage by rememberSaveable { mutableStateOf("3단계 안에서 여행 취향만 가볍게 고릅니다.") }

    val preference = CuratorTravelPreference(
        ridePurpose = RidePurpose.valueOf(ridePurpose),
        routePriority = RoutePriority.valueOf(routePriority),
        distanceComfort = DistanceComfort.valueOf(distanceComfort),
        avoidConditions = parseAvoidConditions(avoidConditionCsv),
    )

    Scaffold(
        topBar = { GajaBrandTopBar(title = "온보딩") },
        containerColor = GajaColors.Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large),
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))
            GajaStatusBadge(text = "${step + 1}/3")

            when (step) {
                0 -> CuratorPreferenceFirstStep(
                    ridePurpose = preference.ridePurpose,
                    routePriority = preference.routePriority,
                    onRidePurposeSelected = { ridePurpose = it.name },
                    onRoutePrioritySelected = { routePriority = it.name },
                )
                1 -> CuratorPreferenceConditionStep(
                    distanceComfort = preference.distanceComfort,
                    avoidConditions = preference.avoidConditions,
                    onDistanceSelected = { distanceComfort = it.name },
                    onAvoidConditionToggle = {
                        avoidConditionCsv = preference.toggleAvoidCondition(it)
                            .avoidConditions
                            .sortedBy { condition -> condition.ordinal }
                            .joinToString(",") { condition -> condition.name }
                    },
                )
                else -> CuratorPreferenceSummary(preference = preference, helperMessage = helperMessage)
            }

            if (step < 2) {
                GajaPrimaryButton(text = "다음", onClick = { step += 1 })
                SecondaryActionButton(text = "나중에 설정", onClick = onFinish)
            } else {
                GajaPrimaryButton(
                    text = "홈으로 가기",
                    onClick = {
                        val saved = store.save(preference)
                        helperMessage = if (saved) {
                            "취향 저장 완료"
                        } else {
                            "취향 저장은 나중에 My Page에서 다시 설정할 수 있어요."
                        }
                        onFinish()
                    },
                )
                SecondaryActionButton(text = "이전", onClick = { step = 1 })
            }
            Spacer(Modifier.height(GajaSpacing.Large))
        }
    }
}

@Composable
private fun CuratorPreferenceFirstStep(
    ridePurpose: RidePurpose,
    routePriority: RoutePriority,
    onRidePurposeSelected: (RidePurpose) -> Unit,
    onRoutePrioritySelected: (RoutePriority) -> Unit,
) {
    SectionHeader(
        title = "자전거 여행 취향",
        subtitle = "기록보다 어떤 길이 좋은지 먼저 고릅니다.",
    )
    PreferenceSection(title = "여행 목적") {
        RidePurpose.entries.forEach {
            PreferenceChip(label = it.label, selected = ridePurpose == it, onClick = { onRidePurposeSelected(it) })
        }
    }
    PreferenceSection(title = "경로 우선순위") {
        RoutePriority.entries.forEach {
            PreferenceChip(label = it.label, selected = routePriority == it, onClick = { onRoutePrioritySelected(it) })
        }
    }
}

@Composable
private fun CuratorPreferenceConditionStep(
    distanceComfort: DistanceComfort,
    avoidConditions: Set<AvoidCondition>,
    onDistanceSelected: (DistanceComfort) -> Unit,
    onAvoidConditionToggle: (AvoidCondition) -> Unit,
) {
    SectionHeader(
        title = "편한 거리와 피하고 싶은 조건",
        subtitle = "추천에서 거리 부담과 위험 조건을 낮추는 기준입니다.",
    )
    PreferenceSection(title = "편한 거리") {
        DistanceComfort.entries.forEach {
            PreferenceChip(label = it.label, selected = distanceComfort == it, onClick = { onDistanceSelected(it) })
        }
    }
    PreferenceSection(title = "피하고 싶은 조건") {
        AvoidCondition.entries.forEach {
            PreferenceChip(label = it.label, selected = avoidConditions.contains(it), onClick = { onAvoidConditionToggle(it) })
        }
    }
}

@Composable
private fun CuratorPreferenceSummary(preference: CuratorTravelPreference, helperMessage: String) {
    SectionHeader(
        title = "선호 경로 준비 완료",
        subtitle = "이 기준은 홈 추천과 경로 설명에 먼저 반영됩니다.",
    )
    GajaSectionCard(contentPadding = PaddingValues(GajaSpacing.Large)) {
        SummaryRow("여행 목적", preference.ridePurpose.label)
        SummaryRow("경로 우선순위", preference.routePriority.label)
        SummaryRow("편한 거리", preference.distanceComfort.label)
        SummaryRow(
            "피하고 싶은 조건",
            preference.avoidConditions.sortedBy { it.ordinal }.joinToString(", ") { it.label }.ifBlank { "없음" },
        )
    }
    Text(
        text = helperMessage,
        style = MaterialTheme.typography.bodySmall,
        color = GajaColors.TextSecondary,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferenceSection(title: String, content: @Composable () -> Unit) {
    GajaSectionCard(
        containerColor = GajaColors.SurfaceMuted,
        shape = RoundedCornerShape(GajaSpacing.Large),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
        ) {
            content()
        }
    }
}

@Composable
private fun PreferenceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GajaColors.Primary,
            selectedLabelColor = GajaColors.White,
            containerColor = GajaColors.White,
            labelColor = GajaColors.TextSecondary,
        ),
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = GajaColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextPrimary)
    }
}

private fun parseAvoidConditions(raw: String): Set<AvoidCondition> {
    if (raw.isBlank()) return emptySet()
    return raw.split(",")
        .mapNotNull { name -> AvoidCondition.entries.firstOrNull { it.name == name } }
        .toSet()
}
