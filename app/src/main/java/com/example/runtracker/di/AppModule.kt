package com.example.runtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runtracker.db.RunDatabase
import com.example.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtracker.utils.Constants.KEY_NAME
import com.example.runtracker.utils.Constants.KEY_WEIGHT
import com.example.runtracker.utils.Constants.RUN_DATABASE_NAME
import com.example.runtracker.utils.Constants.SHARED_PREFERENCE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunDatabase(
        @ApplicationContext app:Context
    )= Room.databaseBuilder(
        app,
        RunDatabase::class.java,
        RUN_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db:RunDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext app:Context) =
            app.getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
            sharedPreferences.getString(KEY_NAME,"")?:""

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
            sharedPreferences.getFloat(KEY_WEIGHT,0f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) =
            sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE,true)



}