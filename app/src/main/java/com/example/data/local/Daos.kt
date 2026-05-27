package com.example.data.local

import androidx.room.*
import com.example.data.model.CommunityPost
import com.example.data.model.ObservationLog
import com.example.data.model.PlaceReview
import com.example.data.model.StargazingPlace
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationLogDao {
    @Query("SELECT * FROM observation_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ObservationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ObservationLog): Long

    @Delete
    suspend fun deleteLog(log: ObservationLog)

    @Query("SELECT * FROM observation_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<ObservationLog>

    @Query("UPDATE observation_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markLogsSynced(ids: List<Int>)
}

@Dao
interface StargazingPlaceDao {
    @Query("SELECT * FROM stargazing_places ORDER BY id ASC")
    fun getAllPlaces(): Flow<List<StargazingPlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<StargazingPlace>)

    @Query("SELECT * FROM stargazing_places WHERE id = :placeId")
    suspend fun getPlaceById(placeId: Int): StargazingPlace?

    @Query("UPDATE stargazing_places SET ratingSum = ratingSum + :rating, ratingCount = ratingCount + 1 WHERE id = :placeId")
    suspend fun addReviewRating(placeId: Int, rating: Float)
}

@Dao
interface PlaceReviewDao {
    @Query("SELECT * FROM place_reviews WHERE placeId = :placeId ORDER BY timestamp DESC")
    fun getReviewsByPlaceId(placeId: Int): Flow<List<PlaceReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: PlaceReview)
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Delete
    suspend fun deletePost(post: CommunityPost)

    @Query("UPDATE community_posts SET likesCount = likesCount + :delta WHERE id = :postId")
    suspend fun updateLikes(postId: Int, delta: Int)

    @Query("UPDATE community_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementComments(postId: Int)
}
