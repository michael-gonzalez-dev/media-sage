package com.mediasage.data.analytics

/** Deliberately crashes the app so a real, uncaught exception reaches the platform's Crashlytics handler. */
expect fun triggerTestCrash(): Nothing
