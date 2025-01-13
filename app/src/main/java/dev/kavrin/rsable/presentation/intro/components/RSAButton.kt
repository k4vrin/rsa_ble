package dev.kavrin.rsable.presentation.intro.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kavrin.rsable.presentation.theme.DarkGreen
import dev.kavrin.rsable.presentation.theme.Dimen
import dev.kavrin.rsable.presentation.theme.RsLight
import dev.kavrin.rsable.presentation.theme.RsRed

@Composable
fun RSAButton(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.DefaultButtonHeight),
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RsRed,
            contentColor = RsLight,
            disabledContainerColor = RsRed.copy(alpha = Dimen.DISABLED_ALPHA),
            disabledContentColor = RsLight.copy(alpha = Dimen.DISABLED_ALPHA),
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                )
            }
        }
    }
}


@Preview
@Composable
private fun RSAButtonPrev() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
    ) {
        RSAButton(
            modifier = Modifier,
            title = "Peripheral",
            enabled = true,
            isLoading = false,
            onClick = {

            }
        )
    }
}