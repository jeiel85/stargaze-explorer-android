package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Constellation(
    val name: String,
    val brightestStar: String,
    val bestSeason: String,
    val abbreviation: String,
    val description: String,
    val mythology: String,
    val xOffset: Float, // coordinate position in sky map
    val yOffset: Float
) {
    companion object {
        val ALL = listOf(
            Constellation("오리온자리", "리겔", "겨울", "Ori", "밤하늘에서 가장 식별하기 쉬운 대표적인 겨울 별자리입니다. 사냥꾼 오리온의 모습을 하고 있습니다.", "그리스 신화의 위대한 사냥꾼 오리온은 바다의 신 포세이돈의 아들로 거만함 때문에 전갈의 침에 찔려 죽었습니다.", 0.2f, -0.1f),
            Constellation("큰곰자리", "알리오스", "봄", "UMa", "북두칠성을 포함하고 있으며 북반구에서 사계절 내내 볼 수 있는 거대한 별자리입니다.", "제우스의 사랑을 받은 님프 칼리스토가 헤라의 질투로 곰이 되었으며, 나중에 하늘에 올라 별자리가 되었습니다.", -0.3f, 0.6f),
            Constellation("카시오페아자리", "셰다르", "가을", "Cas", "북극성 반대편에서 'W'자 모양을 그리며 빛나는 가을철 대표 별자리로 방향 찾기의 기준이 됩니다.", "그리스 신화 속 에티오피아의 왕비 카시오페아의 고고한 허영심에 대한 처벌로 하늘에 거꾸로 매달리게 된 모습입니다.", 0.5f, 0.7f),
            Constellation("황소자리", "알데바란", "겨울", "Tau", "붉은 눈을 가진 황소 머리 모양을 하고 있으며 플레이아데스 성단을 품고 있습니다.", "제우스가 아름다운 에우로페를 납치해 크레타 섬으로 달아나기 위해 변신한 눈부신 흰 황소의 모습입니다.", -0.1f, -0.3f),
            Constellation("사자자리", "레굴루스", "봄", "Leo", "봄을 알리는 대표적 별자리이자 밤하늘에서 거대한 물음표 혹은 낫 모양을 그리며 빛납니다.", "헤라클레스가 그의 12가지 과업 중 첫 번째로 네메아 골짜기에서 해치운 거대하고 불사신인 황금 사자입니다.", -0.5f, -0.1f),
            Constellation("백조자리", "데네브", "여름", "Cyg", "은하수 위에 날개를 활짝 편 십자가 모양(북십자성)으로 빛나는 여름 밤의 보석입니다.", "제우스가 레다 왕비를 비밀리에 만나 사랑을 나누기 위해 몸을 숨겼던 우아한 물새 백조의 변신 모습입니다.", 0.1f, 0.4f),
            Constellation("전갈자리", "안타레스", "여름", "Sco", "독침을 곧추세운 굽은 전갈의 곡선이 은하수 한가운데에서 아름다운 붉은 빛으로 넘실거립니다.", "오만을 떨던 사냥꾼 오리온을 징벌하기 위해 보낸 전갈로, 지금도 하늘에서 둘은 마주치지 않고 서로 피합니다.", 0.6f, -0.6f),
            Constellation("거문고자리", "베가(직녀성)", "여름", "Lyr", "여름철 대삼각형의 일원이자 밤하늘에서 다섯 번째로 밝고 푸른 직녀성을 주성으로 합니다.", "음악의 명수 오르페우스가 죽은 아내 에우리디케를 구하러 지하세계로 갈 때 연주했던 황금 거문고입니다.", -0.1f, 0.3f),
            Constellation("독수리자리", "알타이르(견우성)", "여름", "Aql", "직녀성과 은하수를 사이에 두고 반대편에서 반짝이는 견우성을 거느린 용맹한 조류의 모형입니다.", "제우스의 번개를 맡아 나르거나, 청춘의 여신 헤베의 후임인 가니메데를 올림포스로 납치할 때 활약했던 독수리입니다.", 0.3f, 0.1f)
        )
    }
}

@Entity(tableName = "observation_logs")
data class ObservationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val constellationName: String,
    val date: String,
    val time: String,
    val location: String,
    val notes: String,
    val rating: Float, // 1.0 to 5.0 scale
    val weather: String, // e.g., "맑음", "구름조금", "안개"
    val photoUri: String?, // local path or simulated URI
    val isSynced: Boolean = false, // simulated offline-sync flag
    val observerName: String = "Stargazer",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stargazing_places")
data class StargazingPlace(
    @PrimaryKey val id: Int,
    val name: String,
    val address: String,
    val description: String,
    val lightPollution: String, // e.g., "매우 낮음(보틀 1등급)", "극히 적음(보틀 2등급)"
    val altitude: String, // e.g., "해발 1,110m"
    val tip: String, // Tip for visit
    val latitude: Double,
    val longitude: Double,
    val ratingSum: Float = 15f,
    val ratingCount: Int = 3
) {
    val averageRating: Float
        get() = if (ratingCount > 0) ratingSum / ratingCount else 0.0f

    companion object {
        val DEFAULT_PLACES = listOf(
            StargazingPlace(
                id = 1,
                name = "강릉 안반데기",
                address = "강원특별자치도 강릉시 왕산면 안반데기길 428",
                description = "해발 1,100m의 고랭지 배추밭으로, 국내에서 불빛이 가로막지 않는 은하수를 찍을 수 있는 성지 중의 성지입니다.",
                lightPollution = "매우 낮음 (보틀 클래스 2)",
                altitude = "해발 1,100m",
                tip = "차량 진입이 가능하지만 밤에는 상향등을 켜면 강한 민폐가 됩니다. 겨울에는 극도로 추우니 패딩이 필수입니다.",
                latitude = 37.6256,
                longitude = 128.9881,
                ratingSum = 14.5f,
                ratingCount = 3
            ),
            StargazingPlace(
                id = 2,
                name = "화악산 쌈지공원",
                address = "경기도 가평군 북면 화악산로 1196",
                description = "경기권에서 가장 유명한 은하수 관측 및 차박 성지로, 터널 부근의 쌈지공원 정자 주변에서 맑은 밤하늘을 볼 수 있습니다.",
                lightPollution = "적음 (보틀 클래스 3)",
                altitude = "해발 860m",
                tip = "주차 공간이 협소하므로 주말보다 평일에 찾는 편이 여유롭습니다. 가로등이 없어 랜턴이 꼭 필요합니다.",
                latitude = 37.9943,
                longitude = 127.4339,
                ratingSum = 13.2f,
                ratingCount = 3
            ),
            StargazingPlace(
                id = 3,
                name = "양평 벗고개 터널",
                address = "경기도 양평군 양동면 금왕리 산76",
                description = "서울 근교에서 은하수 터널 사진으로 매우 각광받는 터널 포인트입니다. 산악 지형이 불빛을 가려 밤하늘이 무척 선명합니다.",
                lightPollution = "보통 낮음 (보틀 클래스 4)",
                altitude = "해발 250m",
                tip = "터널 입구에서 실루엣 사진을 찍는 것이 유행입니다. 차량들이 지나다닐 수 있으므로 터널 내부에서는 안전주의가 필요합니다.",
                latitude = 37.4475,
                longitude = 127.6983,
                ratingSum = 12.8f,
                ratingCount = 3
            ),
            StargazingPlace(
                id = 4,
                name = "태백 함백산 만항재",
                address = "강원특별자치도 태백시 혈동 산57-12",
                description = "우리나라에서 차로 갈 수 있는 가장 높은 도로(해발 1,330m)로, 하늘과 가장 맞닿은 광활한 은하수 조망을 선사합니다.",
                lightPollution = "매우 낮음 (보틀 클래스 1)",
                altitude = "해발 1,330m",
                tip = "만항재 야생화 공원 쉼터의 주차장이 아주 깔끔하게 꾸며져 있어 쾌적하며, 습기가 많고 바람이 세게 부는 날은 피하는 것이 좋습니다.",
                latitude = 37.1622,
                longitude = 128.8913,
                ratingSum = 14.8f,
                ratingCount = 3
            )
        )
    }
}

@Entity(tableName = "place_reviews")
data class PlaceReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val placeId: Int,
    val authorName: String,
    val content: String,
    val rating: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorAvatar: String, // name of default profile icon (e.g. "comet", "nebula", "supernova")
    val constellationName: String?,
    val content: String,
    val photoUri: String?, // attached photo or simulated photo
    val timestamp: Long = System.currentTimeMillis(),
    val location: String?,
    var likesCount: Int = 0,
    var commentsCount: Int = 0
)
