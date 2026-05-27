package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CommunityPost
import com.example.data.model.ObservationLog
import com.example.data.model.PlaceReview
import com.example.data.model.StargazingPlace

@Database(
    entities = [
        ObservationLog::class,
        StargazingPlace::class,
        PlaceReview::class,
        CommunityPost::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun observationLogDao(): ObservationLogDao
    abstract fun stargazingPlaceDao(): StargazingPlaceDao
    abstract fun placeReviewDao(): PlaceReviewDao
    abstract fun communityPostDao(): CommunityPostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "star_guide_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
