# GitHub 원격 동기화 코멘트 - 2026-06-04

## 목적

사용자 지시에 따라 로컬 `dev/bike-front` 작업 내용을 GitHub 원격 `BIKE-PROJECT-MINJI/bike-front`에 반영한다.

## 변경 묶음

- Android Native 앱 설정과 인증 흐름 보강
- 주소/AI route/curator 관련 화면 및 테스트 보강
- release workflow와 앱 설정 정리
- QA용 테스트와 UI 상태 검증 보강

## 원격 반영 방식

- 기준 브랜치: `main`
- 원격 백업 브랜치를 만든 뒤 로컬 기준 커밋을 원격에 반영한다.
- 비밀값 후보는 push 전 스캔한다.

## 검증 메모

- 이번 작업은 원격 관리와 로컬 반영 정리 목적이다.
- 상세 Android 실기기/릴리즈 검증은 별도 release gate에서 다시 수행한다.
