package com.mediasage.data.analytics

actual fun triggerTestCrash(): Nothing = throw RuntimeException("MS-683 test crash — Android")
