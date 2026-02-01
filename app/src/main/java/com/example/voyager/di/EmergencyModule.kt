package com.example.voyager.di

import android.content.Context
import com.example.voyager.data.local.datastore.EmergencyContactsStore
import com.example.voyager.data.repository.LocationRepository
import com.example.voyager.data.repository.OfflineEmergencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EmergencyModule {

    @Provides
    @Singleton
    fun provideEmergencyContactsStore(
        @ApplicationContext context: Context
    ): EmergencyContactsStore {
        return EmergencyContactsStore(context)
    }

    @Provides
    @Singleton
    fun provideOfflineEmergencyRepository(
        @ApplicationContext context: Context,
        locationRepository: LocationRepository,  // Changed from lastLocationCache
        emergencyContactsStore: EmergencyContactsStore
    ): OfflineEmergencyRepository {
        return OfflineEmergencyRepository(
            context = context,
            locationRepository = locationRepository,  // Changed parameter name
            emergencyContactsStore = emergencyContactsStore
        )
    }
}