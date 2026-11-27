# مدل دامنه — فروشگاه اینترنتی

بخش دوم فاز ۱ · اصول طراحی نرم‌افزار · دکتر هانیه فلاح

این سند، گام اولِ بخش دوم است: تبدیل ۱۵ Domain Story و ۳۶ User Story بخش اول به یک مدل
شیءگرا، به‌همراه جزئیات کامل کلاس‌ها در هر سه لایه.

---

## ۱. روش کار

طبق اسلاید «تبدیل Domain Story به Domain Model»، هفت گام طی شد:

| گام | کاری که انجام شد |
|---|---|
| ۱ — اسم‌ها را پیدا کن | ۸۰ اسم متمایز از ۱۰۹ فعالیتِ ۱۵ داستان، مکانیکی استخراج شد |
| ۲ — مفاهیم اصلی را انتخاب کن | ۱۶ اسمِ آرتیفکت رابط کاربری حذف شد (جدول ۲٫۱) |
| ۳ — ارتباط‌ها را مشخص کن | بخش ۴ |
| ۴ — تعدّد را تعیین کن | بخش ۴ |
| ۵ — ویژگی‌ها را اضافه کن | بخش ۵ |
| ۶ — قوانین دامنه را بررسی کن | به‌صورت RI در هر کلاس (بخش ۵) |
| ۷ — اعتبارسنجی | نگاشت کامل ۳۶ داستان در بخش ۸ |

### ۱٫۱ تفکیک Entity از Value Object

معیار اسلاید: **Entity را با هویتش می‌شناسیم، Value Object را با مقدارش.**

---

## ۲. از اسم‌ها به مفاهیم

### ۲٫۱ اسم‌هایی که کلاس نشدند

اسلاید می‌گوید «اسم‌هایی را نگه دار که در کسب‌وکار معنا دارند، نه هر اسمی که در داستان آمده».
این ۱۶ اسم، آرتیفکت رابط کاربری‌اند و هیچ مفهوم دامنه‌ای ندارند:

> صفحهٔ نخست فروشگاه · صفحهٔ پروفایل · صفحهٔ محصول · صفحهٔ شخصی · صفحه‌های خصوصی ·
> نشست کاربر · نتایج جستجو · فهرست محصولات · فهرست مرتب‌شده · فهرست سفارش‌ها ·
> فهرست سفارش‌ها با وضعیت · فهرست آدرس‌ها · گزارش انبار · تاریخچهٔ سفارش‌ها ·
> پیام خوش‌آمد · خبر ناموفق بودن

این‌ها در لایهٔ `cli` به‌صورت نمایش ظاهر می‌شوند، نه در `model`.

### ۲٫۲ اسم‌هایی که به یک مفهوم تا خوردند

| اسم‌های داستان | مفهوم |
|---|---|
| محصول انتخابی · محصول تازه · محصول قدیمی · محصول غیرفعال · محصول خریداری‌شده · مشخصات محصول · تصاویر محصول | `Product` |
| سفارش پرداخت‌شده · سفارش پرداخت‌نشده · یک سفارش · سفارش‌های گذشته · خلاصهٔ سفارش · همهٔ سفارش‌ها | `Order` |
| آدرس تازه · آدرس قدیمی · آدرس پیش‌فرض · آدرس ارسال · کد پستی | `Address` |
| مبلغ نهایی · مبلغ کل سبد · مبلغ پرداخت‌شده · بازهٔ قیمت | `Money` |
| تعداد کالا · موجودی انبار · موجودی محصول · موجودی منفی | `Quantity` / `StockCount` |
| امتیاز ستاره‌ای · میانگین امتیاز | `Rating` |
| رمز عبور · رمز عبور تازه · اطلاعات ورود | `PasswordHash` |

---

## ۳. Entity یا Value Object؟

### ۳٫۱ Entity ها

| کلاس | هویت | چرا Entity |
|---|---|---|
| `User` | `UserId` | ایمیل و نام تغییر می‌کند، ولی همان کاربر است (داستان ۴، ۶) |
| `Address` | `AddressId` | کاربر «آن آدرس خاص» را ویرایش و حذف می‌کند و یکی را پیش‌فرض می‌کند (داستان ۵) |
| `Category` | `CategoryId` | نامش عوض می‌شود، محصولاتش همان‌جا می‌مانند |
| `Product` | `ProductId` | قیمت و موجودی مدام تغییر می‌کند، محصول همان است (داستان ۱۴، ۱۵) |
| `Cart` | `UserId` مالکش | هر کاربر دقیقاً یک سبد دارد که در طول جلسه تغییر می‌کند (داستان ۹) |
| `Order` | `OrderId` | وضعیتش از پرداخت‌نشده به پرداخت‌شده می‌رود (داستان ۱۰، ۱۱) |
| `Payment` | `PaymentId` | هر تلاش پرداخت یک رویداد مستقل است (داستان ۱۰، ۱۱) |
| `Review` | `ReviewId` | نظر یک کاربر روی یک محصول، سند مستقلی است (داستان ۱۳) |

> **`Address` چرا Entity شد؟** اسلاید درس خودش آدرس را مثال Value Object می‌زند. ولی اسلاید
> صریح می‌گوید تصمیم به **قوانین دامنه** بستگی دارد، نه به وجود فیلد ID. در دامنهٔ ما داستان
> کاربری ۷ می‌گوید کاربر آدرس را **ویرایش و حذف** می‌کند و یکی را **پیش‌فرض** علامت می‌زند.
> این یعنی باید بتوانیم به «آن آدرس» اشاره کنیم؛ پس هویت دارد.
>
> ولی آدرسی که روی سفارش ثبت می‌شود **کپی تغییرناپذیر** است، نه ارجاع — چون سفارش یک سند
> تاریخی است و تغییر بعدی آدرسِ کاربر نباید سفارش گذشته را عوض کند.

### ۳٫۲ Value Object ها

| کلاس | چرا Value Object | قانون دامنه (RI) |
|---|---|---|
| `Money` | دو مبلغ با مقدار برابر، یکی‌اند | نامنفی، حداکثر دو رقم اعشار |
| `Quantity` | «۳ عدد» هویت ندارد | **اکیداً مثبت** |
| `StockCount` | | **نامنفی** (داستان ۲۹ صریحاً منفی را منع می‌کند) |
| `Rating` | | بین ۱ تا ۵ (داستان ۳۱) |
| `Email` | | قالب معتبر (داستان ۱) |
| `PhoneNumber` | | رقم، طول معتبر |
| `PasswordHash` | | رمزنگاری‌شده، هرگز متن خام (داستان ۱) |
| `PostalCode` | | غیرخالی (داستان ۷) |
| `SearchPhrase` | | غیرخالی، **حداکثر ۱۰۰ کاراکتر** (داستان ۱۰) |
| `ProductFilterRequest` ¹ | ترکیبی از شرط‌ها | `minPrice ≤ maxPrice` (داستان ۱۱) |
| `UserId`, `ProductId`, `OrderId`, `AddressId`, `CategoryId`, `PaymentId`, `ReviewId` | شناسهٔ تایپ‌دار | غیرخالی |

> **¹ `ProductFilterRequest` در لایهٔ `service` نشست، نه `model`.** هنگام پیاده‌سازی روشن شد
> که «کاربر فهرست را چطور باریک می‌کند» یک دغدغهٔ *پرس‌وجو* است، نه یک مفهوم دامنه. فروشگاه
> بدون آن هم کامل معنا دارد. گذاشتنش در `model` باعث می‌شد لایهٔ مدل از نحوهٔ جستجوی کاربر
> باخبر باشد.

> **چرا `Quantity` و `StockCount` دو نوع جدا؟** چون قانونشان فرق دارد: تعدادِ سفارش هرگز صفر
> نیست، ولی موجودی انبار می‌تواند صفر باشد. یکی کردنشان یعنی یکی از دو قانون را از دست بدهیم.
> این دقیقاً همان کاری است که داکِ Data Abstraction در بند ۸٫۳ توصیه می‌کند: **تبدیل یک
> Precondition تکرارشونده به یک RI سطح نوع**.

### ۳٫۳ enum ها

| enum | مقادیر | منبع |
|---|---|---|
| `UserRole` | `CUSTOMER`, `ADMIN` | داستان‌های ۲۸ تا ۳۶ |
| `OrderStatus` | `AWAITING_PAYMENT`, `PAID`, `CANCELLED` | داستان ۲۳، ۲۴، ۲۶ |
| `PaymentStatus` | `PENDING`, `SUCCESSFUL`, `FAILED` | داستان ۲۳، ۲۴، ۲۵ |
| `SortOption` | `CHEAPEST`, `MOST_EXPENSIVE`, `NEWEST`, `MOST_POPULAR`, `HIGHEST_RATED` | داستان ۱۲ (عیناً همان پنج گزینه) |

«مهمان» نقش نیست — یعنی هنوز وارد نشده. پس در `UserRole` نیامده.

---

## ۴. روابط و تعدّد

```
User 1 ──────── 0..*  Address          (کاربر چند آدرس دارد)
User 1 ──────── 1     Cart             (هر کاربر دقیقاً یک سبد)
User 1 ──────── 0..*  Order            (تاریخچهٔ سفارش‌ها)
User 1 ──────── 0..*  Review           (نظرهای کاربر)

Category 1 ──── 0..*  Product          (هر محصول دقیقاً یک دسته‌بندی)

Cart 1 ───────── 0..*  CartLine        (هر خط = یک محصول + تعداد)
CartLine *  ───── 1     Product        (با ProductId، نه ارجاع مستقیم)

Order 1 ──────── 1..*  OrderLine       (سفارش حداقل یک قلم دارد)
Order 1 ──────── 1     Address         (کپی تغییرناپذیر، نه ارجاع)
Order 1 ──────── 0..*  Payment         (تلاش‌های پرداخت؛ داستان ۲۴ تلاش دوباره دارد)

Product 1 ────── 0..*  Review
Product 1 ────── 0..*  StockChange     (سابقهٔ تغییر موجودی؛ داستان ۲۹)
```

سه نکتهٔ طراحی در این روابط:

1. **`CartLine` و `OrderLine` به `ProductId` اشاره می‌کنند، نه به `Product`.** اگر مدیر بعداً
   قیمت محصول را عوض کند (داستان ۳۴)، سبد و سفارش ثبت‌شده نباید تغییر کنند. پس قیمت واحد
   **در لحظهٔ افزودن** داخل خط ذخیره می‌شود — همان کاری که داکِ Data Abstraction در بند ۳٫۱ می‌گوید.
2. **`Review` داخل `Product` نیست.** اگر بود، `Product` یک Aggregate غول‌پیکر می‌شد و خواندن
   مشخصات محصول یعنی بارکردن همهٔ نظرها. میانگین امتیاز را `ReviewService` حساب می‌کند.
3. **`Order` آدرس را کپی می‌کند.** داستان ۳۵ می‌گوید «اطلاعات سفارش‌های قبلی نباید حذف شوند».

---

## ۵. لایهٔ `model` — جزئیات کلاس‌ها

> جدول‌های این سه بخش **مستقیماً از روی کد جاوا تولید شده‌اند**، پس نام کلاس، نام فیلد،
> نام متد، نام پارامتر، نوع ورودی و نوع خروجی مو به مو با `src/main/java` یکی است.
> اسکریپت `tools/check_model_sync.py` این یکی بودن را بررسی می‌کند.

#### `Address`

| فیلد | نوع | |
|---|---|---|
| `id` | `AddressId` | final |
| `recipientName` | `String` |  |
| `phoneNumber` | `PhoneNumber` |  |
| `country` | `String` |  |
| `city` | `String` |  |
| `line` | `String` |  |
| `postalCode` | `PostalCode` |  |
| `title` | `String` |  |
| `defaultAddress` | `boolean` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Address` | `AddressId id, String recipientName, PhoneNumber phoneNumber, String country, String city, String line, PostalCode postalCode, String title` | — | public |
| `id` | `—` | `AddressId` | public |
| `recipientName` | `—` | `String` | public |
| `phoneNumber` | `—` | `PhoneNumber` | public |
| `country` | `—` | `String` | public |
| `city` | `—` | `String` | public |
| `line` | `—` | `String` | public |
| `postalCode` | `—` | `PostalCode` | public |
| `title` | `—` | `String` | public |
| `isDefault` | `—` | `boolean` | public |
| `updateDetails` | `String recipientName, PhoneNumber phoneNumber, String country, String city, String line, PostalCode postalCode, String title` | `void` | public |
| `markDefault` | `—` | `void` | package |
| `unmarkDefault` | `—` | `void` | package |
| `frozenCopy` | `—` | `Address` | package |
| `requireText` | `String value, String field` | `String` | private static |
| `requireNonNull` | `T value, String field` | `T` | private static |
| `checkRep` | `—` | `void` | private |
| `toString` | `—` | `String` | public |

#### `AddressId`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `AddressId` | `String value` | — | public |
| `newId` | `—` | `AddressId` | public static |
| `toString` | `—` | `String` | public |

#### `Cart`

| فیلد | نوع | |
|---|---|---|
| `ownerId` | `UserId` | final |
| `lines` | `Map<ProductId, CartLine>` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Cart` | `UserId ownerId` | — | public |
| `ownerId` | `—` | `UserId` | public |
| `lines` | `—` | `List<CartLine>` | public |
| `quantityOf` | `ProductId productId` | `int` | public |
| `itemsSubtotal` | `—` | `Money` | public |
| `isEmpty` | `—` | `boolean` | public |
| `addProduct` | `Product product, Quantity quantity` | `void` | public |
| `changeQuantity` | `ProductId productId, Quantity quantity` | `void` | public |
| `removeProduct` | `ProductId productId` | `void` | public |
| `clear` | `—` | `void` | public |
| `checkRep` | `—` | `void` | private |

#### `CartLine`  (record)

| فیلد | نوع | |
|---|---|---|
| `productId` | `ProductId` | final |
| `productName` | `String` | final |
| `unitPrice` | `Money` | final |
| `quantity` | `Quantity` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `CartLine` | `ProductId productId, String productName, Money unitPrice, Quantity quantity` | — | public |
| `of` | `Product product, Quantity quantity` | `CartLine` | public static |
| `lineTotal` | `—` | `Money` | public |
| `withQuantity` | `Quantity newQuantity` | `CartLine` | public |

#### `Category`

| فیلد | نوع | |
|---|---|---|
| `id` | `CategoryId` | final |
| `name` | `String` |  |
| `active` | `boolean` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Category` | `CategoryId id, String name` | — | public |
| `id` | `—` | `CategoryId` | public |
| `name` | `—` | `String` | public |
| `isActive` | `—` | `boolean` | public |
| `rename` | `String name` | `void` | public |
| `deactivate` | `—` | `void` | public |
| `requireName` | `String name` | `String` | private static |
| `checkRep` | `—` | `void` | private |
| `toString` | `—` | `String` | public |

#### `CategoryId`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `CategoryId` | `String value` | — | public |
| `newId` | `—` | `CategoryId` | public static |
| `toString` | `—` | `String` | public |

#### `Email`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

ثابت‌ها: `VALID_FORMAT` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Email` | `String value` | — | public |
| `toString` | `—` | `String` | public |

#### `Money`  (record)

| فیلد | نوع | |
|---|---|---|
| `amount` | `BigDecimal` | final |

ثابت‌ها: `DECIMAL_PLACES` (خصوصی) · `ZERO`

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Money` | `BigDecimal amount` | — | public |
| `of` | `String amount` | `Money` | public static |
| `plus` | `Money other` | `Money` | public |
| `minus` | `Money other` | `Money` | public |
| `times` | `int factor` | `Money` | public |
| `isLessThan` | `Money other` | `boolean` | public |
| `isGreaterThan` | `Money other` | `boolean` | public |
| `toString` | `—` | `String` | public |

#### `Order`

| فیلد | نوع | |
|---|---|---|
| `id` | `OrderId` | final |
| `customerId` | `UserId` | final |
| `lines` | `List<OrderLine>` | final |
| `shippingAddress` | `Address` | final |
| `itemsSubtotal` | `Money` | final |
| `shippingFee` | `Money` | final |
| `totalAmount` | `Money` | final |
| `placedAt` | `LocalDateTime` | final |
| `status` | `OrderStatus` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Order` | `OrderId id, UserId customerId, List<OrderLine> lines, Address shippingAddress, Money shippingFee, LocalDateTime placedAt` | — | public |
| `id` | `—` | `OrderId` | public |
| `customerId` | `—` | `UserId` | public |
| `lines` | `—` | `List<OrderLine>` | public |
| `shippingAddress` | `—` | `Address` | public |
| `itemsSubtotal` | `—` | `Money` | public |
| `shippingFee` | `—` | `Money` | public |
| `totalAmount` | `—` | `Money` | public |
| `placedAt` | `—` | `LocalDateTime` | public |
| `status` | `—` | `OrderStatus` | public |
| `isPayable` | `—` | `boolean` | public |
| `isPaid` | `—` | `boolean` | public |
| `markPaid` | `—` | `void` | public |
| `cancel` | `—` | `void` | public |
| `checkRep` | `—` | `void` | private |
| `toString` | `—` | `String` | public |

#### `OrderId`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `OrderId` | `String value` | — | public |
| `newId` | `—` | `OrderId` | public static |
| `toString` | `—` | `String` | public |

#### `OrderLine`  (record)

| فیلد | نوع | |
|---|---|---|
| `productId` | `ProductId` | final |
| `productName` | `String` | final |
| `unitPrice` | `Money` | final |
| `quantity` | `Quantity` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `OrderLine` | `ProductId productId, String productName, Money unitPrice, Quantity quantity` | — | public |
| `from` | `CartLine cartLine` | `OrderLine` | public static |
| `lineTotal` | `—` | `Money` | public |

#### `OrderStatus`  (enum)

مقادیر: `AWAITING_PAYMENT` · `PAID`

#### `PasswordHash`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

ثابت‌ها: `ALGORITHM` (خصوصی) · `MIN_RAW_LENGTH` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `PasswordHash` | `String value` | — | public |
| `of` | `String rawPassword` | `PasswordHash` | public static |
| `matches` | `String rawPassword` | `boolean` | public |
| `digestOf` | `String rawPassword` | `String` | private static |
| `toString` | `—` | `String` | public |

#### `Payment`

| فیلد | نوع | |
|---|---|---|
| `id` | `PaymentId` | final |
| `orderId` | `OrderId` | final |
| `amount` | `Money` | final |
| `attemptedAt` | `LocalDateTime` | final |
| `status` | `PaymentStatus` |  |
| `gatewayReference` | `String` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Payment` | `PaymentId id, OrderId orderId, Money amount, LocalDateTime attemptedAt` | — | public |
| `id` | `—` | `PaymentId` | public |
| `orderId` | `—` | `OrderId` | public |
| `amount` | `—` | `Money` | public |
| `attemptedAt` | `—` | `LocalDateTime` | public |
| `status` | `—` | `PaymentStatus` | public |
| `gatewayReference` | `—` | `String` | public |
| `isSuccessful` | `—` | `boolean` | public |
| `markSuccessful` | `String reference` | `void` | public |
| `markFailed` | `—` | `void` | public |
| `requirePending` | `—` | `void` | private |
| `checkRep` | `—` | `void` | private |

#### `PaymentId`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `PaymentId` | `String value` | — | public |
| `newId` | `—` | `PaymentId` | public static |
| `toString` | `—` | `String` | public |

#### `PaymentStatus`  (enum)

مقادیر: `PENDING` · `SUCCESSFUL`

#### `PhoneNumber`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

ثابت‌ها: `VALID_FORMAT` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `PhoneNumber` | `String value` | — | public |
| `toString` | `—` | `String` | public |

#### `PostalCode`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

ثابت‌ها: `VALID_FORMAT` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `PostalCode` | `String value` | — | public |
| `toString` | `—` | `String` | public |

#### `Product`

| فیلد | نوع | |
|---|---|---|
| `id` | `ProductId` | final |
| `createdAt` | `LocalDateTime` | final |
| `imageUrls` | `List<String>` | final |
| `name` | `String` |  |
| `description` | `String` |  |
| `brand` | `String` |  |
| `price` | `Money` |  |
| `stock` | `StockCount` |  |
| `categoryId` | `CategoryId` |  |
| `active` | `boolean` |  |
| `timesSold` | `int` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Product` | `ProductId id, String name, String description, Money price, StockCount stock, CategoryId categoryId, String brand, LocalDateTime createdAt` | — | public |
| `id` | `—` | `ProductId` | public |
| `name` | `—` | `String` | public |
| `description` | `—` | `String` | public |
| `price` | `—` | `Money` | public |
| `stock` | `—` | `StockCount` | public |
| `categoryId` | `—` | `CategoryId` | public |
| `brand` | `—` | `String` | public |
| `createdAt` | `—` | `LocalDateTime` | public |
| `timesSold` | `—` | `int` | public |
| `imageUrls` | `—` | `List<String>` | public |
| `isActive` | `—` | `boolean` | public |
| `isPurchasable` | `—` | `boolean` | public |
| `hasEnoughStock` | `Quantity quantity` | `boolean` | public |
| `rename` | `String name` | `void` | public |
| `changeDescription` | `String description` | `void` | public |
| `changePrice` | `Money price` | `void` | public |
| `changeBrand` | `String brand` | `void` | public |
| `moveToCategory` | `CategoryId categoryId` | `void` | public |
| `addImage` | `String url` | `void` | public |
| `reduceStock` | `Quantity quantity` | `void` | public |
| `increaseStock` | `int amount` | `void` | public |
| `recordSale` | `Quantity quantity` | `void` | public |
| `deactivate` | `—` | `void` | public |
| `activate` | `—` | `void` | public |
| `requireText` | `String value, String field` | `String` | private static |
| `checkRep` | `—` | `void` | private |
| `toString` | `—` | `String` | public |

#### `ProductId`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ProductId` | `String value` | — | public |
| `newId` | `—` | `ProductId` | public static |
| `toString` | `—` | `String` | public |

#### `Quantity`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `int` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Quantity` | `int value` | — | public |
| `plus` | `Quantity other` | `Quantity` | public |
| `toString` | `—` | `String` | public |

#### `Rating`  (record)

| فیلد | نوع | |
|---|---|---|
| `stars` | `int` | final |

ثابت‌ها: `MIN_STARS` (خصوصی) · `MAX_STARS` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Rating` | `int stars` | — | public |
| `toString` | `—` | `String` | public |

#### `Review`  (record)

| فیلد | نوع | |
|---|---|---|
| `id` | `ReviewId` | final |
| `productId` | `ProductId` | final |
| `authorId` | `UserId` | final |
| `text` | `String` | final |
| `rating` | `Rating` | final |
| `createdAt` | `LocalDateTime` | final |

ثابت‌ها: `MAX_TEXT_LENGTH`

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `Review` | `ReviewId id, ProductId productId, UserId authorId, String text, Rating rating, LocalDateTime createdAt` | — | public |

#### `ReviewId`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ReviewId` | `String value` | — | public |
| `newId` | `—` | `ReviewId` | public static |
| `toString` | `—` | `String` | public |

#### `SearchPhrase`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

ثابت‌ها: `MAX_LENGTH`

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `SearchPhrase` | `String value` | — | public |
| `matches` | `String text` | `boolean` | public |
| `toString` | `—` | `String` | public |

#### `SortOption`  (enum)

مقادیر: `CHEAPEST` · `MOST_EXPENSIVE` · `NEWEST` · `MOST_POPULAR`

#### `StockChange`  (record)

| فیلد | نوع | |
|---|---|---|
| `productId` | `ProductId` | final |
| `delta` | `int` | final |
| `reason` | `String` | final |
| `changedBy` | `UserId` | final |
| `changedAt` | `LocalDateTime` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `StockChange` | `ProductId productId, int delta, String reason, UserId changedBy, LocalDateTime changedAt` | — | public |
| `isIncrease` | `—` | `boolean` | public |

#### `StockCount`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `int` | final |

ثابت‌ها: `NONE`

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `StockCount` | `int value` | — | public |
| `canCover` | `Quantity quantity` | `boolean` | public |
| `decreaseBy` | `Quantity quantity` | `StockCount` | public |
| `increaseBy` | `int amount` | `StockCount` | public |
| `isEmpty` | `—` | `boolean` | public |
| `toString` | `—` | `String` | public |

#### `User`

| فیلد | نوع | |
|---|---|---|
| `id` | `UserId` | final |
| `role` | `UserRole` | final |
| `createdAt` | `LocalDateTime` | final |
| `addresses` | `List<Address>` | final |
| `email` | `Email` |  |
| `phoneNumber` | `PhoneNumber` |  |
| `fullName` | `String` |  |
| `passwordHash` | `PasswordHash` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `User` | `UserId id, Email email, PhoneNumber phoneNumber, String fullName, PasswordHash passwordHash, UserRole role, LocalDateTime createdAt` | — | public |
| `id` | `—` | `UserId` | public |
| `email` | `—` | `Email` | public |
| `phoneNumber` | `—` | `PhoneNumber` | public |
| `fullName` | `—` | `String` | public |
| `role` | `—` | `UserRole` | public |
| `createdAt` | `—` | `LocalDateTime` | public |
| `isAdmin` | `—` | `boolean` | public |
| `matchesPassword` | `String rawPassword` | `boolean` | public |
| `addresses` | `—` | `List<Address>` | public |
| `findDefaultAddress` | `—` | `Optional<Address>` | public |
| `findAddress` | `AddressId addressId` | `Optional<Address>` | public |
| `changePassword` | `PasswordHash newPasswordHash` | `void` | public |
| `updateProfile` | `String fullName, Email email, PhoneNumber phoneNumber` | `void` | public |
| `addAddress` | `Address address` | `void` | public |
| `removeAddress` | `AddressId addressId` | `void` | public |
| `markDefaultAddress` | `AddressId addressId` | `void` | public |
| `requireOwnAddress` | `AddressId addressId` | `Address` | private |
| `requireFullName` | `String fullName` | `String` | private static |
| `checkRep` | `—` | `void` | private |
| `toString` | `—` | `String` | public |

#### `UserId`  (record)

| فیلد | نوع | |
|---|---|---|
| `value` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `UserId` | `String value` | — | public |
| `newId` | `—` | `UserId` | public static |
| `toString` | `—` | `String` | public |

#### `UserRole`  (enum)

مقادیر: `CUSTOMER`

---

## ۶. لایهٔ `service` — جزئیات کلاس‌ها

> جدول‌های این سه بخش **مستقیماً از روی کد جاوا تولید شده‌اند**، پس نام کلاس، نام فیلد،
> نام متد، نام پارامتر، نوع ورودی و نوع خروجی مو به مو با `src/main/java` یکی است.
> اسکریپت `tools/check_model_sync.py` این یکی بودن را بررسی می‌کند.

هر سرویس داده‌اش را در `Map`/`List` درون‌حافظه نگه می‌دارد و این‌ها در `Main` ساخته و
تزریق می‌شوند — همان الگوی اسلاید `OrderService(Map<Long, Product> products)`.

#### `AddressService`

| فیلد | نوع | |
|---|---|---|
| `users` | `Map<UserId, User>` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `AddressService` | `Map<UserId, User> users` | — | public |
| `addAddress` | `UserId ownerId, String recipientName, PhoneNumber phoneNumber, String country, String city, String line, PostalCode postalCode, String title` | `Address` | public |
| `updateAddress` | `UserId ownerId, AddressId addressId, String recipientName, PhoneNumber phoneNumber, String country, String city, String line, PostalCode postalCode, String title` | `void` | public |
| `removeAddress` | `UserId ownerId, AddressId addressId` | `void` | public |
| `markDefault` | `UserId ownerId, AddressId addressId` | `void` | public |
| `listAddresses` | `UserId ownerId` | `List<Address>` | public |
| `requireAddress` | `UserId ownerId, AddressId addressId` | `Address` | private |
| `requireUser` | `UserId userId` | `User` | private |

#### `CartService`

| فیلد | نوع | |
|---|---|---|
| `carts` | `Map<UserId, Cart>` | final |
| `products` | `Map<ProductId, Product>` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `CartService` | `Map<UserId, Cart> carts, Map<ProductId, Product> products` | — | public |
| `cartOf` | `UserId ownerId` | `Cart` | public |
| `addToCart` | `UserId ownerId, ProductId productId, Quantity quantity` | `void` | public |
| `changeQuantity` | `UserId ownerId, ProductId productId, Quantity quantity` | `void` | public |
| `removeFromCart` | `UserId ownerId, ProductId productId` | `void` | public |
| `clearCart` | `UserId ownerId` | `void` | public |
| `requireStockCovers` | `Product product, Quantity quantity` | `void` | private |
| `requirePurchasableProduct` | `ProductId productId` | `Product` | private |
| `requireKnownProduct` | `ProductId productId` | `Product` | private |

#### `CatalogService`

| فیلد | نوع | |
|---|---|---|
| `products` | `Map<ProductId, Product>` | final |
| `categories` | `Map<CategoryId, Category>` | final |
| `reviewService` | `ReviewService` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `CatalogService` | `Map<ProductId, Product> products, Map<CategoryId, Category> categories, ReviewService reviewService` | — | public |
| `listActiveCategories` | `—` | `List<Category>` | public |
| `listActiveProducts` | `—` | `List<Product>` | public |
| `productsInCategory` | `CategoryId categoryId` | `List<Product>` | public |
| `findProduct` | `ProductId productId` | `Optional<Product>` | public |
| `findCategory` | `CategoryId categoryId` | `Optional<Category>` | public |
| `search` | `SearchPhrase phrase` | `List<Product>` | public |
| `filter` | `ProductFilterRequest filter` | `List<Product>` | public |
| `sort` | `List<Product> productsToSort, SortOption option` | `List<Product>` | public |
| `ratingOf` | `Product product` | `double` | public |
| `comparatorFor` | `SortOption option` | `Comparator<Product>` | private |

#### `GatewayResult`  (record)

| فیلد | نوع | |
|---|---|---|
| `approved` | `boolean` | final |
| `reference` | `String` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `GatewayResult` | `boolean approved, String reference` | — | public |
| `approved` | `String reference` | `GatewayResult` | public static |
| `refused` | `String reason` | `GatewayResult` | public static |

#### `InventoryService`

| فیلد | نوع | |
|---|---|---|
| `products` | `Map<ProductId, Product>` | final |
| `changeLog` | `List<StockChange>` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `InventoryService` | `Map<ProductId, Product> products, List<StockChange> changeLog` | — | public |
| `viewStock` | `User actor` | `List<Product>` | public |
| `changeStock` | `User actor, ProductId productId, int delta, String reason` | `void` | public |
| `changeLogOf` | `User actor, ProductId productId` | `List<StockChange>` | public |
| `requireProduct` | `ProductId productId` | `Product` | private |
| `requireAdmin` | `User actor` | `void` | private static |

#### `OrderService`

| فیلد | نوع | |
|---|---|---|
| `orders` | `Map<OrderId, Order>` | final |
| `users` | `Map<UserId, User>` | final |
| `products` | `Map<ProductId, Product>` | final |
| `cartService` | `CartService` | final |

ثابت‌ها: `SHIPPING_FEE` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `OrderService` | `Map<OrderId, Order> orders, Map<UserId, User> users, Map<ProductId, Product> products, CartService cartService` | — | public |
| `shippingFee` | `—` | `Money` | public |
| `startCheckout` | `UserId customerId, AddressId shippingAddressId` | `Order` | public |
| `findOrder` | `OrderId orderId` | `Optional<Order>` | public |
| `findOrderOfCustomer` | `OrderId orderId, UserId customerId` | `Optional<Order>` | public |
| `ordersOf` | `UserId customerId` | `List<Order>` | public |
| `allOrders` | `User actor` | `List<Order>` | public |
| `ordersByStatus` | `User actor, OrderStatus status` | `List<Order>` | public |
| `hasPurchased` | `UserId customerId, ProductId productId` | `boolean` | public |
| `requireEveryLineStillAvailable` | `List<CartLine> cartLines` | `void` | private |
| `takeOrderedUnitsOffTheShelf` | `List<OrderLine> orderLines` | `void` | private |
| `requireUser` | `UserId userId` | `User` | private |
| `requireAdmin` | `User actor` | `void` | private static |

#### `PaymentGateway`  (interface)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `authorize` | `OrderId orderId, Money amount` | `GatewayResult` | package |

#### `PaymentService`

| فیلد | نوع | |
|---|---|---|
| `payments` | `Map<PaymentId, Payment>` | final |
| `orderService` | `OrderService` | final |
| `gateway` | `PaymentGateway` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `PaymentService` | `Map<PaymentId, Payment> payments, OrderService orderService, PaymentGateway gateway` | — | public |
| `pay` | `OrderId orderId, UserId payerId` | `Payment` | public |
| `paymentsOf` | `OrderId orderId` | `List<Payment>` | public |
| `confirm` | `Payment attempt, Order order, String reference` | `void` | private |

#### `ProductAdminService`

| فیلد | نوع | |
|---|---|---|
| `products` | `Map<ProductId, Product>` | final |
| `categories` | `Map<CategoryId, Category>` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ProductAdminService` | `Map<ProductId, Product> products, Map<CategoryId, Category> categories` | — | public |
| `createProduct` | `User actor, String name, String description, Money price, StockCount stock, CategoryId categoryId, String brand` | `Product` | public |
| `updateProduct` | `User actor, ProductId productId, String name, String description, Money price, CategoryId categoryId, String brand` | `void` | public |
| `deactivateProduct` | `User actor, ProductId productId` | `void` | public |
| `activateProduct` | `User actor, ProductId productId` | `void` | public |
| `createCategory` | `User actor, String name` | `Category` | public |
| `requireKnownCategory` | `CategoryId categoryId` | `void` | private |
| `requireProduct` | `ProductId productId` | `Product` | private |
| `requireAdmin` | `User actor` | `void` | private static |

#### `ProductFilterRequest`  (record)

| فیلد | نوع | |
|---|---|---|
| `categoryId` | `CategoryId` | final |
| `minPrice` | `Money` | final |
| `maxPrice` | `Money` | final |
| `brand` | `String` | final |
| `minRating` | `Rating` | final |
| `inStockOnly` | `boolean` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ProductFilterRequest` | `CategoryId categoryId, Money minPrice, Money maxPrice, String brand, Rating minRating, boolean inStockOnly` | — | public |
| `none` | `—` | `ProductFilterRequest` | public static |
| `accepts` | `Product product, double averageRating` | `boolean` | public |

#### `ReviewService`

| فیلد | نوع | |
|---|---|---|
| `reviews` | `Map<ReviewId, Review>` | final |
| `orderService` | `OrderService` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ReviewService` | `Map<ReviewId, Review> reviews, OrderService orderService` | — | public |
| `addReview` | `UserId authorId, ProductId productId, String text, Rating rating` | `Review` | public |
| `reviewsOf` | `ProductId productId` | `List<Review>` | public |
| `averageRating` | `ProductId productId` | `double` | public |
| `reviewCount` | `ProductId productId` | `int` | public |
| `hasReviewed` | `UserId authorId, ProductId productId` | `boolean` | private |

#### `SimulatedPaymentGateway`

| فیلد | نوع | |
|---|---|---|
| `approving` | `boolean` |  |
| `refusalReason` | `String` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `approveEverything` | `—` | `void` | public |
| `refuseEverything` | `String reason` | `void` | public |
| `isApproving` | `—` | `boolean` | public |
| `authorize` | `OrderId orderId, Money amount` | `GatewayResult` | public |

#### `UserService`

| فیلد | نوع | |
|---|---|---|
| `users` | `Map<UserId, User>` | final |
| `resetTokens` | `Map<String, UserId>` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `UserService` | `Map<UserId, User> users` | — | public |
| `signUp` | `Email email, PhoneNumber phoneNumber, String fullName, String rawPassword` | `User` | public |
| `registerAdmin` | `Email email, PhoneNumber phoneNumber, String fullName, String rawPassword` | `User` | public |
| `logIn` | `String emailOrPhone, String rawPassword` | `User` | public |
| `requestPasswordReset` | `Email email` | `String` | public |
| `resetPassword` | `String token, String newPassword` | `void` | public |
| `findById` | `UserId userId` | `Optional<User>` | public |
| `updateProfile` | `UserId userId, String fullName, Email email, PhoneNumber phoneNumber` | `User` | public |
| `findByEmailOrPhone` | `String emailOrPhone` | `Optional<User>` | private |
| `findByEmail` | `Email email` | `Optional<User>` | private |
| `findByPhoneNumber` | `PhoneNumber phoneNumber` | `Optional<User>` | private |
| `requireFreeEmail` | `Email email` | `void` | private |
| `requireFreePhoneNumber` | `PhoneNumber phoneNumber` | `void` | private |
| `requireUser` | `UserId userId` | `User` | private |

---

## ۷. لایهٔ `cli` و `Main` — جزئیات کلاس‌ها

> جدول‌های این سه بخش **مستقیماً از روی کد جاوا تولید شده‌اند**، پس نام کلاس، نام فیلد،
> نام متد، نام پارامتر، نوع ورودی و نوع خروجی مو به مو با `src/main/java` یکی است.
> اسکریپت `tools/check_model_sync.py` این یکی بودن را بررسی می‌کند.

`System.out` و `Scanner` تنها در `ConsoleIo` ظاهر می‌شوند.

#### `AdminCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `inventoryService` | `InventoryService` | final |
| `productAdminService` | `ProductAdminService` | final |
| `catalogService` | `CatalogService` | final |
| `orderService` | `OrderService` | final |

ثابت‌ها: `VIEW_STOCK` (خصوصی) · `CHANGE_STOCK` (خصوصی) · `CREATE_PRODUCT` (خصوصی) · `EDIT_PRODUCT` (خصوصی) · `DEACTIVATE_PRODUCT` (خصوصی) · `CREATE_CATEGORY` (خصوصی) · `ALL_ORDERS` (خصوصی) · `ORDERS_BY_STATUS` (خصوصی) · `BACK` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `AdminCli` | `ConsoleIo io, Session session, InventoryService inventoryService, ProductAdminService productAdminService, CatalogService catalogService, OrderService orderService` | — | public |
| `run` | `—` | `void` | public |
| `viewStock` | `—` | `void` | private |
| `changeStock` | `—` | `void` | private |
| `showChangeLog` | `Product product` | `void` | private |
| `createProduct` | `—` | `void` | private |
| `editProduct` | `—` | `void` | private |
| `deactivateProduct` | `—` | `void` | private |
| `createCategory` | `—` | `void` | private |
| `showOrdersByStatus` | `—` | `void` | private |
| `showOrders` | `List<Order> orders` | `void` | private |
| `chooseProduct` | `String prompt, List<Product> products` | `Optional<Product>` | private |
| `chooseCategory` | `—` | `Optional<Category>` | private |
| `allProducts` | `—` | `List<Product>` | private |
| `admin` | `—` | `User` | private |

#### `AuthCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `userService` | `UserService` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `AuthCli` | `ConsoleIo io, Session session, UserService userService` | — | public |
| `signUp` | `—` | `void` | public |
| `logIn` | `—` | `void` | public |
| `logOut` | `—` | `void` | public |
| `recoverPassword` | `—` | `void` | public |

#### `CartCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `cartService` | `CartService` | final |

ثابت‌ها: `CHANGE_QUANTITY` (خصوصی) · `REMOVE_LINE` (خصوصی) · `EMPTY_CART` (خصوصی) · `BACK` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `CartCli` | `ConsoleIo io, Session session, CartService cartService` | — | public |
| `run` | `—` | `void` | public |
| `showCart` | `—` | `void` | private |
| `changeQuantity` | `—` | `void` | private |
| `removeLine` | `—` | `void` | private |
| `emptyCart` | `—` | `void` | private |
| `chooseLine` | `String prompt` | `Optional<CartLine>` | private |
| `currentCart` | `—` | `Cart` | private |
| `labelsOf` | `List<CartLine> lines` | `List<String>` | private |

#### `CatalogCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `catalogService` | `CatalogService` | final |
| `cartService` | `CartService` | final |
| `reviewService` | `ReviewService` | final |

ثابت‌ها: `ALL_PRODUCTS` (خصوصی) · `BY_CATEGORY` (خصوصی) · `SEARCH` (خصوصی) · `FILTER` (خصوصی) · `BACK` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `CatalogCli` | `ConsoleIo io, Session session, CatalogService catalogService, CartService cartService, ReviewService reviewService` | — | public |
| `run` | `—` | `void` | public |
| `browseByCategory` | `—` | `void` | private |
| `search` | `—` | `void` | private |
| `filter` | `—` | `void` | private |
| `showProducts` | `List<Product> found` | `void` | private |
| `askForSortOption` | `—` | `SortOption` | private |
| `labelOf` | `SortOption option` | `String` | private |
| `showProductDetails` | `Product product` | `void` | private |
| `addToCart` | `Product product` | `void` | private |
| `showReviews` | `Product product` | `void` | private |
| `ratingTextOf` | `Product product` | `String` | private |
| `categoryNameOf` | `Product product` | `String` | private |
| `picturesOf` | `Product product` | `String` | private |
| `labelsOf` | `List<Product> shown` | `List<String>` | private |

#### `CheckoutCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `cartService` | `CartService` | final |
| `addressService` | `AddressService` | final |
| `orderService` | `OrderService` | final |
| `paymentService` | `PaymentService` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `CheckoutCli` | `ConsoleIo io, Session session, CartService cartService, AddressService addressService, OrderService orderService, PaymentService paymentService` | — | public |
| `run` | `—` | `void` | public |
| `showOrderSummary` | `Cart cart, Address shippingAddress` | `void` | private |
| `payFor` | `Order order` | `void` | private |
| `chooseShippingAddress` | `—` | `Optional<Address>` | private |

#### `ConsoleIo`

| فیلد | نوع | |
|---|---|---|
| `scanner` | `Scanner` | final |

ثابت‌ها: `PROMPT_SUFFIX` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ConsoleIo` | `Scanner scanner` | — | public |
| `print` | `String message` | `void` | public |
| `printBlankLine` | `—` | `void` | public |
| `printTitle` | `String title` | `void` | public |
| `printError` | `String message` | `void` | public |
| `printSuccess` | `String message` | `void` | public |
| `printNumberedList` | `List<String> items, String emptyNote` | `void` | public |
| `readLine` | `String prompt` | `String` | public |
| `readOptionalLine` | `String prompt` | `Optional<String>` | public |
| `readInt` | `String prompt, int lowest, int highest` | `int` | public |
| `confirm` | `String prompt` | `boolean` | public |
| `chooseFrom` | `String prompt, List<T> items, List<String> labels` | `Optional<T>` | public |

#### `OrderCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `orderService` | `OrderService` | final |
| `paymentService` | `PaymentService` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `OrderCli` | `ConsoleIo io, Session session, OrderService orderService, PaymentService paymentService` | — | public |
| `run` | `—` | `void` | public |
| `showOrderDetails` | `Order order` | `void` | private |
| `showPaymentAttempts` | `Order order` | `void` | private |
| `retryPayment` | `Order order` | `void` | private |
| `labelsOf` | `List<Order> orders` | `List<String>` | private |

#### `ProfileCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `userService` | `UserService` | final |
| `addressService` | `AddressService` | final |

ثابت‌ها: `VIEW_PROFILE` (خصوصی) · `EDIT_PROFILE` (خصوصی) · `LIST_ADDRESSES` (خصوصی) · `ADD_ADDRESS` (خصوصی) · `EDIT_ADDRESS` (خصوصی) · `REMOVE_ADDRESS` (خصوصی) · `MARK_DEFAULT_ADDRESS` (خصوصی) · `BACK` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ProfileCli` | `ConsoleIo io, Session session, UserService userService, AddressService addressService` | — | public |
| `run` | `—` | `void` | public |
| `showProfile` | `—` | `void` | private |
| `editProfile` | `—` | `void` | private |
| `listAddresses` | `—` | `void` | private |
| `addAddress` | `—` | `void` | private |
| `editAddress` | `—` | `void` | private |
| `removeAddress` | `—` | `void` | private |
| `markDefaultAddress` | `—` | `void` | private |
| `chooseAddress` | `String prompt` | `Optional<Address>` | private |
| `addressLabels` | `—` | `List<String>` | private |

#### `ReviewCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `orderService` | `OrderService` | final |
| `reviewService` | `ReviewService` | final |

ثابت‌ها: `LOWEST_RATING` (خصوصی) · `HIGHEST_RATING` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ReviewCli` | `ConsoleIo io, Session session, OrderService orderService, ReviewService reviewService` | — | public |
| `run` | `—` | `void` | public |
| `purchasedLines` | `User customer` | `List<OrderLine>` | private |

#### `Session`

| فیلد | نوع | |
|---|---|---|
| `currentUser` | `User` |  |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `isLoggedIn` | `—` | `boolean` | public |
| `isAdminLoggedIn` | `—` | `boolean` | public |
| `currentUser` | `—` | `Optional<User>` | public |
| `requireCurrentUser` | `—` | `User` | public |
| `logIn` | `User user` | `void` | public |
| `logOut` | `—` | `void` | public |

#### `ShopCli`

| فیلد | نوع | |
|---|---|---|
| `io` | `ConsoleIo` | final |
| `session` | `Session` | final |
| `authCli` | `AuthCli` | final |
| `profileCli` | `ProfileCli` | final |
| `catalogCli` | `CatalogCli` | final |
| `cartCli` | `CartCli` | final |
| `checkoutCli` | `CheckoutCli` | final |
| `orderCli` | `OrderCli` | final |
| `reviewCli` | `ReviewCli` | final |
| `adminCli` | `AdminCli` | final |

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `ShopCli` | `ConsoleIo io, Session session, AuthCli authCli, ProfileCli profileCli, CatalogCli catalogCli, CartCli cartCli, CheckoutCli checkoutCli, OrderCli orderCli, ReviewCli reviewCli, AdminCli adminCli` | — | public |
| `run` | `—` | `void` | public |
| `runVisitorMenu` | `—` | `boolean` | private |
| `runSignedInMenu` | `—` | `boolean` | private |
| `keepGoingAfter` | `Runnable screen` | `boolean` | private |

### `Main`

#### `Main`

ثابت‌ها: `DEMO_ADMIN_EMAIL` (خصوصی) · `DEMO_ADMIN_PASSWORD` (خصوصی)

| متد | ورودی | خروجی | دسترسی |
|---|---|---|---|
| `main` | `String[] args` | `void` | public static |
| `seedShop` | `UserService userService, ProductAdminService productAdminService` | `void` | private static |

---

## ۸. نگاشت ۳۶ داستان کاربری

| # | داستان | کلاس و متد |
|---|---|---|
| ۱ | ثبت‌نام | `UserService.signUp` |
| ۲ | ورود | `UserService.logIn` → `Session.logIn` |
| ۳ | خروج | `Session.logOut` |
| ۴ | فراموشی رمز | `UserService.requestPasswordReset` + `resetPassword` |
| ۵ | مشاهده پروفایل | `UserService.findById` (رمز هرگز نمایش داده نمی‌شود) |
| ۶ | ویرایش پروفایل | `UserService.updateProfile` |
| ۷ | مدیریت آدرس | `AddressService.*` |
| ۸ | مشاهده محصولات | `CatalogService.listActiveProducts` |
| ۹ | جزئیات محصول | `CatalogService.findProduct` + `ReviewService.averageRating` |
| ۱۰ | جستجو | `CatalogService.search(SearchPhrase)` |
| ۱۱ | فیلتر | `CatalogService.filter(ProductFilterRequest filter)` |
| ۱۲ | مرتب‌سازی | `CatalogService.sort(…, SortOption)` |
| ۱۳ | مشاهده دسته‌بندی‌ها | `CatalogService.listActiveCategories` |
| ۱۴ | محصولات یک دسته | `CatalogService.productsInCategory` |
| ۱۵ | افزودن به سبد | `CartService.addToCart` → `Cart.addProduct` |
| ۱۶ | مشاهده سبد | `CartService.cartOf` → `Cart.itemsSubtotal` |
| ۱۷ | تغییر تعداد | `CartService.changeQuantity` |
| ۱۸ | حذف از سبد | `CartService.removeFromCart` |
| ۱۹ | خالی کردن سبد | `CartService.clearCart` |
| ۲۰ | شروع خرید | `OrderService.startCheckout` |
| ۲۱ | انتخاب آدرس | پارامتر `AddressId` در `startCheckout` |
| ۲۲ | مشاهده مبلغ نهایی | `Order.totalAmount` |
| ۲۳ | پرداخت | `PaymentService.pay` |
| ۲۴ | پرداخت ناموفق | `Payment.markFailed` + `Order.isPayable` |
| ۲۵ | تأیید پرداخت | `PaymentService.confirm` (خصوصی، از دل `pay` صدا زده می‌شود) |
| ۲۶ | مشاهده سفارش‌ها | `OrderService.ordersOf` |
| ۲۷ | جزئیات سفارش | `OrderService.findOrder` |
| ۲۸ | مشاهده موجودی | `InventoryService.viewStock` |
| ۲۹ | تغییر موجودی | `InventoryService.changeStock` + `StockChange` |
| ۳۰ | ثبت نظر | `ReviewService.addReview` |
| ۳۱ | امتیاز دادن | `Rating` + `ReviewService.addReview` |
| ۳۲ | مشاهده نظرات | `ReviewService.reviewsOf` |
| ۳۳ | ایجاد محصول | `ProductAdminService.createProduct` |
| ۳۴ | ویرایش محصول | `ProductAdminService.updateProduct` |
| ۳۵ | غیرفعال کردن محصول | `ProductAdminService.deactivateProduct` → `Product.deactivate` |
| ۳۶ | مدیریت سفارش‌ها | `OrderService.allOrders` + `ordersByStatus` |

---

## ۹. قوانین دامنه و جای پیاده‌سازی‌شان

| قانون (از محدودیت‌های داستان‌ها) | کجا تضمین می‌شود |
|---|---|
| ایمیل قالب درست دارد | `Email` (سازنده) |
| ایمیل تکراری نیست | `UserService.signUp` |
| رمز رمزنگاری‌شده ذخیره می‌شود | `PasswordHash.of` |
| رمز هرگز نمایش داده نمی‌شود | `User` getter ای برای رمز ندارد |
| قیمت منفی نیست | `Money` (سازنده) |
| موجودی منفی نمی‌شود | `StockCount` (سازنده) + `Product.reduceStock` |
| تعداد حداقل ۱ است | `Quantity` (سازنده) |
| تعداد بیشتر از موجودی نیست | `Product.hasEnoughStock` |
| محصول تکراری در سبد، تعدادش جمع می‌شود | `Cart.addProduct` |
| فقط محصولات فعال دیده می‌شوند | `CatalogService` + `Product.isActive` |
| عبارت جستجو حداکثر ۱۰۰ کاراکتر | `SearchPhrase` (سازنده) |
| حداقل قیمت ≤ حداکثر قیمت | `ProductFilterRequest` (سازنده) |
| امتیاز بین ۱ و ۵ | `Rating` (سازنده) |
| فقط خریدار می‌تواند نظر بدهد | `ReviewService.addReview` |
| مبلغ را کاربر تعیین نمی‌کند | `Order` مبلغ را خودش حساب می‌کند |
| سفارش پرداخت‌شده دوباره پرداخت نمی‌شود | `Order.isPayable` |
| مبلغ پرداختی با مبلغ سفارش برابر است | `PaymentService.confirm` |
| فقط صاحب سبد/سفارش دسترسی دارد | `UserId` در امضای همهٔ متدهای سرویس |
| فقط مدیر | `User.isAdmin` در سرویس‌های مدیریتی |
| سفارش‌های گذشته حذف نمی‌شوند | `Product.deactivate` به سفارش‌ها دست نمی‌زند |
| اطلاعات شخصی نظردهنده پنهان است | `ReviewCli` فقط نام کوتاه‌شده را نمایش می‌دهد |
