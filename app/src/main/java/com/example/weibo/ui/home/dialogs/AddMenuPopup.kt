package com.example.weibo.ui.home.dialogs

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.weibo.R

@Composable
fun AddMenuPopup(
    onDismiss: () -> Unit,
    onWritePost: () -> Unit,
    onAlbum: () -> Unit,
    onCheckIn: () -> Unit,
    onLive: () -> Unit
) {
    Popup(
        onDismissRequest = onDismiss,
        alignment = Alignment.TopEnd
    ) {
        
        
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(top = 56.dp, end = 8.dp)
                .offset(y = 4.dp)
        ) {
            MenuArrow(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 20.dp), 
                color = Color(0xFF3A3A3A)
            )

            Column(
                modifier = Modifier
                    .width(160.dp)
                    .background(
                        color = Color(0xFF3A3A3A),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                AddMenuItem(
                    text = "写微博",
                    iconRes = R.drawable.ic_add_to_write_weibo,
                    onClick = {
                        onWritePost()
                        onDismiss()
                    }
                )

                MenuDivider()

                AddMenuItem(
                    text = "相册",
                    iconRes = R.drawable.ic_add_to_album,
                    onClick = {
                        onAlbum()
                        onDismiss()
                    }
                )

                MenuDivider()

                AddMenuItem(
                    text = "签到/点评",
                    iconRes = R.drawable.ic_add_to_check_in_review,
                    onClick = {
                        onCheckIn()
                        onDismiss()
                    }
                )

                MenuDivider()

                AddMenuItem(
                    text = "直播",
                    iconRes = R.drawable.ic_add_to_live,
                    onClick = {
                        onLive()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuArrow(
    modifier: Modifier,
    color: Color,
    width: Dp = 20.dp,
    height: Dp = 10.dp
) {
    Canvas(
        modifier = modifier
            .size(width = width, height = height)
    ) {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width / 2f, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
private fun MenuDivider() {
    Divider(
        color = Color(0xFF4A4A4A),
        thickness = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AddMenuItem(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .indication(interactionSource = interactionSource, indication = null)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = Color.Unspecified,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
