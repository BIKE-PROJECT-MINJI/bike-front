# bike-front

자전거 여행용 주행 HUD 앱의 Android Native 프론트엔드 저장소입니다.

이 저장소는 **홈 추천 코스 → pre-ride 확인 화면 → ride 화면 → 기록 저장/코스 만들기**까지의 모바일 흐름을 담당합니다.

## 현재 기술 스택

- Android Native
- Kotlin + Jetpack Compose
- Android Gradle Plugin 8.5.2
- compileSdk 35 / targetSdk 35 / minSdk 26
- Java 17
- Navigation Compose
- MapLibre Android SDK

## 현재 구현 범위

### 1차 핵심 범위
- 홈 화면 / 앱 셸
- 추천 코스 목록
- 코스 진입 pre-ride 화면
- 자유 주행 / 코스 따라가기 ride 진입 화면
- 지도/현재 위치/속도/날씨/풍향/주행 정책 HUD

### 2차 프론트 범위
- 홈 화면 레이아웃 정리와 다음 행동 강조
- 주행 종료 후 기록 저장/코스 만들기 진입
- 최소 auth/profile 화면
- 코스 초안 편집 화면
- 공개 범위 선택 UI (`비공개 / 링크 공유 / 공개`)
- 실제 ride record 저장 API 호출
- 실제 course create API 호출
- Activity 재생성 시 저장/생성 진행 상태 보존

## 주요 화면 / 흐름

- 홈 / 앱 셸: `MainActivity`, `ui/BikeFrontApp.kt`
- 코스 진입 pre-ride: `ui/screen/CoursePreRideScreen.kt`, `ui/screen/FreeRidePreRideScreen.kt`
- ride 화면: `free/FreeRideActivity.kt`
- 로그인/프로필 진입: `auth/AuthProfileActivity`
- 코스 초안 정리: `course/CourseEditorActivity`

대표 흐름:

1. 홈에서 추천 코스를 본다.
2. pre-ride 화면에서 시작 전 맥락을 확인한다.
3. ride 화면으로 들어가 주행한다.
4. 주행 종료 후 기록 저장과 코스 만들기 흐름으로 이동한다.
5. 로그인 후 코스 초안을 저장한다.

## 현재 모듈 구조

- `ui/`, `ui/screen/` : 앱 셸, 홈, 코스, pre-ride, 내 정보 Compose 화면
- `ridemap/` : 코스 경로 좌표/지도 연동
- `ridepolicy/` : 주행 정책 평가 호출
- `weather/` : 날씨/풍향 호출
- `auth/` : 로그인/세션 저장
- `course/` : 코스 초안 편집/생성 호출
- `config/` : API base URL, 공통 설정

## 실행 / 검증

Windows PowerShell 기준:

- debug 빌드: `./gradlew.bat assembleDebug`
- 전체 빌드: `./gradlew.bat build`
- 전체 테스트: `./gradlew.bat test`

WSL/bash에서 Windows checkout을 그대로 쓰는 경우에는 `./gradlew` 대신 `cmd.exe /c gradlew.bat test`를 우선한다.

### API base URL 주입

- debug 기본값: `http://10.0.2.2:8080`
- release 기본값: `https://bike-back-production.up.railway.app`
- 필요 시 gradle property로 덮어쓴다.
  - debug 예시: `./gradlew.bat assembleDebug -PdebugApiBaseUrl=https://staging.example.com`
  - release 예시: `./gradlew.bat assembleRelease -PreleaseApiBaseUrl=https://api.example.com`

### 실기기 직접 설치용 APK

- 에뮬레이터가 아니라 **실제 Android 기기**에 debug APK를 설치할 때는 기본 `10.0.2.2` 주소를 그대로 쓰면 안 된다.
- 실기기 설치용 debug APK는 운영 backend 또는 접근 가능한 staging URL로 다시 빌드한다.
  - 예시: `./gradlew.bat assembleDebug -PdebugApiBaseUrl=https://bike-back-production.up.railway.app`
- 현재 기본 debug APK 경로: `app/build/outputs/apk/debug/app-debug.apk`
- release APK를 외부 배포용으로 쓰려면 별도 서명 설정과 배포 전 smoke/실기기 검증이 더 필요하다.

## 현재 문서 기준

이 저장소 구현은 아래 current 문서를 따른다.

- `DOCS/00_기준/프로젝트_헌법.md`
- `DOCS/00_기준/통합_개발_테스트_방법론.md`
- `DOCS/00_기준/ADR/frontend-mobile/ADR-FM-007_Kotlin_Compose_프론트_전면_전환_선정.md`
- `DOCS/15_기능명세/frontend-mobile/ride_화면_UX_정책_및_요구사항.md`
- `DOCS/15_기능명세/frontend-mobile/인증_프로필_정책_및_요구사항.md`
- `DOCS/15_기능명세/frontend-mobile/코스_생성_공유_정책_및_요구사항.md`

## 유지 원칙

- 기능이 추가되거나 제거되면 **README의 “현재 구현 범위”와 “주요 화면 / 흐름”을 함께 갱신**합니다.
- 기술 스택이 바뀌면 **“현재 기술 스택”을 바로 갱신**합니다.
- README는 계획이 아니라 **지금 실제로 구현된 화면/흐름만** 적습니다.
