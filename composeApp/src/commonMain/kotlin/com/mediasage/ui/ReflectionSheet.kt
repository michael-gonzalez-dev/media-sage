package com.mediasage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mediasage.theme.CardBorder
import com.mediasage.theme.Ink
import com.mediasage.theme.MediaSageTheme
import com.mediasage.theme.Navy
import com.mediasage.theme.SlateOnPaper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.comic_paper
import mediasage.composeapp.generated.resources.reflect_close_action
import mediasage.composeapp.generated.resources.reflect_discard_cancel
import mediasage.composeapp.generated.resources.reflect_discard_confirm
import mediasage.composeapp.generated.resources.reflect_discard_message
import mediasage.composeapp.generated.resources.reflect_discard_title
import mediasage.composeapp.generated.resources.reflect_note_hint
import mediasage.composeapp.generated.resources.reflect_paper_light
import mediasage.composeapp.generated.resources.reflect_sheet_subtitle
import mediasage.composeapp.generated.resources.reflect_save_action
import mediasage.composeapp.generated.resources.reflect_saved_confirmation
import mediasage.composeapp.generated.resources.reflect_sheet_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Reflect chip's bottom sheet — the reflection challenge question with a note field beneath
 * it. A stock [ModalBottomSheet] sized to its content (no partially-expanded stop), so it moves
 * with the sheet's own motion. The only customizations are the paper look (painted on the
 * content, with the bottom insets applied *inside* the paper so it extends behind the keyboard
 * and navigation bar) and the unsaved-changes confirmation. When [editable] is false (a past
 * briefing whose tone slot is no longer active), the note renders as read-only text and no
 * save/discard affordance is shown. [noteText] is `null` while the saved note is still loading
 * (the sheet opens on [challenge] alone, without waiting on it) — the body shows a loading
 * indicator in place of the field until it resolves.
 *
 * [SheetState]'s `confirmValueChange` — not `onDismissRequest` — is what gates a *drag* to
 * hidden: a swipe-to-dismiss settles the sheet to [SheetValue.Hidden] before `onDismissRequest`
 * ever fires, so vetoing there is the only way to keep the sheet visually open underneath the
 * discard dialog.
 *
 * Every close the sheet starts itself (Save, the close button, Discard) sets `isClosing`,
 * which waits for the keyboard to close, animates the sheet down, then calls [onDismiss].
 * Removing the sheet from composition without hiding it first skips the exit animation.
 * `isClosing` also lifts the discard veto for that hide, since `hasUnsavedChanges` may not have
 * recomposed to false yet when the animation starts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionSheet(
    challenge: String,
    noteText: String?,
    editable: Boolean,
    hasUnsavedChanges: Boolean,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }
    val hasUnsavedChangesState = rememberUpdatedState(hasUnsavedChanges)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target ->
            val shouldVeto = target == SheetValue.Hidden && hasUnsavedChangesState.value && !isClosing
            if (shouldVeto) showDiscardDialog = true
            !shouldVeto
        },
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (hasUnsavedChangesState.value && !isClosing) showDiscardDialog = true else onDismiss()
        },
        sheetState = sheetState,
        // Stops below the status bar like a native iOS sheet. Letting the sheet reach the top
        // made the top inset it consumes change with every pixel it moved, resizing the content
        // mid-drag and making a full-height sheet jump.
        modifier = modifier.statusBarsPadding(),
        containerColor = Color.Transparent,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = null,
    ) {
        // Read inside the sheet's content: ModalBottomSheet hosts it in its own window on Android,
        // so focus and keyboard insets read outside it belong to the screen behind the sheet.
        val focusManager = LocalFocusManager.current
        val imeInsets = WindowInsets.ime
        val density = LocalDensity.current
        LaunchedEffect(isClosing) {
            if (!isClosing) return@LaunchedEffect
            focusManager.clearFocus()
            // Hiding while the keyboard is still closing resizes the sheet mid-animation and
            // stalls it half-open, so let the keyboard finish first.
            withTimeoutOrNull(KEYBOARD_HIDE_TIMEOUT_MS) {
                snapshotFlow { imeInsets.getBottom(density) }.first { it == 0 }
            }
            sheetState.hide()
            onDismiss()
        }
        ReflectionSheetContent(
            challenge = challenge,
            noteText = noteText,
            editable = editable,
            hasUnsavedChanges = hasUnsavedChanges,
            onNoteChange = onNoteChange,
            onSave = {
                onSave()
                isClosing = true
            },
            onCloseClick = { if (hasUnsavedChanges) showDiscardDialog = true else isClosing = true },
        )
    }

    // Kept outside the sheet's content: shown from inside it, the dialog dismissed the sheet on
    // iOS instead of appearing over it.
    if (showDiscardDialog) {
        ReflectionDiscardDialog(
            onConfirm = {
                showDiscardDialog = false
                isClosing = true
            },
            onDismiss = { showDiscardDialog = false },
        )
    }
}

@Composable
private fun ReflectionDiscardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    MediaSageConfirmDialog(
        title = stringResource(Res.string.reflect_discard_title),
        message = stringResource(Res.string.reflect_discard_message),
        confirmLabel = stringResource(Res.string.reflect_discard_confirm),
        dismissLabel = stringResource(Res.string.reflect_discard_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ReflectionSheetContent(
    challenge: String,
    noteText: String?,
    editable: Boolean,
    hasUnsavedChanges: Boolean,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCloseClick: () -> Unit,
) {
    // The paper is always a light surface — a tan comic-paper texture in dark mode, white in
    // light mode — so its ink stays the same fixed dark-on-paper palette in both, matching
    // LoginScreen's formOnPaper convention. Only the painted texture itself switches on theme.
    val accentColor = Navy
    val bodyColor = Ink
    val mutedColor = SlateOnPaper
    val borderColor = CardBorder
    val paperImage = if (MediaSageTheme.isDark) Res.drawable.comic_paper else Res.drawable.reflect_paper_light

    // "Saved" reflects a real comparison against the last-saved text, not a one-shot flag from
    // the save action — so editing away from and then back to the saved value (e.g. deleting and
    // retyping the last character) correctly reads as saved again, with no separate state to sync.
    val showSavedLabel = !hasUnsavedChanges && noteText?.isNotBlank() == true

    val scrollState = rememberScrollState()
    var isFieldFocused by remember { mutableStateOf(false) }
    // The field displays its own locally-owned text, not `noteText` directly. `noteText` round-trips
    // through the ViewModel's StateFlow on every keystroke (onNoteChange copies the whole
    // UiState.Success tree, which recomposes back down as a new noteText) — on iOS, Compose
    // Multiplatform's software-keyboard input reconciles against the field's `value` each
    // recomposition, and driving that off a StateFlow round trip instead of local state made fast
    // typing outrun recomposition and skip characters. Seeding once, when the note finishes loading
    // (null -> non-null), keeps the field's displayed text purely local while still forwarding every
    // change to the ViewModel for save/unsaved-changes tracking.
    var fieldText by remember { mutableStateOf(noteText.orEmpty()) }
    var hasSeededFieldText by remember { mutableStateOf(false) }
    LaunchedEffect(noteText) {
        if (noteText != null && !hasSeededFieldText) {
            fieldText = noteText
            hasSeededFieldText = true
        }
    }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    // The note field grows with its content instead of scrolling internally, so once it's taller
    // than the visible area the newest typed line — and the cursor on it — can end up behind the
    // keyboard. BringIntoViewRequester asks the framework to scroll just enough to reveal the
    // field's current bounds, rather than a manually computed scrollState.animateScrollTo(maxValue)
    // on every keystroke — that approach raced the field's own height/cursor recalculation and was
    // the source of the visible cursor lagging behind newly typed characters once the note wrapped
    // past one line. Only while the field is actually focused, not on the initial load of an
    // existing note.
    LaunchedEffect(noteText, isFieldFocused) {
        if (isFieldFocused) bringIntoViewRequester.bringIntoView()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .paint(painter = painterResource(paperImage), contentScale = ContentScale.FillBounds)
            // The bottom insets the stock sheet would apply around its content, applied inside
            // the paper instead so the texture runs behind the keyboard and navigation bar.
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(horizontal = 20.dp),
    ) {
        ReflectionSheetHeader(
            titleColor = bodyColor,
            mutedColor = mutedColor,
            onCloseClick = onCloseClick,
        )
        // Only the body scrolls, so the handle and heading stay put while a long note scrolls
        // beneath them. weight(fill = false) lets the body shrink to the space left under the
        // header once the sheet reaches full height, instead of pushing the header off the top.
        Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(scrollState)) {
            Text(
                text = challenge,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = bodyColor,
                modifier = Modifier.padding(top = 8.dp),
            )
            Column(modifier = Modifier.padding(top = 16.dp)) {
                if (noteText == null) {
                    NoteLoadingIndicator(color = accentColor)
                } else if (editable) {
                    OutlinedTextField(
                        value = fieldText,
                        onValueChange = {
                            fieldText = it
                            onNoteChange(it)
                        },
                        placeholder = { Text(stringResource(Res.string.reflect_note_hint)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = bodyColor,
                            unfocusedTextColor = bodyColor,
                            disabledTextColor = bodyColor,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = borderColor,
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = mutedColor,
                            focusedPlaceholderColor = mutedColor,
                            unfocusedPlaceholderColor = mutedColor,
                            cursorColor = accentColor,
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusChanged { isFieldFocused = it.isFocused },
                    )
                    MediaSageSurface(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp),
                        shape = MaterialTheme.shapes.medium,
                        bordered = true,
                        shadowElevation = 2.dp,
                        enabled = hasUnsavedChanges,
                    ) { contentColor ->
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(
                                    if (showSavedLabel) Res.string.reflect_saved_confirmation else Res.string.reflect_save_action
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                            )
                        }
                    }
                } else if (noteText.isNotBlank()) {
                    ReflectionAnswerText(noteText = noteText, bodyColor = bodyColor, dividerColor = borderColor)
                }
            }
        }
    }
}

/**
 * The fixed top of the sheet — drag handle, title with the close button, and subtitle — ruled off
 * from the body so a scrolled note reads as passing beneath it rather than being cut off.
 */
@Composable
private fun ReflectionSheetHeader(titleColor: Color, mutedColor: Color, onCloseClick: () -> Unit) {
    ReflectionSheetHandle(color = mutedColor)
    Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(
            text = stringResource(Res.string.reflect_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
        )
        IconButton(onClick = onCloseClick, modifier = Modifier.align(Alignment.CenterEnd).size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.reflect_close_action),
                tint = mutedColor,
            )
        }
    }
    Text(
        text = stringResource(Res.string.reflect_sheet_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = mutedColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    // The handle's color rather than CardBorder, which nearly vanishes against the paper.
    HorizontalDivider(color = mutedColor, thickness = 1.dp, modifier = Modifier.padding(top = 12.dp))
}

/**
 * Replaces [ModalBottomSheet]'s default drag handle, which is drawn above the sheet content and
 * left the paper texture looking disconnected from it. Rendered as the first child inside the
 * painted paper [Column] instead, so the handle sits on the same continuous surface.
 */
@Composable
private fun ReflectionSheetHandle(color: Color) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}

/** Shown in place of the note field/answer text while the saved note is still loading. */
@Composable
private fun NoteLoadingIndicator(color: Color) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = color, modifier = Modifier.size(28.dp))
    }
}

/**
 * The read-only presentation of a past reflection's saved note — plain text, no text-field
 * chrome, set off from the AI-generated [challenge] above it by a divider and a bolder weight so
 * the human-written answer visually stands out.
 */
@Composable
private fun ReflectionAnswerText(noteText: String, bodyColor: Color, dividerColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))
        Text(
            text = noteText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = bodyColor,
        )
    }
}

/** Upper bound on waiting for the keyboard to close before the sheet slides down after a save. */
private const val KEYBOARD_HIDE_TIMEOUT_MS = 500L
