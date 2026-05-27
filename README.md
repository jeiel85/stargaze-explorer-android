# 🌌 별자리 탐험 (Stargaze Explorer)

<div align="center">
  <img src="https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=1200&auto=format&fit=crop&q=80" width="1200" height="400" alt="Stargaze Explorer Banner" style="border-radius: 16px;" />
  <br/><br/>
  <p><strong>실시간 AR 천체 탐색과 인공지능 기상 분석으로 만나는 내 손안의 우주 정원</strong></p>
  <p><em>"Explore the beautiful constellations in real-time AR, guided by Gemini AI stargazing recommendations."</em></p>
</div>

---

## ⭐️ 프로젝트 개요 (Overview)

**별자리 탐험 (Stargaze Explorer)**은 밤하늘에 쏟아지는 별빛과 은하수를 더욱 가까이서 느끼고 기록할 수 있도록 돕는 프리미엄 스마트 천체 관측 동반자 안드로이드 애플리케이션입니다. 

실시간 자이로 센서 및 모바일 카메라를 결합한 고정밀 **천체 AR 탐험 지도**를 기반으로 밤하늘에 놓인 별자리들을 직관적으로 포착합니다. 또한, 전국의 실시간 위성 기상 정보와 차세대 인공지능 **Gemini 3.5 Flash**를 결합하여 오늘 밤 은하수 관측 확률과 맞춤형 방한/방풍 복장 팁, 이번 달 예보된 초특급 천문 이벤트 정보까지 감성적인 다이어리 형태로 속삭여 줍니다.

---

## 🎨 주요 프리미엄 기능 (Key Features)

### 1. 실시간 천체 AR 탐험 지도 (Interactive AR Sky Map)
- **자이로 & 카메라 융합 AR**: 기기의 자이로 센서 및 CameraX 뷰파인더를 정밀 결합하여, 카메라가 가리키는 실제 밤하늘 위에 실시간으로 은하 적경(RA) 좌표계와 신화 속 별자리 기하선들을 부드럽게 오버레이합니다.
- **별자리 상세 정보 & 신화 연동**: 지도상의 특정 별자리를 클릭하면 해당 별자리의 영문 약어, 주성(가장 밝은 별), 계절별 최적 관측 타이밍뿐만 아니라 낭만적인 그리스 로마 신화 이야기까지 그 자리에서 탐색할 수 있습니다.

### 2. Gemini 3.5 Flash 기반 지능형 관측 코칭 (AI Stargazing Coach)
- **하이브리드 기상 지능**: 실시간 개방형 날씨 API(Open-Meteo)를 통해 관측 예정지의 미세 구름 분포(Cloud Cover), 실시간 기온, 상대 습도를 수집합니다.
- **맞춤형 천체 예보**: 수집된 기상 수치와 현재 계절을 바탕으로 **Gemini 3.5 Flash**가 관측 추천 지수(S+부터 D등급까지)를 도출하고, "현재 영하의 날씨이니 온수가 담긴 보온병과 귀도리를 챙기라"거나 "구름이 많으니 방 안에서 AR 가상 천도 모드로 탐색하라"는 식의 실재적인 팁을 아름다운 한글 시문 레이아웃으로 실시간 조작 제공합니다.
- **탄력적 오프라인 폴백 (Offline Resiliency)**: API 키가 없거나 네트워크 통신 불가 상황에서도, 수십 가지 계절/기온 시나리오가 내장된 '로컬 천문 전문가 시스템'으로 100% 끊김 없이 탄력 작동합니다.

### 3. Room DB 기반 오프라인 관측 도감 & 사진 일기장 (Observation Journal)
- **나만의 도감 수집**: 관측 일지를 작성하면 해당 별자리가 도감에서 '해제(Unlocked)'되며 영롱한 금빛 카드로 활성화됩니다.
- **사진 일기장**: 관측한 장소, 그날의 날씨, 밤하늘에 매긴 나만의 별점 리뷰와 촬영 사진(또는 우주 실루엣)을 영구 소장 형태로 기록하고 편리하게 관리합니다.

### 4. 실시간 커뮤니티 피드 (Social Live Feed)
- 전국의 별빛 모험가들이 올리는 생생한 실시간 관측 성공 소식과 우주 일러스트를 한눈에 모아보고, '좋아요' 및 '댓글'로 실시간 기상 상태를 격려 소통할 수 있습니다.

### 5. 위치 기반 지능형 푸시 예보 알림 (Nightly Stargazing Alerts)
- 이번 달 펼쳐질 대유성우 최극대(페르세우스자리 유성우, 핼리 혜성의 잔해물 등) 및 개기월식 일정을 사전에 알림 예약하고, 최적 관측 시점에 맞춰 실시간 천문 특보 알림을 자동으로 수령합니다.

---

## 🛠️ 기술 스택 (Technology Stack)

본 프로젝트는 고수준의 모던 안드로이드 권장 아키텍처 및 라이브러리를 엄격히 준수하여 구축되었습니다.

| 영역 | 기술 사양 |
|---|---|
| **언어 및 컴포저블** | Kotlin, Jetpack Compose (Declarative UI) |
| **비동기 흐름** | Kotlin Coroutines, StateFlow / SharedFlow |
| **로컬 데이터베이스** | Room DB, KSP (Kotlin Symbol Processing) |
| **네트워킹 & 파싱** | Retrofit2, OkHttp3, Moshi (JSON 직렬화) |
| **멀티미디어 & 카메라** | Android CameraX (Lifecycle 연동), Coil (비동기 이미지 로더) |
| **인공지능 코어** | Google Gemini 3.5 Flash API Integration |
| **센서 연동** | Device Gyroscope / Compass Sensor Mapping |

---

## ⚙️ 로컬 실행 및 실행법 (Local Setup & Run)

### 1. 전제 조건 (Prerequisites)
- **Android Studio** Ladybug 이상 권장
- **JDK 21** 설치 및 환경 세팅 완료

### 2. 프로젝트 가져오기 및 라이브러리 동기화
1. 저장소를 클론하거나 다운로드합니다.
   ```bash
   git clone https://github.com/jeiel85/stargaze-explorer-android.git
   ```
2. Android Studio를 실행한 뒤 **Open**을 선택하여 해당 프로젝트 디렉터리를 엽니다.
3. Gradle Sync가 완료될 때까지 대기합니다.

### 3. Gemini API 키 주입 (Secrets 설정)
본 프로젝트는 **Secrets Gradle Plugin**을 탑재하여 API 키가 외부 코드 저장소에 노출되지 않도록 엄격히 제어합니다.
1. 프로젝트 루트 디렉터리에 `.env` 파일을 생성합니다.
2. 생성한 파일 내부에 아래와 같이 본인의 Gemini API 키를 입력합니다. (발급처: [Google AI Studio](https://aistudio.google.com/))
   ```env
   GEMINI_API_KEY=AIzaSy...YourActualGeminiKey...
   ```
3. 빌드 시 Secrets 플러그인이 이 값을 읽어 `BuildConfig.GEMINI_API_KEY`에 안전하게 주입합니다. 만약 키를 빈 상태로 두거나 `.env` 파일을 생략할 경우, 내장된 **탄력적 로컬 AI 전문가 시스템**으로 자동 전환되어 오프라인으로 안전하게 구동됩니다.

---

## 📦 릴리즈 배포 지침 (Release Guidelines)

사용자용 새 버전 패키지(AAB 및 릴리즈 노트)를 빌드하여 사용자의 로컬 Windows 바탕화면에 즉시 내보내려면 아래 절차를 수행하십시오.

1. **프로덕션 패키징 빌드 수행**:
   ```powershell
   .\gradlew.bat bundleRelease
   ```
   이 명령은 최적화된 배포용 Signed Android App Bundle(AAB)을 `app/build/outputs/bundle/release/` 폴더에 생성합니다.

2. **바탕화면 원스톱 복사 스크립트 실행**:
   ```powershell
   powershell -File scripts\export-play-store-release.ps1
   ```
   스크립트가 `app/build.gradle.kts` 상의 `versionName`과 `versionCode`를 정확히 분석한 뒤, 사용자의 실제 바탕화면(`OneDrive\바탕 화면` 또는 `Desktop`)을 자동 확보하여 아래 규격의 산출물 파일로 자동 내보냅니다.
   - 📦 **배포 패키지**: `StargazeExplorer-v<Version>-vc<Code>.aab`
   - 📝 **플레이스토어 출시 노트**: `StargazeExplorer-v<Version>-vc<Code>-release-notes.txt`
