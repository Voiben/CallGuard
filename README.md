# CallGuard — App chặn cuộc gọi lừa đảo, chặn spam Android (chan cuoc goi lua dao, chan spam, chan tin nhan rac)

App Android (Kotlin + Jetpack Compose) chặn số điện thoại lừa đảo/spam và cảnh báo SMS nghi spam. Lấy cảm hứng từ ý tưởng Truecaller nhưng là **code gốc**, không giải mã ngược hay sao chép từ Truecaller thật.

*Từ khoá tìm kiếm không dấu: chan cuoc goi lua dao, chan so la, chan tin nhan spam, canh bao lua dao qua dien thoai, caller id tieng viet, app chong lua dao Android.*

**Tác giả:** [Voiben](https://github.com/Voiben) · **Giấy phép:** [MIT](LICENSE) — ai cũng được tự do dùng, sửa, phát hành lại, miễn ghi nhận nguồn gốc.

Dữ liệu đầu số/từ khoá spam trong app chỉ là **mẫu tham khảo do tác giả tự biên soạn**, không phải cơ sở dữ liệu chính thức từ nhà mạng hay cơ quan nào — xem mục "Giới hạn hiện tại" bên dưới trước khi dùng thật.

## Chức năng
- **Danh sách chặn cục bộ**: tự thêm/xoá số, hoặc nạp sẵn từ `assets/scam_prefixes_vn.json` (file mẫu, bạn tự chỉnh).
- **Chặn cuộc gọi tự động** qua `CallScreeningService` (Android 10+) — cần người dùng cấp quyền "Caller ID & chặn spam mặc định" trong Settings.
- **Cảnh báo SMS spam**: quét nội dung SMS đến theo từ khoá trong `assets/spam_sms_keywords_vn.json`. App **không phải ứng dụng SMS mặc định** nên chỉ cảnh báo/ghi log, không tự xoá tin khỏi hộp thư hệ thống.
- **Báo cáo cộng đồng (local-only)**: người dùng tự báo cáo số nghi lừa đảo; một số bị báo cáo ≥ 2 lần sẽ tự escalate vào danh sách chặn. Hiện chưa có server dùng chung — mỗi máy lưu riêng.
- **Caller ID cho số lạ**: với mọi cuộc gọi đến từ số KHÔNG có trong Danh bạ máy (Android chỉ gửi những cuộc gọi này tới CallGuard — xem mục giới hạn Danh bạ bên dưới), CallGuard hiện thông báo ngay — gắn nhãn loại spam/lừa đảo nếu số đó từng bị báo cáo trong app, kèm nút **"Chặn số này"** để chặn ngay từ thông báo mà không cần mở app.
- Lịch sử hoạt động + Dashboard thống kê.

## Đã kiểm thử logic chặn (module `core`)
Logic quyết định "chặn hay cho qua" (chuẩn hoá số, khớp đầu số, khớp từ khoá SMS, ngưỡng tự escalate khi bị báo cáo) được tách riêng thành module Kotlin/JVM thuần `core/` — **không phụ thuộc Android SDK** — để có thể build & test thật ngay trên máy phát triển, không cần cài Android Studio trước.

Đã chạy thật (không phải mô phỏng):
```bash
./gradlew :core:test
```
Kết quả: **134 test cases PASS**, chạy lại nhiều vòng liên tiếp (`--rerun`) đều ổn định — bao gồm parameterized test cho nhiều định dạng số (+84/84/0, có khoảng trắng/dấu gạch), test khớp đầu số, test khớp từ khoá SMS (kèm trường hợp biên: rỗng, không dấu, không khớp), và test ngưỡng tự động chặn sau khi bị báo cáo cộng đồng ≥ 2 lần. Xem file test tại `core/src/test/kotlin/com/nxt/callguard/core/`. Báo cáo HTML sau khi chạy: `core/build/reports/tests/test/index.html`.

App module (`app/`) dùng lại đúng engine này (`BlockDecisionEngine`, `SpamKeywordMatcher`, `ReportEscalation` trong `com.nxt.callguard.core`) thay vì viết lại logic — nên hành vi chặn thật trên điện thoại khớp với hành vi đã test.

## Đã build & test thật app module (không chỉ core)
Đã cài Android SDK (platform 34, build-tools) trên máy viết code, build và test thật module `app`, không chỉ mô phỏng:

```bash
./gradlew :app:assembleDebug        # ra APK thật: app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest    # test tích hợp Room + Repository qua Robolectric (không cần emulator)
```

Quá trình build/test thật đã **bắt được các lỗi thật** (không phải chỉ chạy suôn sẻ ngay lần đầu):
- `Icons.Filled.Block/Dashboard/History/Report` không có trong `material-icons-core` → phải đổi sang `material-icons-extended`.
- Thiếu `@OptIn(ExperimentalMaterial3Api::class)` cho `TopAppBar`/`ExposedDropdownMenuBox` → thêm cờ compiler `-opt-in`.
- Thiếu import `getValue`/`padding` trong `CallGuardNavHost.kt` → sửa.
- **Lỗi logic thật trong `PhoneNumberUtils.normalize()`**: một đầu số ngắn kiểu `"+8428770"` (dùng cho chặn theo đầu số) không được quy đổi `84→0` đúng vì điều kiện cũ chỉ kích hoạt khi chuỗi đủ dài như một số điện thoại thật — khiến đầu số mẫu trong `scam_prefixes_vn.json` không khớp được số nào. Đã sửa: ký tự `+` rõ ràng luôn được coi là mã quốc gia, bất kể độ dài. Có test riêng cho trường hợp này.
- Test tích hợp Room qua Robolectric ban đầu FAIL với `IllegalStateException: Illegal connection pointer` do coroutine seed dữ liệu nền của `CallGuardApp.onCreate()` và singleton `AppDatabase` bị tranh chấp giữa các lần chạy test → đã refactor `CallGuardRepository` nhận `AppDatabase` qua constructor (dependency injection) để test dùng DB in-memory riêng, độc lập hoàn toàn với app thật.

Sau khi sửa: **toàn bộ test (134 ở `core` + 6 test tích hợp ở `app`) PASS, chạy lại nhiều vòng liên tiếp đều ổn định**, và `:app:assembleDebug` ra APK thật (`app-debug.apk`, ~15MB).

## Đã kiểm thử end-to-end thật trên hệ thống Android thật (CallScreeningService + SmsReceiver)
Cài APK lên 2 môi trường thật và quan sát kết quả qua `adb`/logcat — không phải mô phỏng đơn vị:

**Điện thoại thật (Xiaomi, MIUI, Android 13) — đã xác nhận chặn đúng:** Lần thử đầu gán role `CALL_SCREENING` qua `adb` không đủ hiệu lực thật (MIUI vẫn định tuyến cuộc gọi tới `com.truecaller`); phải đổi qua đúng nút "Đặt làm app chặn cuộc gọi mặc định" trong app (hộp thoại hệ thống chuẩn) mới có hiệu lực. Sau đó phát hiện thêm: **Android tự động bỏ qua toàn bộ app chặn gọi (kể cả CallGuard) nếu số gọi đến đã được lưu trong Danh bạ** (`mIsInContacts=true` → `mCallScreeningPackageName='null'`, không gọi app nào cả) — đây là hành vi gốc của hệ điều hành, không phải lỗi CallGuard. Sau khi xoá số test khỏi Danh bạ và gọi lại, log hệ thống thật xác nhận:

```
Telecom: SCREENING_COMPLETED [Reject, logged, mCallBlockReason = 1,
  mCallScreeningAppName = CallGuard,
  mCallScreeningComponentName = com.nxt.callguard/...CallGuardScreeningService]
onCallFilteringCompleted(mIsBlocked=true, mIsInContacts=false,
  mCallScreeningPackageName='ComponentInfo{com.nxt.callguard/...}')
```

→ CallGuard thực sự chặn được cuộc gọi thật trên chính điện thoại Android đang cài, không phải mô phỏng. Hai điều cần nhớ khi dùng thật: (1) phải tự bấm nút "Đặt làm mặc định" trong app — `adb`/cài role nền không đủ trên MIUI; (2) số đã lưu trong Danh bạ sẽ không bao giờ bị app chặn gọi nào xử lý, kể cả khi đã thêm vào danh sách chặn của CallGuard — đây là giới hạn của chính Android, không riêng gì app này.

### Caller ID + nút "Chặn số này" — đã test thật, bắt được 3 lỗi thật
Khi thêm tính năng thông báo số lạ kèm nút chặn nhanh, test thật trên emulator (API 34) bắt được:
- **Icon action = 0**: `NotificationCompat.Action.Builder(0, ...)` khiến hệ thống âm thầm không hiển thị nút hành động trong thông báo (không crash, không log lỗi — chỉ đơn giản là nút biến mất). Sửa bằng cách dùng icon resource hợp lệ.
- **Race điều kiện trong BroadcastReceiver**: `QuickBlockReceiver`/`SmsReceiver` dùng coroutine "bắn rồi quên" (`launch{}` không chờ) — tiến trình có thể bị hệ thống dừng trước khi ghi xong database. Sửa bằng `goAsync()` đúng chuẩn Android để giữ tiến trình sống đến khi ghi xong.
- **`adb shell am broadcast` không đại diện cho hành vi thật**: receiver khai báo `exported="false"` (đúng, để bảo mật — chỉ chính CallGuard mới được tự kích hoạt) khiến lệnh test qua `adb shell` (chạy với quyền `shell`, khác UID với app) bị hệ thống âm thầm từ chối (`is not exported from uid ...`, xem được qua `dumpsys activity broadcasts`). Phải test bằng `adb shell run-as com.nxt.callguard am broadcast --user 0 ...` (chạy đúng UID của app) mới phản ánh đúng luồng thật khi người dùng bấm nút trên thông báo.

Sau khi sửa, xác nhận bằng dữ liệu thật trong SQLite của app:
```
0955666777|Chặn nhanh từ thông báo|MANUAL
```
→ Số được thêm đúng vào danh sách chặn khi "bấm" nút trong thông báo (mô phỏng thật qua run-as, tương đương thao tác chạm thật của người dùng).

**Android Emulator (API 34, chạy đầy đủ stack Telecom/Telephony thật, không phải giả lập logic):** sau khi cài, cấp quyền, gán role (hoạt động bình thường, không bị khoá như MIUI), mô phỏng 2 cuộc gọi thật (`adb emu gsm call <số>`) và 2 SMS thật (`adb emu sms send <số> <nội dung>`) — một số nằm trong danh sách chặn, một số không. Kết quả đọc trực tiếp từ log hệ thống Telecom và từ chính database thật của app (`run-as com.nxt.callguard sqlite3 .../callguard.db`), không phải suy đoán:

```
Telecom: SCREENING_COMPLETED [Reject, ..., mCallScreeningAppName = CallGuard]   # số bị chặn
Telecom: SCREENING_COMPLETED [Allow,  ..., mCallScreeningAppName = CallGuard]   # số không bị chặn

activity_log (SQLite thật của app):
0912345678|SMS |ALLOWED|Không phát hiện dấu hiệu spam
0900000000|SMS |BLOCKED|Số gửi nằm trong danh sách chặn
0912345678|CALL|ALLOWED|Không nằm trong danh sách chặn
0900000000|CALL|BLOCKED|Số trong danh sách chặn
```

→ Xác nhận: `CallGuardScreeningService` thực sự được hệ thống Telecom gọi tới và trả lời đúng (chặn/cho qua), `SmsReceiver` thực sự nhận broadcast `SMS_RECEIVED` thật và gắn cờ đúng SMS spam — trên một hệ thống Android thật, qua đường dẫn thật mà điện thoại thật sẽ dùng.

## Yêu cầu để build app Android thật (trên máy khác)
Module `app` vẫn cần Android SDK (Room, Compose, CallScreeningService là API Android). Cần:
1. [Android Studio](https://developer.android.com/studio) (bản mới, hỗ trợ AGP 8.5+, Kotlin 1.9.24).
2. Mở thư mục `CallGuard` này bằng Android Studio → để nó tự tải SDK (API 34) khi sync. Gradle wrapper đã có sẵn trong repo (`gradlew`, `gradlew.bat`). File `local.properties` trỏ `sdk.dir` theo máy đã build — **đổi lại đường dẫn này cho đúng máy của bạn** (hoặc xoá file, để Android Studio tự tạo lại khi sync).
3. Nên test trên thiết bị thật trước khi dùng thật; nếu máy MIUI/Xiaomi, bắt buộc đổi app chặn gọi mặc định qua đúng nút trong app (không phải qua `adb`) — xem mục trên.
4. Nếu project nằm trong thư mục đồng bộ OneDrive như ở đây: `settings.gradle.kts` đã trỏ build output ra `G:/tmp/gradle-builds/CallGuard` (cùng ổ đĩa, ngoài vùng OneDrive đồng bộ) để tránh OneDrive khoá file giữa lúc build — đổi đường dẫn đó nếu ổ đĩa của bạn khác.

## Cấu hình sau khi cài
1. Mở app → tab **Cài đặt** → bấm "Cấp quyền" (đọc cuộc gọi, SMS, thông báo, danh bạ — danh bạ dùng để cảnh báo "số này đang có trong Danh bạ" khi thêm vào danh sách chặn).
2. Bấm "Đặt làm app chặn cuộc gọi mặc định" → chọn CallGuard trong hộp thoại hệ thống.
3. Vào tab **Danh sách chặn** để thêm số cụ thể, hoặc sửa `app/src/main/assets/scam_prefixes_vn.json` trước khi build lại.

## Giới hạn hiện tại (nên biết trước khi dùng thật)
- `scam_prefixes_vn.json` và `spam_sms_keywords_vn.json` chỉ là **dữ liệu mẫu**, không phải cơ sở dữ liệu lừa đảo chính thức từ nhà mạng/Bộ TTTT — độ chính xác phụ thuộc vào việc bạn tự cập nhật hoặc tích lũy báo cáo cộng đồng ngay trong app.
- Báo cáo cộng đồng hiện **chỉ lưu local trên máy**, chưa đồng bộ giữa người dùng. Muốn có "cộng đồng" thật cần thêm backend (ví dụ Firebase/REST) để chia sẻ danh sách số bị báo cáo — chưa làm trong bản này.
- Chặn SMS chỉ ở mức cảnh báo, không xoá/ẩn, vì biến app thành ứng dụng SMS mặc định (có giao diện gửi/nhận tin đầy đủ) là một hạng mục lớn riêng.
- Khớp từ khoá SMS hiện là so khớp chuỗi con, **không chuẩn hoá dấu tiếng Việt** (ví dụ "vay tien nhanh" không dấu sẽ không khớp "vay tiền nhanh" có dấu) — test đã ghi lại rõ giới hạn này, không phải lỗi ẩn.
- Đã test end-to-end thật và PASS trên cả 2 môi trường: Android Emulator (chặn, cho qua, SMS) và điện thoại Xiaomi/MIUI thật (chặn cuộc gọi thật, xác nhận qua log hệ thống Telecom — xem mục trên). **Số đã lưu trong Danh bạ sẽ không bao giờ bị bất kỳ app chặn gọi nào xử lý** (hành vi gốc của Android, phát hiện trong lúc test thật) — nếu muốn chặn một số, người dùng không được lưu số đó vào Danh bạ. Chưa test trên các dòng máy Android khác (Samsung One UI, stock Android, v.v.) — hành vi "ứng dụng chặn gọi mặc định" có thể khác nhau tuỳ hãng.

## Cấu trúc
```
core/src/main/kotlin/com/nxt/callguard/core/   # Logic thuần, có unit test, không phụ thuộc Android
  PhoneNumberUtils.kt      # Chuẩn hoá số điện thoại
  BlockDecisionEngine.kt   # Quyết định chặn/cho qua, khớp từ khoá SMS, ngưỡng escalate báo cáo
core/src/test/kotlin/...   # 134 test cases, parameterized + repeated rounds

app/src/main/java/com/nxt/callguard/
  CallGuardApp.kt          # Application, khởi tạo DB + notification channel
  MainActivity.kt          # Entry point, xin quyền, xin role Call Screening
  data/                    # Room entities/DAO/DB + repository (gọi vào :core để quyết định)
  service/                 # CallScreeningService + SmsReceiver
  ui/                      # Compose: theme, navigation, các màn hình
app/src/test/java/com/nxt/callguard/data/
  CallGuardRepositoryTest.kt  # 6 test tích hợp Room+Repository thật qua Robolectric
```
