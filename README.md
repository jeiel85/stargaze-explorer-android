# 별자리 탐험

실시간 AR 천체 지도, 관측 날씨 분석, 별자리 도감, 사진 일기장을 한 화면 흐름으로 묶은 Android 별자리 관측 앱입니다. 밤하늘을 카메라로 비추며 주요 별자리를 탐색하고, 관측 장소의 날씨와 구름 상태를 기준으로 오늘 관측이 좋은지 판단할 수 있도록 설계했습니다.

[공개 페이지](https://jeiel85.github.io/stargaze-explorer-android/) · [개인정보 처리방침](https://jeiel85.github.io/stargaze-explorer-android/privacy.html)

## 무엇을 하는 앱인가요?

별자리 탐험은 천체 관측을 막 시작한 사용자가 "오늘 어디서, 무엇을, 어떻게 볼지" 빠르게 결정하도록 돕는 모바일 관측 도우미입니다. CameraX 기반 미리보기 위에 별자리 정보를 겹쳐 보여주고, 관측 기록은 Room DB에 보관합니다. Open-Meteo 날씨 데이터와 Gemini API가 연결되면 관측 조건을 자연어로 분석하고, API 키가 없거나 네트워크가 불안정한 상황에서도 로컬 가이드로 동작합니다.

## 주요 기능

- 실시간 AR 별자리 지도: CameraX 화면과 자이로/나침반 기반 방향 정보를 이용해 밤하늘 탐색 화면을 구성합니다.
- 별자리 도감: 오리온자리, 큰곰자리, 카시오페아자리, 백조자리 등 주요 별자리의 계절, 주성, 신화 정보를 제공합니다.
- 관측 일기장: 별자리, 시간, 장소, 날씨, 별점, 메모, 사진 URI를 로컬 Room DB에 기록합니다.
- 관측 장소 가이드: 강릉 안반데기, 화악산 쌈지공원, 양평 벗고개 터널, 태백 함백산 만항재 같은 국내 관측 명소 정보를 제공합니다.
- 날씨 기반 관측 코칭: Open-Meteo 기상 데이터와 Gemini API를 조합해 구름, 기온, 관측 조건을 설명합니다.
- 커뮤니티 피드: 관측 로그를 공유 피드 형태로 보여주고 좋아요와 댓글 흐름을 시뮬레이션합니다.
- 오프라인 우선 구조: 일지와 장소 데이터는 로컬 DB에 저장되며, 네트워크 실패 시에도 핵심 화면을 사용할 수 있습니다.

## 앱 화면 구성

| 탭 | 역할 |
| --- | --- |
| 지도로 탐험 | AR 별자리 지도와 별자리 상세 정보 확인 |
| 나의 도감 | 관측 일지 작성, 사진 기록, 별자리 수집 상태 확인 |
| 관측가이더 | 국내 관측 명소, 날씨, 리뷰 확인 |
| 커뮤니티 | 관측 후기 피드와 반응 UI |
| 프로필·알림 | 사용자 프로필, 알림, 동기화 상태 관리 |

## 기술 스택

| 영역 | 사용 기술 |
| --- | --- |
| 언어/UI | Kotlin, Jetpack Compose, Material 3 |
| 아키텍처 | ViewModel, StateFlow, Coroutines |
| 로컬 저장소 | Room, KSP |
| 카메라/센서 | CameraX, Android sensor APIs |
| 네트워크 | Retrofit, OkHttp, Moshi |
| 이미지 | Coil |
| AI/날씨 | Gemini API, Open-Meteo API |
| 테스트 | JUnit, Robolectric, Roborazzi, Compose UI Test |

## 로컬 실행

### 요구 사항

- Android Studio Ladybug 이상 권장
- JDK 21
- Android SDK 36

### 실행 절차

```powershell
git clone https://github.com/jeiel85/stargaze-explorer-android.git
cd stargaze-explorer-android
.\gradlew.bat assembleDebug
```

Android Studio에서 열어 Gradle Sync를 마친 뒤 `app` 구성을 실행하면 됩니다.

### Gemini API 키

Gemini 기반 관측 코칭을 사용하려면 루트에 `.env` 파일을 만들고 다음 값을 넣습니다.

```env
GEMINI_API_KEY=your_api_key_here
```

키가 없으면 앱은 내장 로컬 안내 로직으로 폴백합니다. 저장소에는 `.env.example`만 포함되며 실제 키는 커밋하지 않습니다.

## 릴리즈 산출물 만들기

Play Console 업로드용 AAB와 릴리즈 노트는 다음 명령으로 만들고 바탕화면에 복사할 수 있습니다.

```powershell
.\gradlew.bat bundleRelease
powershell -ExecutionPolicy Bypass -File scripts\export-play-store-release.ps1
```

스크립트는 `app/build.gradle.kts`의 `versionName`과 `versionCode`를 읽어 다음 형식으로 내보냅니다.

- `StargazeExplorer-v<version>-vc<code>.aab`
- `StargazeExplorer-v<version>-vc<code>-release-notes.txt`

## 개인정보

관측 일지와 사진 URI는 기본적으로 기기 로컬 저장소에 보관됩니다. 외부 API 호출은 날씨 조회와 Gemini 관측 조언 생성에 한정되며, 자세한 내용은 [개인정보 처리방침](https://jeiel85.github.io/stargaze-explorer-android/privacy.html)을 기준으로 관리합니다.
