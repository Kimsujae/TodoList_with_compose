# 📍 Location-Based Todo List

> **위치 정보를 결합한 스마트한 할 일 관리 안드로이드 애플리케이션**
> 
> 단순히 할 일을 적는 것에 그치지 않고, 지도를 활용해 장소 정보를 부여하고 알람을 통해 잊지 않도록 도와주는 현대적인 Todo 앱입니다.

---

## 🚀 주요 기능 (Key Features)

### 1. 위치 기반 할 일 관리
- **지도에서 직접 추가**: `MapView`에서 원하는 지점을 클릭하여 즉시 할 일을 등록할 수 있습니다.
- **장소 부여**: 각 할 일에 위도/경도 정보를 저장하고 장소 이름을 지정할 수 있습니다.
- **장소 확인**: 리스트에서 할 일과 연결된 장소를 바로 확인하고 위치 아이콘을 통해 시각화합니다.

### 2. 통합 지도 뷰 (Integrated Map)
- **전체 할 일 시각화**: 모든 할 일의 위치를 마커로 표시하여 한눈에 파악할 수 있습니다.
- **현재 위치 기반**: 앱 실행 및 지도 진입 시 사용자의 현재 위치를 자동으로 파악하여 보여줍니다.

### 3. 정교한 알람 및 리마인더
- **커스트마이징 피커**: 사용자가 사용하기 편리한 커스텀 Date/Time Picker를 제공합니다.
- **정확한 알람**: `AlarmManager`를 활용하여 지정된 시간에 정확하게 알림을 보냅니다.

### 4. 사용자 친화적 UI/UX
- **Jetpack Compose**: 최신 선언형 UI 프레임워크를 사용하여 매끄럽고 현대적인 디자인을 구현했습니다.
- **권한 관리 시스템**: 알림 및 위치 권한이 필요한 시점에 사용자에게 친절하게 요청하는 워크플로우를 갖추고 있습니다.

---

## 🛠 기술 스택 (Tech Stack)

| 분류 | 기술 |
| --- | --- |
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Architecture** | Clean Architecture (Domain, Data, Presentation) |
| **DI** | Hilt |
| **Database** | Room (SQLite) with Migration (v2 -> v3) |
| **Maps** | Google Maps SDK for Android (Maps Compose) |
| **Location** | Google Play Services Location (FusedLocationProvider) |
| **Async** | Coroutines, Flow |

---

## ⚙️ 설치 및 설정 (Setup)

### 1. 사전 요구 사항
- Android Studio Jellyfish 이상
- Google Maps API Key

### 2. API 키 설정 (보안)
본 프로젝트는 API 키 보안을 위해 `Secrets Gradle Plugin`을 사용합니다.
프로젝트 루트의 `local.properties` 파일에 아래 내용을 추가하세요.

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
```

### 3. 빌드 및 실행
1. 저장소를 클론합니다.
2. Android Studio에서 프로젝트를 엽니다.
3. Gradle Sync를 완료한 후 실행(Run) 버튼을 누릅니다.

---

## 📸 스크린샷 (Screenshots)

| 메인 리스트 | 할 일 추가/수정 | 전체 지도 뷰 |
| :---: | :---: | :---: |
| ![Main](https://via.placeholder.com/200x400?text=Main+List) | ![Input](https://via.placeholder.com/200x400?text=Input+View) | ![Map](https://via.placeholder.com/200x400?text=Map+View) |

---

## 📂 프로젝트 구조

```text
com.example.test240402
├── data
│   ├── dao        # Room DAOs
│   ├── model      # Entity models
│   ├── mapper     # Data mappers
│   └── repository # Repository implementations
├── domain
│   ├── model      # Domain models
│   ├── repository # Repository interfaces
│   └── usecase    # Business logic units
├── presentation
│   ├── ui         # Composables (MainActivity, MapView, etc.)
│   └── viewmodel  # State management
└── di             # Dependency Injection modules
```
