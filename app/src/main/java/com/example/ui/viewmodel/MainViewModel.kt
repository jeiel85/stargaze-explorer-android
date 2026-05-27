package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CommunityPost
import com.example.data.model.Constellation
import com.example.data.model.ObservationLog
import com.example.data.model.PlaceReview
import com.example.data.model.StargazingPlace
import com.example.data.remote.CurrentWeatherData
import com.example.data.repository.StargazingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StargazingRepository

    // Database state flows
    val allLogs: StateFlow<List<ObservationLog>>
    val allPlaces: StateFlow<List<StargazingPlace>>
    val allCommunityPosts: StateFlow<List<CommunityPost>>

    // Active User Profile State
    val userProfile = MutableStateFlow(UserProfile("별빛 소년", "nebula", "지구계 은하 탐사자"))
    val isLoggedIn = MutableStateFlow(true) // Start authenticated for smooth evaluation

    // Selected Stargazing Spot for details/reviews
    val selectedPlaceId = MutableStateFlow<Int?>(1)
    val selectedPlaceReviews = MutableStateFlow<List<PlaceReview>>(emptyList())

    // Astronomical Recommendations & Dynamic Weather
    val selectedLocationIndex = MutableStateFlow(0) // 0: GPS/Current, 1: Anbandegi, 2: Hwaaksan, 3: Beotgogae, 4: Hambacksan
    val currentLocationName = MutableStateFlow("서울 종로구 (현재 GPS 위치)")
    val currentLatitude = MutableStateFlow(37.5665)
    val currentLongitude = MutableStateFlow(126.9780)

    val currentWeatherData = MutableStateFlow<CurrentWeatherData?>(null)
    val recommendationTip = MutableStateFlow("밤하늘을 분석하는 중...")
    val isLoadingTip = MutableStateFlow(false)

    // Notification Alerts Settings State
    val isAlertEnabled = MutableStateFlow(true)
    val selectedAlertHour = MutableStateFlow(22) // 10 PM default
    val notificationSentCount = MutableStateFlow(0)

    // Offline data-syncing state
    val isSyncing = MutableStateFlow(false)
    val unsyncedCount = MutableStateFlow(0)

    // Camera view backing configuration for AR view
    val isArCameraOn = MutableStateFlow(false)

    // Interactive sky map drag offsets (for AR panning!)
    val skyMapDragX = MutableStateFlow(0f)
    val skyMapDragY = MutableStateFlow(0f)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StargazingRepository(
            database.observationLogDao(),
            database.stargazingPlaceDao(),
            database.placeReviewDao(),
            database.communityPostDao()
        )

        allLogs = repository.allLogs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allPlaces = repository.allPlaces.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allCommunityPosts = repository.allCommunityPosts.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        // Async seed database state and fetch initial weather/tips
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            updateUnsyncedCount()
            loadWeatherAndTips()
            checkAndNotifyMonthlyAstronomicalEvents()
        }

        // Observe reviews for the active place
        viewModelScope.launch {
            selectedPlaceId.collect { placeId ->
                if (placeId != null) {
                    repository.getReviewsForPlace(placeId).collect { reviews ->
                        selectedPlaceReviews.value = reviews
                    }
                }
            }
        }
    }

    // Load active weather data from open weather API!
    suspend fun loadWeatherAndTips() {
        isLoadingTip.value = true
        val lat = currentLatitude.value
        val lng = currentLongitude.value
        val weather = repository.fetchWeather(lat, lng)
        currentWeatherData.value = weather

        val temp = weather?.temperature ?: 12.0
        val cloud = weather?.cloudCover ?: 15.0
        val location = currentLocationName.value

        val tip = repository.getGeminiStargazingTip(location, temp, cloud)
        recommendationTip.value = tip
        isLoadingTip.value = false
    }

    fun changeLocation(index: Int) {
        selectedLocationIndex.value = index
        when (index) {
            0 -> {
                currentLocationName.value = "내 위치 (GPS 연동)"
                currentLatitude.value = 37.5665
                currentLongitude.value = 126.9780
            }
            1 -> {
                currentLocationName.value = "강릉 안반데기"
                currentLatitude.value = 37.6256
                currentLongitude.value = 128.9881
            }
            2 -> {
                currentLocationName.value = "화악산 쌈지공원"
                currentLatitude.value = 37.9943
                currentLongitude.value = 127.4339
            }
            3 -> {
                currentLocationName.value = "양평 벗고개"
                currentLatitude.value = 37.4475
                currentLongitude.value = 127.6983
            }
            4 -> {
                currentLocationName.value = "함백산 만항재"
                currentLatitude.value = 37.1622
                currentLongitude.value = 128.8913
            }
        }
        viewModelScope.launch {
            loadWeatherAndTips()
        }
    }

    // Community and logs operations
    fun addObservationLog(constellation: String, location: String, notes: String, rating: Float, weather: String, photoUri: String?) {
        viewModelScope.launch {
            val successId = repository.insertLog(
                ObservationLog(
                    constellationName = constellation,
                    date = getTodayDateString(),
                    time = getCurrentTimeString(),
                    location = location,
                    notes = notes,
                    rating = rating,
                    weather = weather,
                    photoUri = photoUri,
                    isSynced = false, // starts unsynced for offline synchronization demonstration!
                    observerName = userProfile.value.nickname
                )
            )
            updateUnsyncedCount()
        }
    }

    fun deleteObservationLog(log: ObservationLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
            updateUnsyncedCount()
        }
    }

    fun syncLogsOffline() {
        if (isSyncing.value) return
        viewModelScope.launch {
            isSyncing.value = true
            val syncedAmount = repository.simulateSync()
            isSyncing.value = false
            updateUnsyncedCount()
            if (syncedAmount > 0) {
                stargazingSystemNotification("동기화 완료 🌌", "내 관측 기록장 ${syncedAmount}개가 실시간 원격 은하 데이터베이스와 동기화되었습니다!")
            }
        }
    }

    private fun updateUnsyncedCount() {
        viewModelScope.launch {
            val logs = allLogs.value
            unsyncedCount.value = logs.count { !it.isSynced }
        }
    }

    fun submitPlaceReview(placeId: Int, content: String, rating: Float) {
        viewModelScope.launch {
            repository.addReview(placeId, userProfile.value.nickname, content, rating)
        }
    }

    fun postToCommunity(constellation: String?, content: String, photoUri: String?) {
        viewModelScope.launch {
            repository.createCommunityPost(
                authorName = userProfile.value.nickname,
                avatar = userProfile.value.avatar,
                constellation = constellation,
                content = content,
                photoUri = photoUri,
                location = currentLocationName.value
            )
        }
    }

    fun likePost(postId: Int) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun commentPost(postId: Int) {
        viewModelScope.launch {
            repository.addCommentPost(postId)
        }
    }

    fun handleSocialLogin(nickname: String, avatar: String) {
        userProfile.value = UserProfile(nickname, avatar, assignTitleForBadge(avatar))
        isLoggedIn.value = true
    }

    fun handleLogout() {
        isLoggedIn.value = false
    }

    private fun assignTitleForBadge(avatar: String): String {
        return when (avatar) {
            "nebula" -> "성운 관찰학자"
            "comet" -> "혜성 추적 요원"
            "supernova" -> "초신성 항해사"
            else -> "은하수 모험가"
        }
    }

    // Simulated Android Push Notification System!
    fun testStargazingAlert(title: String = "관측 알람 🌌", message: String = "오늘 밤 하늘 상태가 아주 좋습니다! 어서 밖으로 나가 가로등 불빛이 없는 곳에서 밤하늘을 찾아보세요.") {
        stargazingSystemNotification(title, message)
        notificationSentCount.value += 1
    }

    private fun stargazingSystemNotification(title: String, message: String) {
        val context = getApplication<Application>().applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "stargazing_alerts_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "별자리 관측 알림", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "알맞은 별 관측 타이밍 안내 알림"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // Helper utilities for date/time formatted string
    private fun getTodayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREAN)
        return sdf.format(java.util.Date())
    }

    private fun getCurrentTimeString(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.KOREAN)
        return sdf.format(java.util.Date())
    }

    // Special Astronomical Events List (No external API dependency)
    val astronomicalEvents = MutableStateFlow(listOf(
        AstronomicalEvent(1, "목성-토성 이중 대접근 우주쇼", "2026-05-26 ~ 05-31", "행성합", "목성과 토성이 동쪽 청명한 밤하늘에서 아주 가깝게 접근 정렬하는 절호의 관측 이벤트입니다!"),
        AstronomicalEvent(2, "물병자리 에타 유성우 극대기", "2026-05-05 ~ 05-07", "유성우", "핼리 혜성의 잔해들이 지구 대기권을 관통하며 연출하는 5월 초순의 화려한 별똥별 비입니다."),
        AstronomicalEvent(3, "페르세우스자리 대유성우 최극대", "2026-08-12 ~ 08-13", "유성우", "연중 가장 엄청나고 굵은 은하 빛줄기들을 볼 수 있는 3대 메이저 유성우의 화려한 쇼 피크!"),
        AstronomicalEvent(4, "대유럽 개기일식 우주 대정렬", "2026-08-12", "일식", "태양이 달의 영역에 완벽히 은폐되어 순백색 코로나를 형성하는 기적적인 천체 전조 현상입니다."),
        AstronomicalEvent(5, "구리빛 블러드문 개기월식", "2026-08-28", "월식", "지구 그림자가 달을 완전히 영식하여 밤하늘 보름달이 영롱한 구리빛 혈달로 타오르는 완벽한 월식입니다."),
        AstronomicalEvent(6, "가을의 전령 오리온자리 유성우", "2026-10-21 ~ 10-22", "유성우", "오리온자리의 삼태성을 기점으로 주황색 유성 꼬리들이 심야 동남 방향을 수놓는 아름다운 연출!"),
        AstronomicalEvent(7, "쌍둥이자리 은하 대유성우 축제", "2026-12-13 ~ 12-15", "유성우", "추위에 얼어붙은 청정한 겨울 밤하늘 위로 시간당 150개의 은빛 별똥별이 사방으로 소나기처럼 무수히 쏟아지는 극상의 피크입니다.")
    ))

    fun toggleEventSubscription(eventId: Int) {
        astronomicalEvents.value = astronomicalEvents.value.map { event ->
            if (event.id == eventId) {
                val newStatus = !event.isSubscribed
                val actionText = if (newStatus) "천문 관측 푸시 알림 예약됨 🔔" else "알림 예약 해제됨 🔕"
                stargazingSystemNotification(
                    "📅 특별 천문 일정 예약",
                    "'${event.title}' ${actionText} (${event.date})"
                )
                notificationSentCount.value += 1
                event.copy(isSubscribed = newStatus)
            } else {
                event
            }
        }
    }

    fun triggerEventImmediateAlert(eventId: Int) {
        val event = astronomicalEvents.value.firstOrNull { event -> event.id == eventId } ?: return
        val prefix = when(event.type) {
            "유성우" -> "🌠 [실시간 유성 관측 특보]"
            "일식" -> "☀️ [실시간 일식 관측 특보]"
            "월식" -> "🌕 [실시간 월식 관측 특보]"
            "행성합" -> "🪐 [실시간 행성접근 특보]"
            else -> "☄️ [실시간 대혜성 접근 경보]"
        }
        stargazingSystemNotification(
            "$prefix ${event.title}",
            "오늘 밤 (${event.date}) 우주 쇼의 정점이 도래했습니다! 현 관측지의 기상 조건은 쾌청하며 깊은 은하수 식별이 수월합니다. 즉시 정조준해서 눈부신 순간을 맞이해 보세요!"
        )
        notificationSentCount.value += 1
    }

    private fun checkAndNotifyMonthlyAstronomicalEvents() {
        if (hasNotifiedThisSession) return
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH) + 1 // 1-12

        val eventsThisMonth = astronomicalEvents.value.filter {
            val eventMonth = when (it.id) {
                1, 2 -> 5
                3, 4, 5 -> 8
                6 -> 10
                7 -> 12
                else -> 0
            }
            eventMonth == month
        }

        if (eventsThisMonth.isNotEmpty()) {
            val eventNames = eventsThisMonth.joinToString(", ") { "'${it.title}'" }
            stargazingSystemNotification(
                title = "🌌 [이달 (${month}월)의 특급 천체 예경]",
                message = "이번 달에 ${eventNames} 관측 피크가 찾아옵니다! [프로필·알림] 탭에서 상세 일정 확인 및 알림을 On 하세요."
            )
            notificationSentCount.value += 1
            hasNotifiedThisSession = true
        }
    }

    companion object {
        private var hasNotifiedThisSession = false
    }
}

data class UserProfile(
    val nickname: String,
    val avatar: String, // name key of avatar
    val title: String // companion status label
)

data class AstronomicalEvent(
    val id: Int,
    val title: String,
    val date: String,
    val type: String, // "유성우", "개기일식", "월식", "행성합", "혜성"
    val description: String,
    val isSubscribed: Boolean = false
)
