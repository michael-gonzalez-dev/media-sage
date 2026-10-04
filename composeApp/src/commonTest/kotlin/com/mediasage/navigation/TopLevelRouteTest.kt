package com.mediasage.navigation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TopLevelRouteTest {

    @Test
    fun everyTabRouteIsTopLevel() {
        TopLevelDestination.entries.forEach { destination ->
            assertTrue(isTopLevelRoute(destination.route), "${destination.route} should be top-level")
        }
    }

    @Test
    fun pushedRoutesAreNotTopLevel() {
        listOf(Route.FigureDetail(figureId = 1L), Route.Settings, Route.About, Route.Quotes).forEach { route ->
            assertFalse(isTopLevelRoute(route), "$route should not be top-level")
        }
    }
}
