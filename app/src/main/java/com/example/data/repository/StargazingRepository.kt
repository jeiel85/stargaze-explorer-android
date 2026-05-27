package com.example.data.repository

import com.example.data.local.CommunityPostDao
import com.example.data.local.ObservationLogDao
import com.example.data.local.PlaceReviewDao
import com.example.data.local.StargazingPlaceDao
import com.example.data.model.CommunityPost
import com.example.data.model.ObservationLog
import com.example.data.model.PlaceReview
import com.example.data.model.StargazingPlace
import com.example.data.remote.CurrentWeatherData
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.WeatherApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StargazingRepository(
    private val logDao: ObservationLogDao,
    private val placeDao: StargazingPlaceDao,
    private val reviewDao: PlaceReviewDao,
    private val postDao: CommunityPostDao
) {
    // 1. Observation Logs
    val allLogs: Flow<List<ObservationLog>> = logDao.getAllLogs()

    suspend fun insertLog(log: ObservationLog): Long = withContext(Dispatchers.IO) {
        logDao.insertLog(log)
    }

    suspend fun deleteLog(log: ObservationLog) = withContext(Dispatchers.IO) {
        logDao.deleteLog(log)
    }

    // Offline database sync simulation!
    suspend fun simulateSync(): Int = withContext(Dispatchers.IO) {
        val unsynced = logDao.getUnsyncedLogs()
        if (unsynced.isEmpty()) return@withContext 0

        // In a real app we would post logs to a API server here.
        // We simulate a 2-second server network delay.
        kotlinx.coroutines.delay(1800)
        
        // After sending, mark all of them as synced!
        val ids = unsynced.map { it.id }
        logDao.markLogsSynced(ids)

        // Also, automatically post a sync accomplishment to community feed for extra realistic integration!
        unsynced.forEach { log ->
            postDao.insertPost(
                CommunityPost(
                    authorName = log.observerName,
                    authorAvatar = "shooting_star",
                    constellationName = log.constellationName,
                    content = "🌌 방금 오프라인 관측 도감 동기화를 마쳤습니다! ${log.location}에서 목격한 아름다운 ${log.constellationName} 기록입니다: \"${log.notes}\"",
                    photoUri = log.photoUri,
                    timestamp = log.timestamp,
                    location = log.location,
                    likesCount = 3,
                    commentsCount = 1
                )
            )
        }
        
        return@withContext unsynced.size
    }

    // 2. Stargazing Places
    val allPlaces: Flow<List<StargazingPlace>> = placeDao.getAllPlaces()

    suspend fun getPlaceById(placeId: Int): StargazingPlace? = withContext(Dispatchers.IO) {
        placeDao.getPlaceById(placeId)
    }

    suspend fun addReview(placeId: Int, author: String, content: String, rating: Float) = withContext(Dispatchers.IO) {
        // Insert review
        reviewDao.insertReview(
            PlaceReview(placeId = placeId, authorName = author, content = content, rating = rating)
        )
        // Accumulate ratings onto the place entity
        placeDao.addReviewRating(placeId, rating)
    }

    fun getReviewsForPlace(placeId: Int): Flow<List<PlaceReview>> {
        return reviewDao.getReviewsByPlaceId(placeId)
    }

    // Seeding default places and mock data if missing
    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        val existingSpots = placeDao.getAllPlaces().firstOrNull()
        if (existingSpots.isNullOrEmpty()) {
            // Seed places
            placeDao.insertPlaces(StargazingPlace.DEFAULT_PLACES)
            
            // Seed a few default reviews
            reviewDao.insertReview(PlaceReview(placeId = 1, authorName = "은하수홀릭", content = "안반데기 은하수 전망대는 정말 최고입니다. 은하수 선명함이 달라요!", rating = 5f))
            reviewDao.insertReview(PlaceReview(placeId = 1, authorName = "캠핑의달인", content = "주말엔 차가 많으니 평일 관측을 강추해드립니다.", rating = 4f))
            reviewDao.insertReview(PlaceReview(placeId = 2, authorName = "가평길잡이", content = "화악산 터널 가기 직전 정자가 명당입니다! 추워요 신세계임.", rating = 5.0f))
            reviewDao.insertReview(PlaceReview(placeId = 3, authorName = "포토그래퍼K", content = "벗고개는 터널 뚫린 샷 구도 잡는 게 미쳤어요. 가성비 좋은 곳.", rating = 4.0f))

            // Seed mock community posts to make feed super lively right away!
            postDao.insertPost(
                CommunityPost(
                    authorName = "은하수냥",
                    authorAvatar = "comet",
                    constellationName = "오리온자리",
                    content = "강릉 안반데기 가본 분 계신가요? 어제 다녀왔는데 구름이 걷히며 오리온자리 삼태성이 쏟아질 것처럼 빛났어요! 맑은 밤하늘이 주신 최고의 선물이었습니다. 🌌✨",
                    photoUri = "sim_orion", // flag to display simulated gorgeous space illustrations in coil
                    timestamp = System.currentTimeMillis() - 7200000, // 2 hrs ago
                    location = "강릉 안반데기",
                    likesCount = 14,
                    commentsCount = 3
                )
            )
            postDao.insertPost(
                CommunityPost(
                    authorName = "별빛소년",
                    authorAvatar = "nebula",
                    constellationName = "카시오페아자리",
                    content = "양평 벗고개 터널 근처에서 밤하늘 관측 중! 카시오페아자리의 선명한 W 구조를 두 눈으로 직접 포착했습니다. 가슴이 벅차오르네요.",
                    photoUri = "sim_cassiopeia",
                    timestamp = System.currentTimeMillis() - 14400000, // 4 hrs ago
                    location = "양평 벗고개",
                    likesCount = 8,
                    commentsCount = 1
                )
            )
        }
    }

    // 3. Community Feed
    val allCommunityPosts: Flow<List<CommunityPost>> = postDao.getAllPosts()

    suspend fun createCommunityPost(authorName: String, avatar: String, constellation: String?, content: String, photoUri: String?, location: String?) = withContext(Dispatchers.IO) {
        postDao.insertPost(
            CommunityPost(
                authorName = authorName,
                authorAvatar = avatar,
                constellationName = constellation,
                content = content,
                photoUri = photoUri,
                location = location,
                likesCount = 0,
                commentsCount = 0
            )
        )
    }

    suspend fun toggleLikePost(postId: Int) = withContext(Dispatchers.IO) {
        postDao.updateLikes(postId, 1)
    }

    suspend fun addCommentPost(postId: Int) = withContext(Dispatchers.IO) {
        postDao.incrementComments(postId)
    }

    // 4. Remote Data (Real Weather API + Gemini API)
    suspend fun fetchWeather(lat: Double, lng: Double): CurrentWeatherData? = withContext(Dispatchers.IO) {
        try {
            val response = WeatherApiClient.service.getStargazingWeather(lat, lng)
            response.current
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGeminiStargazingTip(location: String, temp: Double, cloud: Double): String = withContext(Dispatchers.IO) {
        GeminiApiClient.queryGeminiRecommendation(location, temp, cloud)
    }
}
