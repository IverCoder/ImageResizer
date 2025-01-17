package ru.tech.imageresizershrinker.compare_screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cookhelper.dynamic.theme.LocalDynamicThemeState
import com.smarttoolfactory.beforeafter.BeforeAfterImage
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.generate_palette.isScrollingUp
import ru.tech.imageresizershrinker.main_screen.components.Screen
import ru.tech.imageresizershrinker.main_screen.components.block
import ru.tech.imageresizershrinker.resize_screen.components.ImageNotPickedWidget
import ru.tech.imageresizershrinker.resize_screen.components.LoadingDialog
import ru.tech.imageresizershrinker.resize_screen.components.LocalToastHost
import ru.tech.imageresizershrinker.utils.BitmapUtils.getBitmapByUri
import ru.tech.imageresizershrinker.widget.Marquee

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    comparableUris: Pair<Uri, Uri>?,
    pushNewUris: (List<Uri>?) -> Unit,
    navController: NavController<Screen>,
    onGoBack: () -> Unit,
    viewModel: CompareViewModel = viewModel()
) {
    val context = LocalContext.current
    val toastHostState = LocalToastHost.current
    val scope = rememberCoroutineScope()
    val themeState = LocalDynamicThemeState.current

    var progress by rememberSaveable { mutableStateOf(50f) }

    LaunchedEffect(viewModel.bitmapData) {
        viewModel.bitmapData?.first?.let {
            themeState.updateColorByImage(it)
        }
    }
    LaunchedEffect(comparableUris) {
        comparableUris?.let { (before, after) ->
            pushNewUris(null)
            val newBeforeBitmap = context.getBitmapByUri(before)
            val newAfterBitmap = context.getBitmapByUri(after)
            if (newAfterBitmap != null && newBeforeBitmap != null) {
                viewModel.updateBitmapData(
                    newBeforeBitmap = newBeforeBitmap,
                    newAfterBitmap = newAfterBitmap
                )
                progress = 50f
            } else {
                scope.launch {
                    toastHostState.showToast(
                        context.getString(R.string.something_went_wrong),
                        Icons.Rounded.ErrorOutline
                    )
                }
            }
        }
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(2)
        ) { uris ->
            uris.takeIf { it.isNotEmpty() }?.let {
                if (uris.size != 2) {
                    scope.launch {
                        toastHostState.showToast(
                            message = context.getString(R.string.pick_two_images),
                            icon = Icons.Rounded.ErrorOutline
                        )
                    }
                } else {
                    val newBeforeBitmap = context.getBitmapByUri(uris[0])
                    val newAfterBitmap = context.getBitmapByUri(uris[1])
                    if (newAfterBitmap != null && newBeforeBitmap != null) {
                        viewModel.updateBitmapData(
                            newBeforeBitmap = newBeforeBitmap,
                            newAfterBitmap = newAfterBitmap
                        )
                        progress = 50f
                    } else {
                        scope.launch {
                            toastHostState.showToast(
                                context.getString(R.string.something_went_wrong),
                                Icons.Rounded.ErrorOutline
                            )
                        }
                    }
                }
            }
        }

    val pickImage = {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()

    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (viewModel.bitmapData == null) {
                LargeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (navController.backstack.entries.isNotEmpty()) navController.pop()
                                onGoBack()
                                themeState.reset()
                            }
                        ) {
                            Icon(Icons.Rounded.ArrowBack, null)
                        }
                    },
                    title = {
                        Marquee(
                            edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        ) {
                            Text(stringResource(R.string.compare))
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ),
                    modifier = Modifier
                        .shadow(6.dp)
                        .zIndex(6f)
                )
            } else {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (navController.backstack.entries.isNotEmpty()) navController.pop()
                                onGoBack()
                                themeState.reset()
                            }
                        ) {
                            Icon(Icons.Rounded.ArrowBack, null)
                        }
                    },
                    title = {
                        Marquee(
                            edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        ) {
                            Text(stringResource(R.string.compare))
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ),
                    modifier = Modifier
                        .shadow(6.dp)
                        .zIndex(6f)
                )
            }
            AnimatedContent(targetState = viewModel.bitmapData) { data ->
                data?.let { (b, a) ->
                    val before = remember(data) { b?.asImageBitmap() }
                    val after = remember(data) { a?.asImageBitmap() }

                    if (before != null && after != null) {
                        BeforeAfterImage(
                            modifier = Modifier
                                .then(
                                    if (viewModel.bitmapData == null) Modifier.padding(bottom = 72.dp)
                                    else Modifier.padding(bottom = 88.dp)
                                )
                                .padding(16.dp)
                                .navigationBarsPadding()
                                .block(RoundedCornerShape(4.dp))
                                .padding(4.dp),
                            progress = animateFloatAsState(targetValue = progress).value,
                            onProgressChange = {
                                progress = it
                            },
                            beforeImage = before,
                            afterImage = after,
                            beforeLabel = { },
                            afterLabel = { }
                        )
                    }

                } ?: Column(Modifier.verticalScroll(scrollState)) {
                    ImageNotPickedWidget(
                        onPickImage = pickImage,
                        modifier = Modifier
                            .padding(bottom = 88.dp, top = 20.dp, start = 20.dp, end = 20.dp)
                            .navigationBarsPadding(),
                        text = stringResource(R.string.pick_two_images)
                    )
                }
            }
        }

        if (viewModel.bitmapData == null) {
            FloatingActionButton(
                onClick = pickImage,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val expanded = scrollState.isScrollingUp()
                val horizontalPadding by animateDpAsState(targetValue = if (expanded) 16.dp else 0.dp)
                Row(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, null)
                    AnimatedVisibility(visible = expanded) {
                        Row {
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.pick_images))
                        }
                    }
                }
            }
        } else {
            BottomAppBar(
                modifier = Modifier
                    .shadow(6.dp)
                    .zIndex(6f)
                    .align(Alignment.BottomCenter),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = pickImage,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null)
                    }
                },
                actions = {
                    Slider(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(100f, true),
                        value = animateFloatAsState(targetValue = progress).value,
                        onValueChange = {
                            progress = it
                        },
                        valueRange = 0f..100f
                    )
                }
            )
        }
    }

    if (viewModel.isLoading) LoadingDialog()

    BackHandler {
        if (navController.backstack.entries.isNotEmpty()) navController.pop()
        onGoBack()
        themeState.reset()
    }
}