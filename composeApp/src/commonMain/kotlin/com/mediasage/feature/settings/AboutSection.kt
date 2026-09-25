package com.mediasage.feature.settings

import kotlinx.serialization.Serializable
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.about_disclaimer_ai_heading
import mediasage.composeapp.generated.resources.about_disclaimer_figures_body
import mediasage.composeapp.generated.resources.about_disclaimer_figures_heading
import mediasage.composeapp.generated.resources.about_disclaimer_portraits_body
import mediasage.composeapp.generated.resources.about_disclaimer_portraits_label
import mediasage.composeapp.generated.resources.about_disclaimer_quotes_body
import mediasage.composeapp.generated.resources.about_disclaimer_quotes_label
import mediasage.composeapp.generated.resources.about_disclaimer_reflections_body
import mediasage.composeapp.generated.resources.about_disclaimer_reflections_label
import mediasage.composeapp.generated.resources.about_disclaimer_title
import mediasage.composeapp.generated.resources.about_disclaimer_why_body
import mediasage.composeapp.generated.resources.about_disclaimer_why_heading
import mediasage.composeapp.generated.resources.about_studio_body
import mediasage.composeapp.generated.resources.about_studio_title
import mediasage.composeapp.generated.resources.about_why_body
import mediasage.composeapp.generated.resources.about_why_title
import org.jetbrains.compose.resources.StringResource

/** One piece of a long-form About page, rendered in order by [AboutDetailScreen]. */
sealed interface AboutBlock {
    data class Heading(val text: StringResource) : AboutBlock
    data class Paragraph(val text: StringResource) : AboutBlock

    /** A paragraph that opens with a bold lead-in label, for scannable lists like "Quotes." / "Portraits.". */
    data class LabeledItem(val label: StringResource, val text: StringResource) : AboutBlock
}

/** A long-form About page, each opened from its own row on the About screen. */
@Serializable
enum class AboutSection(val title: StringResource, val blocks: List<AboutBlock>) {
    WHY(Res.string.about_why_title, listOf(AboutBlock.Paragraph(Res.string.about_why_body))),
    STUDIO(Res.string.about_studio_title, listOf(AboutBlock.Paragraph(Res.string.about_studio_body))),
    DISCLAIMER(
        Res.string.about_disclaimer_title,
        listOf(
            AboutBlock.Heading(Res.string.about_disclaimer_figures_heading),
            AboutBlock.Paragraph(Res.string.about_disclaimer_figures_body),
            AboutBlock.Heading(Res.string.about_disclaimer_ai_heading),
            AboutBlock.LabeledItem(Res.string.about_disclaimer_quotes_label, Res.string.about_disclaimer_quotes_body),
            AboutBlock.LabeledItem(
                Res.string.about_disclaimer_reflections_label,
                Res.string.about_disclaimer_reflections_body,
            ),
            AboutBlock.LabeledItem(Res.string.about_disclaimer_portraits_label, Res.string.about_disclaimer_portraits_body),
            AboutBlock.Heading(Res.string.about_disclaimer_why_heading),
            AboutBlock.Paragraph(Res.string.about_disclaimer_why_body),
        ),
    ),
}
