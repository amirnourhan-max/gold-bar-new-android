# GOLD BAR Android

نسخه Android نرم‌افزار **GOLD BAR R12 Final**، مشتق‌شده از نسخه نهایی ویندوز.

## وضعیت

نسخه اولیه Android با Kotlin + Jetpack Compose ساخته شده و بخش اتصال ترازو/Serial عمداً حذف شده است.

## امکانات منتقل‌شده

- ثبت آبشده دستی
- خلاصه وزن، عیار میانگین و تعداد آبشده‌ها
- افزایش عیار
- عیار و بار مورد نیاز
- محاسبه سریع
- هزینه عیار
- ذخیره گزارش Excel پنج‌بخشی `.xlsx`
- ورود گزارش Excel و جایگزینی کامل اطلاعات کاری
- ظاهر Dark + Gold، راست‌به‌چپ و فارسی

## تفاوت با نسخه Windows

بخش ترازو، Serial Port، تنظیمات COM و خواندن خودکار وزن حذف شده‌اند؛ ورود وزن فقط دستی است.

## ساخت

```bash
gradle testDebugUnitTest assembleDebug
```

GitHub Actions فایل APK دیباگ را با نام `GoldBar-Android-debug-apk` منتشر می‌کند.

## منبع مرجع

نسخه مرجع ویندوز: `amirnourhan-max/GoldBar-windows-for`، tag: `v2.0.0-r12-final`.
