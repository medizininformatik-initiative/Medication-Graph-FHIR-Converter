package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.ui.theme.TUMWeb

/**
 * Clips this component and equips it with a box-like background.
 *
 * @param color the color of the background
 */
fun Modifier.clipToBox(color: Color = TUMWeb.TumGrey7) = this
    .clip(RoundedCornerShape(4.dp))
    .background(color)
    .padding(horizontal = 4.dp, vertical = 2.dp)