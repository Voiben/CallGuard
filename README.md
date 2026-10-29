# CallGuard — App chặn cuộc gọi lừa đảo, chặn spam Android (chan cuoc goi lua dao, chan spam, chan tin nhan rac)

App Android (Kotlin + Jetpack Compose) chặn số điện thoại lừa đảo/spam và cảnh báo SMS nghi spam. Lấy cảm hứng từ ý tưởng Truecaller nhưng là **code gốc**, không giải mã ngược hay sao chép từ Truecaller thật.

**Tác giả:** [Voiben](https://github.com/Voiben) · **Giấy phép:** [MIT](LICENSE).

## Chức năng
- Danh sách chặn cục bộ: tự thêm/xoá số, hoặc nạp sẵn từ `scam_prefixes_vn.json` (dữ liệu mẫu, tự chỉnh).
- Chặn cuộc gọi tự động qua `CallScreeningService` (Android 7+).
- Cảnh báo SMS spam theo từ khoá — chỉ cảnh báo/ghi log, không xoá tin (app chưa phải SMS mặc định).
- Báo cáo cộng đồng (lưu local): số bị báo cáo ≥ 2 lần tự chuyển sang danh sách chặn.
- Caller ID cho số lạ: cuộc gọi từ số không có trong Danh bạ sẽ có thông báo kèm nút "Chặn số này".
- Lịch sử hoạt động + Dashboard thống kê.
- Kiểm tra cập nhật ngay trong app (tab Cài đặt), dựa trên GitHub Releases của repo này.

## Cài đặt & cấu hình
1. Cài APK, mở app.
2. Tab **Cài đặt** → bấm "Cấp quyền" (điện thoại, SMS, thông báo, danh bạ).
3. Bấm "Đặt làm app chặn cuộc gọi mặc định" → xác nhận trong hộp thoại hệ thống.
4. Tab **Danh sách chặn** để thêm số cụ thể.

Yêu cầu Android 7.0 (API 24) trở lên; cách chọn app mặc định ở bước 3 khác nhau theo phiên bản/hãng máy — rõ ràng và có hộp thoại chuẩn từ Android 10+, các bản cũ hơn cần vào Cài đặt hệ thống thủ công.

## Giới hạn cần biết
- Dữ liệu đầu số/từ khoá spam chỉ là **mẫu tham khảo**, không phải cơ sở dữ liệu chính thức — độ chính xác phụ thuộc vào bạn tự cập nhật hoặc báo cáo cộng đồng tích luỹ trong app.
- Báo cáo cộng đồng hiện chỉ lưu trên máy, chưa đồng bộ giữa người dùng.
- Không chặn/xoá được SMS, chỉ cảnh báo.
- Khớp từ khoá SMS không chuẩn hoá dấu tiếng Việt (ví dụ "vay tien nhanh" không dấu sẽ không khớp bản có dấu).
- **Android không gửi bất kỳ cuộc gọi nào tới app chặn spam nếu số gọi đến đã lưu trong Danh bạ máy** — hành vi gốc của hệ điều hành. Muốn chặn một số, không được lưu số đó vào Danh bạ.

## Build từ source
Cần Android Studio (AGP 8.5+, Kotlin 1.9.24) — mở thư mục này, để Android Studio tự tải SDK khi sync. Gradle wrapper có sẵn (`gradlew`).

```bash
./gradlew :core:test          # unit test logic chặn (không cần SDK)
./gradlew :app:testDebugUnitTest   # test tích hợp Room qua Robolectric
./gradlew :app:assembleDebug  # ra APK: app/build/outputs/apk/debug/app-debug.apk
```

## Cấu trúc
```
core/   # Logic chặn thuần Kotlin/JVM, không phụ thuộc Android (PhoneNumberUtils, BlockDecisionEngine)
app/    # UI Compose, Room DB, CallScreeningService, SmsReceiver
```

## Báo lỗi / đóng góp
- Báo lỗi qua [GitHub Issues](https://github.com/Voiben/CallGuard/issues/new/choose) — có sẵn mẫu báo lỗi.
- Issue mới tự động báo qua Telegram (xem `.github/workflows/notify-telegram.yml`) — cấu hình secret `TELEGRAM_BOT_TOKEN`/`TELEGRAM_CHAT_ID` trong `Settings > Secrets and variables > Actions` của repo.
- Ai cũng có thể fork/sửa/gửi Pull Request theo giấy phép MIT.
