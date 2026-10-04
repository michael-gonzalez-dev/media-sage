package com.mediasage.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.mediasage.data.ReporterViewPreferencesRepository
import okio.Path.Companion.toPath
import org.koin.dsl.module

val reportersModule = module {
    single {
        ReporterViewPreferencesRepository(
            PreferenceDataStoreFactory.createWithPath {
                val context: Context = get()
                context.filesDir.resolve(ReporterViewPreferencesRepository.FILE_NAME).absolutePath.toPath()
            }
        )
    }
}
