package com.mediasage.feature.settings

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.mediasage.theme.MediaSageTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class AboutScreenRenderTest {

    @Test
    fun rendersAboutScreen() {
        captureRoboImage("build/outputs/roborazzi/about_screen.png") {
            MediaSageTheme {
                AboutScreen()
            }
        }
    }
}
