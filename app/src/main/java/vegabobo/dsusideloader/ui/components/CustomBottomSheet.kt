package vegabobo.dsusideloader.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomBottomSheet(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.(hideSheet: suspend () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )

    // Initial state of BottomSheet is "Hidden"
    // * we can change it to "Expanded", however, animation would be lost.
    // This workaround detects if BottomSheet is being called by its first time
    // if so, then, we call "show()" (Hidden => Expanded)
    // after that, if state changes again to Hidden, it means user dismissed our sheet
    // then we call "onDismiss()", that may do the job to make us gone away.
    val isFirst = remember { mutableStateOf(true) }
    val shouldCallOnDismiss = remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        snapshotFlow { sheetState.currentValue }
            .collect {
                if (it == ModalBottomSheetValue.Hidden) {
                    if (isFirst.value) {
                        sheetState.show()
                        isFirst.value = false
                        return@collect
                    }
                    if (shouldCallOnDismiss.value) {
                        onDismiss()
                        return@collect
                    }
                }
            }
    }

    BackHandler(sheetState.isVisible) {
        coroutineScope.launch { sheetState.hide() }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            BottomSheet(
                title = title,
                icon = icon
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 18.dp, start = 18.dp, bottom = 14.dp, top = 14.dp)
                ) {
                    // Shortcut used to hide sheet by event
                    content { sheetState.hide(); shouldCallOnDismiss.value = false; }
                }
            }
        },
        modifier = modifier.fillMaxSize(),
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
    ) {}

    // block touch on any part of screen
    // till BottomSheet is opened by its first time
    if (isFirst.value)
        Surface(
            Modifier
                .fillMaxSize()
                .alpha(0F)
        ) {
            BackHandler {}
        }

}