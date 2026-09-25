package com.siaka.di

import android.content.Context
import androidx.room.Room
import com.siaka.data.local.RouteDao
import com.siaka.data.local.CompletedRideDao
import com.siaka.data.local.CyclePathDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CyclePathDatabase {
        return Room.databaseBuilder(
            context,
            CyclePathDatabase::class.java,
            // Preserve the original database file during the app rename.
            "siaka_database"
        )
            .build()
    }

    @Provides
    fun provideRouteDao(database: CyclePathDatabase): RouteDao {
        return database.routeDao()
    }

    @Provides
    fun provideCompletedRideDao(database: CyclePathDatabase): CompletedRideDao {
        return database.completedRideDao()
    }
}
