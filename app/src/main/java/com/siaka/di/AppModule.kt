package com.siaka.di

import android.content.Context
import androidx.room.Room
import com.siaka.data.local.RouteDao
import com.siaka.data.local.SiakaDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): SiakaDatabase {
        return Room.databaseBuilder(
            context,
            SiakaDatabase::class.java,
            "siaka_database"
        ).build()
    }

    @Provides
    fun provideRouteDao(database: SiakaDatabase): RouteDao {
        return database.routeDao()
    }
}
