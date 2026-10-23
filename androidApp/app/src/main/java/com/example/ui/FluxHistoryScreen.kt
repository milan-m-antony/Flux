package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun FluxHistoryScreen(
    viewModel: MainViewModel,
    onNotificationClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Temperature") }
    var selectedRange by remember { mutableStateOf("Day") }
    val connectionState by viewModel.connectionState.collectAsState()
    val isOnline = connectionState == com.example.ble.ConnectionState.CONNECTED || true

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Shared Flux Header
            FluxHeader(
                isOnline = isOnline,
                hasUnreadAlerts = true,
                onNotificationClick = onNotificationClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Title & Subtitle
            Text(
                text = "History",
                color = FluxTextDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "View past data and device activity",
                color = FluxTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Segmented Navigation Tabs (Temperature / Device Activity / Alerts)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .background(Color.White, RoundedCornerShape(26.dp))
                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Temperature Tab (Active in Screenshot)
                Row(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedTab == "Temperature") FluxOrange else Color.Transparent)
                        .clickable { selectedTab = "Temperature" }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Thermostat,
                        contentDescription = null,
                        tint = if (selectedTab == "Temperature") Color.White else FluxTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Temperature",
                        color = if (selectedTab == "Temperature") Color.White else FluxTextDark,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Device Activity Tab
                Row(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedTab == "Device Activity") FluxOrange else Color.Transparent)
                        .clickable { selectedTab = "Device Activity" }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = null,
                        tint = if (selectedTab == "Device Activity") Color.White else FluxTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Device Activity",
                        color = if (selectedTab == "Device Activity") Color.White else FluxTextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Alerts Tab
                Row(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedTab == "Alerts") FluxOrange else Color.Transparent)
                        .clickable { selectedTab = "Alerts" }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = if (selectedTab == "Alerts") Color.White else FluxTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Alerts",
                        color = if (selectedTab == "Alerts") Color.White else FluxTextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Date Picker & Day/Week/Month Range Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Selector Pill
                Row(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0E000000))
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = FluxTextDark,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "20 Oct 2026",
                        color = FluxTextDark,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = FluxTextDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Day / Week / Month Segmented Pill
                Row(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0E000000))
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Day (Selected Orange Circle)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedRange == "Day") FluxOrange else Color.Transparent)
                            .clickable { selectedRange = "Day" }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Day",
                            color = if (selectedRange == "Day") Color.White else FluxTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Week
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedRange == "Week") FluxOrange else Color.Transparent)
                            .clickable { selectedRange = "Week" }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Week",
                            color = if (selectedRange == "Week") Color.White else FluxTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Month
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedRange == "Month") FluxOrange else Color.Transparent)
                            .clickable { selectedRange = "Month" }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Month",
                            color = if (selectedRange == "Month") Color.White else FluxTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Temperature Trend Card (With 30.8°C Max and 20.1°C Min Badges)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(0.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF2EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Thermostat,
                                    contentDescription = null,
                                    tint = FluxOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Temperature Trend",
                                    color = FluxTextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "20 Oct 2026 • Hourly",
                                    color = FluxTextMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Max & Min Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Max badge
                            Column(
                                modifier = Modifier
                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "30.8°C",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Max",
                                    color = Color(0xFFEF4444),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Min badge
                            Column(
                                modifier = Modifier
                                    .background(Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "20.1°C",
                                    color = Color(0xFF2563EB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Min",
                                    color = Color(0xFF3B82F6),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hourly Area Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        HistoryChartCanvas(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 6. Data Log Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Data Log",
                        color = FluxTextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Recorded temperature readings",
                        color = FluxTextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "20 Oct 2026",
                    color = FluxTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Spacious Horizontal Data Log Cards
            DataLogCard(
                iconTint = Color(0xFFEF4444),
                iconBg = Color(0xFFFEE2E2),
                time = "12:00 PM",
                value = "30.8°C",
                statusLabel = "High",
                statusBg = Color(0xFFFEE2E2),
                statusColor = Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(10.dp))

            DataLogCard(
                iconTint = FluxOrange,
                iconBg = Color(0xFFFFF2EB),
                time = "08:00 AM",
                value = "24.1°C",
                statusLabel = "Normal",
                statusBg = Color(0xFFDCFCE7),
                statusColor = Color(0xFF16A34A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            DataLogCard(
                iconTint = Color(0xFF3B82F6),
                iconBg = Color(0xFFDBEAFE),
                time = "04:00 AM",
                value = "19.6°C",
                statusLabel = "Low",
                statusBg = Color(0xFFDBEAFE),
                statusColor = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.height(10.dp))

            DataLogCard(
                iconTint = FluxOrange,
                iconBg = Color(0xFFFFF2EB),
                time = "12:00 AM",
                value = "20.2°C",
                statusLabel = "Normal",
                statusBg = Color(0xFFDCFCE7),
                statusColor = Color(0xFF16A34A)
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun DataLogCard(
    iconTint: Color,
    iconBg: Color,
    time: String,
    value: String,
    statusLabel: String,
    statusBg: Color,
    statusColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp), spotColor = Color(0x0C000000))
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(0.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Thermometer Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Time
        Text(
            text = time,
            color = FluxTextMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        // Value in bold
        Text(
            text = value,
            color = FluxTextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Status pill
        Box(
            modifier = Modifier
                .background(statusBg, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun HistoryChartCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftPadding = 30f
        val bottomPadding = 24f
        val chartWidth = width - leftPadding
        val chartHeight = height - bottomPadding

        // 1. Horizontal dashed grid lines (40, 30, 20, 10)
        val ySteps = listOf(40f, 30f, 20f, 10f)
        for (i in ySteps.indices) {
            val y = (chartHeight * i / (ySteps.size - 1))
            drawLine(
                color = Color(0xFFEDF2F7),
                start = Offset(leftPadding, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        // 2. Data Points for the 24-hr spline
        val points = listOf(
            Offset(leftPadding + chartWidth * 0.00f, chartHeight * 0.70f),
            Offset(leftPadding + chartWidth * 0.10f, chartHeight * 0.58f),
            Offset(leftPadding + chartWidth * 0.20f, chartHeight * 0.65f),
            Offset(leftPadding + chartWidth * 0.30f, chartHeight * 0.48f),
            Offset(leftPadding + chartWidth * 0.45f, chartHeight * 0.52f),
            Offset(leftPadding + chartWidth * 0.58f, chartHeight * 0.20f), // Peak 30.8°C around 12:00
            Offset(leftPadding + chartWidth * 0.70f, chartHeight * 0.28f),
            Offset(leftPadding + chartWidth * 0.82f, chartHeight * 0.44f),
            Offset(leftPadding + chartWidth * 0.92f, chartHeight * 0.52f),
            Offset(leftPadding + chartWidth * 1.00f, chartHeight * 0.46f)
        )

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2f
                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, chartHeight)
            lineTo(points.first().x, chartHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    FluxOrange.copy(alpha = 0.35f),
                    FluxOrange.copy(alpha = 0.03f)
                ),
                startY = 0f,
                endY = chartHeight
            )
        )

        drawPath(
            path = path,
            color = FluxOrange,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Static text overlay for labels
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxHeight()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("40", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("30", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("20", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("10", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(start = 24f.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("00:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("04:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("08:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("12:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("16:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("20:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}
