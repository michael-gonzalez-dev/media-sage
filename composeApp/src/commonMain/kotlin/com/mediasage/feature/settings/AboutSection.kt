package com.mediasage.feature.settings

import kotlinx.serialization.Serializable
import mediasage.composeapp.generated.resources.Res
import mediasage.composeapp.generated.resources.about_disclaimer_body
import mediasage.composeapp.generated.resources.about_disclaimer_title
import mediasage.composeapp.generated.resources.about_studio_body
import mediasage.composeapp.generated.resources.about_studio_title
import mediasage.composeapp.generated.resources.about_why_body
import mediasage.composeapp.generated.resources.about_why_title
import org.jetbrains.compose.resources.StringResource

/** A long-form About page, each opened from its own row on the About screen. */
@Serializable
enum class AboutSection(val title: StringResource, val body: StringResource) {
    WHY(Res.string.about_why_title, Res.string.about_why_body),
    STUDIO(Res.string.about_studio_title, Res.string.about_studio_body),
    DISCLAIMER(Res.string.about_disclaimer_title, Res.string.about_disclaimer_body),
}
