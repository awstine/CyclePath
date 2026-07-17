package com.siaka.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siaka.ui.theme.Background
import com.siaka.ui.theme.PrimaryDark

@Composable
fun AuthTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(label, color = Color.Gray.copy(alpha = 0.6f)) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = Color.Gray) } },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Background,
            focusedContainerColor = Background,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedBorderColor = PrimaryDark
        ),
        singleLine = true
    )
}

@Composable
fun SocialAuthButton(
    text: String,
    icon: Painter? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.DarkGray
        ),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Composable
fun AuthDivider(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray,
                fontSize = 12.sp
            )
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}
