# AGENTS.md

이 문서는 AI 코딩 에이전트가 별자리 탐험 (Stargaze Explorer) 저장소에서 작업할 때 따라야 하는 공통 작업 규칙입니다.

---

## 1. 프로젝트 설정값

```text
Project Name: 별자리 탐험 (Stargaze Explorer)
Repository: https://github.com/jeiel85/stargaze-explorer-android.git
Main Branch: main
Primary Spec: README.md
Version Files: app/build.gradle.kts
Build/Test Commands: .\gradlew.bat compileDebugKotlin, .\gradlew.bat bundleRelease
Release Trigger: Local manual release build & tag push
Expected Assets: AAB
Desktop Export: AAB, Play Store release notes TXT
Application ID: com.aistudio.starguide.qkvmsh
```

---

## 2. Automation First Principle

이 프로젝트의 에이전트는 가능한 한 작업을 끝까지 자동으로 수행합니다.
일반적인 개발 작업에서는 사용자에게 중간 확인을 요구하지 않습니다. 명시된 작업 범위 안에서는 에이전트가 직접 분석, 구현, 문서 갱신, 검증, 커밋, 푸시까지 진행합니다.

사용자 확인 없이 자동 진행하는 항목:
- 최신 소스 동기화
- 작업 범위 분석
- 코드 수정 및 리팩토링
- 관련 문서 갱신 (`README.md`, `AGENTS.md` 등)
- 가벼운 로컬 검증 및 컴파일
- 커밋 생성 및 원격 저장소 푸시
- 최종 작업 보고

---

## 3. 기본 커뮤니케이션 규칙

- 사용자에게 하는 설명, 작업 요약, 커밋 메시지는 기본적으로 한국어로 작성합니다.
- 기술 용어는 필요하면 원어를 병기하되, 설명의 중심 언어는 한국어로 유지합니다.
- 불확실한 부분은 추측으로 단정하지 않고 근거, 제약, 확인 결과를 명시합니다.
- 사용자가 요청하지 않은 대규모 리팩터링, 디자인 전면 수정, 기능 확장은 하지 않습니다.
- 진행 상황을 보고할 때는 실제로 수행한 작업과 아직 확인하지 못한 작업을 구분합니다.
- 실제로 실행하지 않은 테스트나 빌드를 성공한 것처럼 기록하지 않습니다.

---

## 4. Play Store 릴리즈 및 배포 확인

사용자가 "새 버전 만들기" 또는 릴리즈 생성을 요청하면, 로컬에서 배포본 패키징을 완료한 후 실제 사용자 바탕화면으로 산출물을 복사합니다.

바탕화면 복사 규칙:
- 대상 위치는 실제 Windows 바탕화면을 확인해 사용합니다. 우선 `C:\Users\jeiel\OneDrive\바탕 화면`을 확인하고, 없으면 `C:\Users\jeiel\Desktop`을 사용합니다.
- 복사 파일명은 타 프로젝트(dockmode, wild-haven 등)의 바탕화면 export 관례와 맞춰 `StargazeExplorer-vX.Y-vcN.aab`, `StargazeExplorer-vX.Y-vcN-release-notes.txt` 형식을 사용합니다. 여기서 `X.Y`는 `versionName`, `N`은 `versionCode`입니다.
- 릴리즈 노트 TXT는 Play Console 언어 블록을 닫는 태그까지 포함합니다. 예: `<ko-KR> ... </ko-KR>`, `<en-US> ... </en-US>`.
- 복사 후 파일 존재 여부, 크기, 수정 시간을 확인해 최종 보고에 포함합니다.

확인 항목:
- Gradle 빌드 성공 여부
- 바탕화면 AAB/TXT 복사 및 파일 크기 확인 여부
- 산출물 파일 크기가 0이 아닌지
- 릴리즈 노트가 최신 변경 사항을 잘 반영하는지
