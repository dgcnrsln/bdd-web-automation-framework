# BDD Web Automation Framework

Selenium WebDriver, Cucumber (BDD) ve TestNG üzerine inşa edilmiş; **JSON tabanlı veri/locator yönetimi**, **dependency injection**, **thread-safe paralel koşum**, **otomatik hata raporlama (Allure)** ve **CI/CD (GitHub Actions)** entegrasyonu ile donatılmış kurumsal seviyede bir web test otomasyon çatısıdır (framework).

Test senaryoları (Gherkin), test verileri, element locator'ları ve ortam (environment) bilgileri birbirinden tamamen izole edilmiştir; kod değişikliği gerektirmeden yeni senaryolar, yeni ortamlar ve yeni elementler eklenebilir.

Referans uygulama olarak [Sauce Demo](https://www.saucedemo.com/) — herkese açık bir e-ticaret demo sitesi — kullanılmıştır. **Login**, **Inventory** ve **Product Detail** sayfaları için toplam **20 senaryo** içerir.

---

## İçindekiler

- [Temel Özellikler](#temel-özellikler)
- [Mimari Genel Bakış](#mimari-genel-bakış)
- [Kullanılan Teknolojiler](#kullanılan-teknolojiler)
- [Proje Yapısı](#proje-yapısı)
- [Tasarım Desenleri ve Prensipler](#tasarım-desenleri-ve-prensipler)
- [Kurulum](#kurulum)
- [Testleri Çalıştırma](#testleri-çalıştırma)
- [Yeni Senaryo / Element / Veri Ekleme Rehberi](#yeni-senaryo--element--veri-ekleme-rehberi)
- [Desteklenen Step (Adım) Listesi](#desteklenen-step-adım-listesi)
- [Örnek Senaryolar](#örnek-senaryolar)
- [Hata Yönetimi ve Otomatik Screenshot](#hata-yönetimi-ve-otomatik-screenshot)
- [Loglama](#loglama)
- [Raporlama (Allure)](#raporlama-allure)
- [CI/CD (GitHub Actions)](#cicd-github-actions)
- [Gelecek Geliştirmeler](#gelecek-geliştirmeler)

---

## Temel Özellikler

- **Davranış Odaklı Geliştirme (BDD):** Cucumber ile yazılan, iş birimlerinin de okuyup anlayabileceği Gherkin senaryoları.
- **JSON Tabanlı Locator Yönetimi:** Element seçicileri (locator) Java kodundan tamamen bağımsız, `locators/*.json` dosyalarında tutulur.
- **JSON Tabanlı Test Verisi ve Ortam Yönetimi:** Test verileri (`testdata/*.json`) ve ortam URL'leri (`environments/*.json`) ayrı katmanlarda yönetilir; klasör yolları tek bir `ResourcePaths` sabitler sınıfında toplanır.
- **Dependency Injection (Cucumber PicoContainer):** Step definition sınıfları ve Actions/Repository katmanı manuel `new` çağrıları yerine constructor injection ile birbirine bağlanır; her senaryoda paylaşılan tek bir örnek kullanılır.
- **JSON Okuma Katmanında Cache:** `JsonDataReader`, aynı klasörü birden fazla kez diskten okumaz; thread-safe (`ConcurrentHashMap`) bir cache üzerinden çalışır.
- **Merkezi Konfigürasyon Yönetimi:** `ConfigReader`, tarayıcı, headless modu ve timeout gibi ayarları önce sistem property'sinden, yoksa `config.properties`'ten, o da yoksa kod içi varsayılandan okur.
- **Çoklu Tarayıcı ve Headless Desteği:** Chrome, Firefox ve Edge tarayıcıları `DriverFactory` üzerinden desteklenir; `headless=true` ile CI ortamlarında pencere açmadan çalıştırılabilir.
- **Thread-Safe Driver Yönetimi:** `ThreadLocal<WebDriver>` kullanılarak paralel test çalıştırmalarında tarayıcı oturumları birbirine karışmaz.
- **Paralel Çalıştırma Desteği:** TestNG suite tanımları ile tekli, paralel (method bazlı, 3 thread) ve rerun (yalnızca başarısız senaryolar) çalıştırma modları hazır durumdadır.
- **Generic ve Yeniden Kullanılabilir Step Definitions:** 21 adet generic Gherkin adımı ile çoğu senaryo yeni Java kodu yazılmadan kurgulanabilir.
- **Bağlamsal Hata Mesajları:** Element etkileşimleri başarısız olduğunda, hangi locator üzerinde başarısız olduğunu belirten anlamlı exception mesajları fırlatılır.
- **Başarısız Adımlarda Otomatik Screenshot:** Bir adım başarısız olduğunda ekran görüntüsü otomatik alınır ve doğrudan Allure raporuna gömülür.
- **Allure Raporlama:** Cucumber senaryoları, adımları, ekran görüntüleri ve retry geçmişi Allure üzerinden görselleştirilir.
- **Otomatik ve Manuel Rerun:** Başarısız senaryolar `rerun.txt`'e yazılır; hem aynı CI koşumu içinde otomatik hem de GitHub Actions üzerinden manuel olarak yalnızca başarısız senaryolar yeniden çalıştırılabilir.
- **SLF4J + Logback Loglama:** Her log satırına thread adı ve timestamp eklenir; paralel koşumda hangi logun hangi senaryoya ait olduğu netleşir.
- **CI/CD Entegrasyonu:** GitHub Actions ile push/PR'da otomatik, ayrıca manuel tetiklenebilir headless test koşumu, Allure raporu üretimi ve artifact yükleme.
- **Explicit Wait Stratejisi:** Tüm etkileşimler `WebDriverWait` ile senkronize edilerek kararsız (flaky) testlerin önüne geçilir.

---

## Mimari Genel Bakış

```
Gherkin Feature Dosyaları (.feature)
        │
        ▼
 Step Definitions  ──────► Repository Katmanı (Locator / Data / Environment)
   (DI ile enjekte             │
    edilen bağımlılıklar)      ▼
        │               JsonDataReader (Jackson + Cache)
        ▼                      │
   Actions Katmanı              ▼
 (Element / Navigation /   JSON Kaynak Dosyaları
  Wait / Verification /
  Alert / Browser)
        │
        ▼
   DriverManager (ThreadLocal)
        │
        ▼
   DriverFactory ──► Selenium WebDriver (Chrome / Firefox / Edge)
        │
        ▼
   ConfigReader ──► config.properties / -D sistem property
```

Bu akıştaki en önemli mimari kararlar:

1. **Klasik Page Object Model (POM) yerine JSON tabanlı locator yönetimi.** Element locator'ları Java sınıfları yerine JSON dosyalarında tanımlanır ve `LocatorRepository` üzerinden isimle sorgulanır.
2. **Dependency Injection ile katmanlar arası gevşek bağlılık (loose coupling).** `cucumber-picocontainer` sayesinde step definition sınıfları, ihtiyaç duydukları `Repository`/`Actions` nesnelerini kendileri oluşturmaz — constructor üzerinden alırlar; aynı senaryo içinde aynı tipten tek bir örnek paylaşılır.
3. **Kod (`src/main/java`) ile test tüketicisi (`src/test/java`) ayrımı.** `actions`, `drivers`, `repositories`, `utils` paketleri — Selenium/Cucumber'a *nasıl* tıklanacağını, bekleneceği, veri okunacağını bilen; hangi senaryonun çalıştığından habersiz, jenerik bir kütüphane olarak `src/main/java` altında tutulur. `hooks`, `runners`, `stepdefinitions` ise bu kütüphaneyi kullanarak Gherkin senaryolarını gerçek Selenium çağrılarına bağlayan, projeye özgü "tüketici" kod olarak `src/test/java` altında tutulur.
4. **Merkezi konfigürasyon.** Tarayıcı, headless modu ve timeout gibi ayarlar tek bir `ConfigReader` üzerinden, öncelik sırasına göre (sistem property → `config.properties` → kod içi varsayılan) çözülür.

---

## Kullanılan Teknolojiler

| Teknoloji | Versiyon | Amaç |
|---|---|---|
| Java | 25 | Ana geliştirme dili |
| Maven | - | Bağımlılık ve derleme yönetimi |
| Selenium WebDriver | 4.45.0 | Tarayıcı otomasyonu |
| Cucumber (java, testng, picocontainer) | 7.34.4 | BDD senaryo motoru + Dependency Injection |
| TestNG | 7.10.2 | Test çalıştırma, suite yönetimi, paralelleştirme |
| Jackson Databind | 2.17.2 | JSON dosyalarının okunması/parse edilmesi |
| SLF4J API | 2.0.16 | Loglama arayüzü (soyutlama katmanı) |
| Logback Classic | 1.5.13 | SLF4J için gerçek loglama implementasyonu |
| Allure (cucumber7-jvm, maven plugin) | 2.29.0 / 2.12.0 | Test raporlama |
| GitHub Actions | - | CI/CD |

> **Not:** Selenium 4.45.0, tarayıcı sürücü (driver binary) yönetimini dahili **Selenium Manager** ile otomatik olarak gerçekleştirir; ayrı bir WebDriverManager bağımlılığına ihtiyaç duyulmaz.
>
> **Not:** `slf4j-api`, Selenium'un transitive olarak getirdiği eski sürümle (1.7.x) çakışmaması için pom.xml'de **doğrudan (explicit)** ve Logback ile uyumlu bir sürümde (2.0.16) sabitlenmiştir.

---

## Proje Yapısı

```
BDDWebAutomationFramework/
├── .github/
│   └── workflows/
│       ├── ci.yml                    # Push/PR + manuel tetiklenen ana pipeline
│       └── manual-rerun.yml          # Sadece manuel: geçmiş bir koşumun rerun.txt'i ile yeniden çalıştırma
├── src/
│   ├── main/
│   │   ├── java/com/dgcnrsln/automation/       ← Framework Kütüphanesi (tekrar kullanılabilir)
│   │   │   ├── actions/
│   │   │   │   ├── AlertActions.java
│   │   │   │   ├── BrowserActions.java
│   │   │   │   ├── ElementActions.java
│   │   │   │   ├── NavigationActions.java
│   │   │   │   ├── VerificationActions.java
│   │   │   │   └── WaitActions.java
│   │   │   ├── drivers/
│   │   │   │   ├── DriverFactory.java
│   │   │   │   └── DriverManager.java
│   │   │   ├── repositories/
│   │   │   │   ├── DataRepository.java
│   │   │   │   ├── EnvironmentRepository.java
│   │   │   │   └── LocatorRepository.java
│   │   │   └── utils/
│   │   │       ├── ConfigReader.java
│   │   │       ├── JsonDataReader.java
│   │   │       └── ResourcePaths.java
│   │   └── resources/
│   │       └── logback.xml
│   └── test/
│       ├── java/com/dgcnrsln/automation/        ← Test Tüketicisi (bu kütüphaneyi kullanan testler)
│       │   ├── hooks/
│       │   │   └── Hooks.java
│       │   ├── runners/
│       │   │   ├── TestRunner.java             # Tüm @run etiketli senaryolar + rerun.txt üretir
│       │   │   └── RerunRunner.java            # Sadece rerun.txt'teki başarısız senaryolar
│       │   └── stepdefinitions/
│       │       ├── ActionSteps.java
│       │       ├── AlertSteps.java
│       │       ├── BrowserSteps.java
│       │       ├── NavigationSteps.java
│       │       ├── VerificationSteps.java
│       │       └── WaitSteps.java
│       └── resources/
│           ├── environments/
│           │   └── saucedemo.json
│           ├── features/
│           │   ├── inventory.feature
│           │   ├── login.feature
│           │   └── product-detail.feature
│           ├── locators/
│           │   ├── inventory.json
│           │   ├── login.json
│           │   └── product-detail.json
│           ├── testdata/
│           │   ├── login.json
│           │   └── product-detail.json
│           ├── allure.properties               # Allure sonuçlarını target/ altına yönlendirir
│           ├── config.properties                # browser / headless / timeout varsayılanları
│           ├── testng-single.xml
│           ├── testng-parallel.xml
│           └── testng-rerun.xml
├── pom.xml
└── README.md
```

---

## Tasarım Desenleri ve Prensipler

- **Repository Pattern:** `LocatorRepository`, `DataRepository`, `EnvironmentRepository` sınıfları, ham JSON verisine erişimi soyutlar; step definition katmanı veri kaynağının detaylarından (dosya yolu, parse mantığı) habersizdir.
- **Factory Pattern:** `DriverFactory`, tarayıcı türüne ve headless ayarına göre uygun `WebDriver` implementasyonunu üretir; yeni bir tarayıcı desteği eklemek tek bir yerde değişiklik yapmayı gerektirir.
- **Dependency Injection (Constructor Injection):** Tüm step definition sınıfları ve `ElementActions`/`VerificationActions` gibi Actions sınıfları, bağımlılıklarını constructor üzerinden alır. `cucumber-picocontainer`, bu bağımlılıkları reflection ile otomatik çözer; ek bir "wiring" kodu yazılmasına gerek yoktur.
- **Thread-Safe Erişim:** `DriverManager`, statik `ThreadLocal` alan üzerinden her thread'e özel tek bir `WebDriver` örneği sağlar; paralel testler birbirinin tarayıcı oturumuna müdahale etmez.
- **Separation of Concerns:** Actions katmanı *nasıl* yapılacağını, Step Definitions katmanı *ne zaman/hangi sırayla* yapılacağını, Repository katmanı *nereden* veri alınacağını bilir.
- **Data-Driven Testing:** Test verileri kaynak koddan ayrıştırılmıştır; aynı senaryo farklı veri setleriyle kod değişikliği yapılmadan yeniden kullanılabilir.
- **Explicit Wait / Senkronizasyon Stratejisi:** `WaitActions`, her çağrıda taze bir `WebDriverWait` üretir; tüm element etkileşimlerinden önce `visibilityOfElementLocated` veya `elementToBeClickable` koşulları beklenir.
- **Fail-Fast, Bağlamsal Hatalar:** `ElementActions` ve `VerificationActions`, Selenium'un ham exception'larını yakalayıp hangi locator üzerinde başarısız olduğunu belirten anlamlı mesajlarla yeniden fırlatır.
- **Konfigürasyon Önceliklendirmesi:** `ConfigReader`, `-D` sistem property'sini her zaman `config.properties`'ten önceliklendirir; bu sayede aynı ayar hem lokal geliştirme (dosya üzerinden) hem CI (komut satırından) için farklı şekilde yönetilebilir.

---

## Kurulum

### Ön Gereksinimler

- **JDK 25**
- **Apache Maven**
- Test edilecek tarayıcılardan en az biri yüklü olmalıdır: **Google Chrome**, **Mozilla Firefox** veya **Microsoft Edge**

> Tarayıcı sürücüleri Selenium 4.45.0'ın dahili **Selenium Manager** özelliği sayesinde otomatik olarak indirilip yönetilir; manuel kurulum gerekmez.

### Projeyi Klonlama ve Bağımlılıkları Yükleme

```bash
git clone <repository-url>
cd BDDWebAutomationFramework
mvn clean install -DskipTests
```

---

## Testleri Çalıştırma

### 1. Tekli (Sıralı) Çalıştırma

```bash
mvn clean test "-DsuiteXmlFile=src/test/resources/testng-single.xml"
```

### 2. Paralel Çalıştırma

Senaryoları method bazlı, 3 paralel thread ile çalıştırır (`testng-parallel.xml` içinde `thread-count="3"`).

```bash
mvn clean test "-DsuiteXmlFile=src/test/resources/testng-parallel.xml"
```

### 3. Sadece Başarısız Senaryoları Yeniden Çalıştırma (Rerun)

Bir önceki koşumda başarısız olan senaryolar `target/rerun.txt`'e yazılır (`TestRunner`'ın `rerun:target/rerun.txt` plugin'i sayesinde). Bu dosyayı okuyup **sadece** o senaryoları çalıştırmak için:

```bash
mvn test "-DsuiteXmlFile=src/test/resources/testng-rerun.xml"
```

> `clean` **kullanma** — `target/rerun.txt` dosyası `clean` ile silinir.

### Headless Çalıştırma

```bash
mvn clean test "-DsuiteXmlFile=src/test/resources/testng-single.xml" "-Dheadless=true"
```

CI ortamlarında pencere açmadan çalıştırmak için kullanılır; kalıcı olarak açmak istersen `config.properties`'te `headless=true` yazman yeterli.

### Tarayıcı Seçimi

Tarayıcı bilgisi şu öncelik sırasına göre çözülür (`Hooks.resolveBrowser()`):

1. **JVM sistem property'si** — her zaman önceliklidir: `-Dbrowser=firefox`
2. **TestNG suite XML dosyasındaki parametre** (geriye dönük uyumluluk için): `<parameter name="browser" value="chrome"/>`
3. **`config.properties`** dosyasındaki `browser` değeri
4. **Varsayılan:** `chrome`

Desteklenen değerler: `chrome`, `firefox`, `edge`.

### Etiket (Tag) Bazlı Çalıştırma

`TestRunner`, varsayılan olarak `@run` etiketine sahip senaryoları çalıştırır. Belirli bir senaryo grubunu dahil etmek/hariç tutmak için `.feature` dosyalarındaki etiketleri veya `TestRunner`'daki `tags` ifadesini güncelleyebilirsiniz.

---

## Yeni Senaryo / Element / Veri Ekleme Rehberi

1. **Yeni bir element eklemek için:** İlgili sayfanın locator JSON dosyasına (`src/test/resources/locators/`) yeni bir kayıt ekleyin:
   ```json
   { "elementName": "Menu Button", "locatorType": "id", "locatorValue": "react-burger-menu-btn" }
   ```
   Desteklenen `locatorType` değerleri: `xpath`, `id`, `css` / `cssSelector`, `name`, `class` / `className`.

2. **Yeni bir test verisi eklemek için:** İlgili JSON dosyasına (`src/test/resources/testdata/`) yeni bir `key`-`value` çifti ekleyin.

3. **Yeni bir ortam/URL eklemek için:** `src/test/resources/environments/` altındaki JSON dosyasına yeni bir kayıt ekleyin.

4. **Yeni bir senaryo eklemek için:** İlgili `.feature` dosyasına, mevcut generic step'leri kullanarak yeni bir `Scenario` yazın.

5. **Mevcut generic step'lerin karşılamadığı yeni bir davranış gerekiyorsa:** İlgili `actions` sınıfına yeni bir metot, ardından `stepdefinitions` katmanına bu metodu çağıran yeni bir step tanımı ekleyin. Yeni bir Actions/Repository sınıfı eklerseniz, ilgili step definition sınıfının constructor'ına parametre olarak eklemeniz yeterlidir — PicoContainer bağımlılığı otomatik olarak çözer.

---

## Desteklenen Step (Adım) Listesi

Framework, kategorilere ayrılmış toplam **21 generic Gherkin adımı** ile birlikte gelir.

### 🌐 Navigation (4)
| Step |
|---|
| `Given I navigate to "{string}"` |
| `When I refresh the page` |
| `When I navigate back` |
| `When I navigate forward` |

### 🖱️ Element Actions (6)
| Step |
|---|
| `When I click the "{string}"` |
| `When I click the "{string}" with JavaScript` |
| `When I enter "{string}" into the "{string}"` |
| `When I enter the data "{string}" into the "{string}"` |
| `When I clear the "{string}"` |
| `When I scroll to the "{string}"` |

### ⏳ Wait (1)
| Step |
|---|
| `When I wait for the "{string}" to be visible` |

### ✅ Verification (6)
| Step |
|---|
| `Then the "{string}" should be visible` |
| `Then the "{string}" should not be visible` |
| `Then the "{string}" should contain "{string}"` |
| `Then the "{string}" should contain the data "{string}"` |
| `Then the "{string}" should have value "{string}"` |
| `Then the "{string}" should have value from the data "{string}"` |

### 🌍 Browser (2)
| Step |
|---|
| `When I take a screenshot` |
| `When I press the Enter key` |

### 🚨 Alert (2)
| Step |
|---|
| `When I accept the alert` |
| `When I dismiss the alert` |

### Özet Tablo

| Kategori | Adet |
|---|---|
| 🌐 Navigation | 4 |
| 🖱️ Element Actions | 6 |
| ⏳ Wait | 1 |
| ✅ Verification | 6 |
| 🌍 Browser | 2 |
| 🚨 Alert | 2 |
| **Toplam** | **21** |

---

## Örnek Senaryolar

Framework 3 feature dosyasında toplam **20 senaryo** içerir: `login.feature` (9), `inventory.feature` (6), `product-detail.feature` (5).

### Login — başarılı giriş

```gherkin
Scenario: LG-01 Successful login with valid standard user
    When I enter the data "Standard User" into the "Username Input"
    When I enter the data "Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Products Title" should be visible
```

### Login — kasıtlı başarısız senaryo (hata raporlamayı göstermek için)

```gherkin
Scenario: LG-09 Invalid credentials should not show products title (Fail Scenario)
    When I enter the data "Invalid User" into the "Username Input"
    When I enter the data "Wrong Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Products Title" should be visible
```

Bu senaryo bilerek geçersiz kimlik bilgileriyle giriş yapıp `"Products Title"`'ın görünür olmasını bekler — login başarısız olduğu için element hiç gelmez, timeout sonrası `RuntimeException` fırlar ve `Hooks.attachScreenshotOnFailure` otomatik devreye girip ekran görüntüsünü Allure raporuna gömer.

> **Not:** Bu senaryo, framework'ün hata yakalama/raporlama/otomatik screenshot mekanizmasını göstermek amacıyla **bilerek** her zaman başarısız olacak şekilde kurgulanmıştır — bir bug değildir. Bu yüzden `@excluded-from-ci` etiketiyle işaretlenmiştir ve `TestRunner`'ın `tags = "@run and not @excluded-from-ci"` filtresi sayesinde CI koşumlarına dahil edilmez; CI her zaman yeşil kalır. Senaryoyu bilerek çalıştırmak istersen (örn. rapor mekanizmasını göstermek için) `.feature` dosyasından `@excluded-from-ci` etiketini geçici olarak kaldırman yeterli.

### Inventory — sepete ekleme

```gherkin
Scenario: INV-01 Cart badge shows count after adding product
    When I click the "Add To Cart"
    Then the "Cart Badge" should contain "1"
```

### Product Detail — veri odaklı doğrulama

```gherkin
Scenario: PD-01 Product detail page displays correct name and price
    Then the "Product Detail Name" should contain "Sauce Labs Backpack"
    Then the "Product Detail Price" should contain the data "Backpack Price"
```

`"Backpack Price"` değeri koddan değil, `testdata/product-detail.json` dosyasından (`"key": "Backpack Price", "value": "$29.99"`) okunur.

### JSON Şema Örnekleri

**Locator dosyası** (`locators/login.json`):
```json
{
  "elements": [
    { "elementName": "Username Input", "locatorType": "id", "locatorValue": "user-name" },
    { "elementName": "Login Button", "locatorType": "id", "locatorValue": "login-button" }
  ]
}
```

**Test verisi dosyası** (`testdata/login.json`):
```json
{
  "testData": [
    { "key": "Standard User", "value": "standard_user" },
    { "key": "Password", "value": "secret_sauce" }
  ]
}
```

**Ortam dosyası** (`environments/saucedemo.json`):
```json
{
  "testData": [
    { "key": "Sauce Demo", "value": "https://www.saucedemo.com/" }
  ]
}
```

`testData` ve `environments` dosyaları aynı şemayı (`key`/`value`) paylaşır — ikisi de `JsonDataReader.getTestData()` üzerinden okunur, sadece klasörleri (dolayısıyla amaçları) farklıdır.

---

## Hata Yönetimi ve Otomatik Screenshot

### Bağlamsal Hata Mesajları

`ElementActions` ve `VerificationActions`, Selenium'un fırlattığı `WebDriverException` alt sınıflarını yakalar ve hangi locator üzerinde başarısız olunduğunu belirten bir mesajla yeniden fırlatır:

```
java.lang.RuntimeException: Element is not visible. Locator: By.cssSelector: .title
Caused by: org.openqa.selenium.TimeoutException: ...
```

Orijinal Selenium exception'ı `cause` olarak korunur; stack trace'de teknik detay kaybolmaz.

### Otomatik Screenshot (Başarısız Adımlarda)

`Hooks` sınıfındaki `@AfterStep` hook'u, her adımdan sonra çalışır ve yalnızca `scenario.isFailed()` `true` ise ekran görüntüsü alıp doğrudan Allure raporuna gömer:

```java
@AfterStep
public void attachScreenshotOnFailure(Scenario scenario) {
    if (scenario.isFailed()) {
        byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                .getScreenshotAs(OutputType.BYTES);
        scenario.attach(screenshot, "image/png", "Failure Screenshot");
    }
}
```

> Bu mekanizma, `BrowserActions.takeScreenshot()` (manuel olarak `When I take a screenshot` adımıyla tetiklenen, `Reports/Screenshots/` klasörüne `.png` dosyası kaydeden) metodundan **bağımsızdır**. Biri elle tetiklenen "istediğim anda dosyaya kaydet", diğeri "hata olursa otomatik rapora göm" — farklı kullanım senaryolarına hizmet eder.

---

## Loglama

SLF4J API + Logback Classic (`src/main/resources/logback.xml`):

- **Konsol ve dosya çıktısı** aynı anda üretilir; dosya çıktısı `target/logs/test-execution.log` altına yazılır (her koşumda sıfırdan).
- **Log formatı**, her satıra thread adı ve milisaniye hassasiyetinde timestamp ekler:
  ```
  14:22:45.123 [TestNG-PoolService-0] INFO  c.d.a.drivers.DriverFactory - Creating WebDriver instance for browser: chrome
  ```
- **Gürültü azaltma:** `org.openqa.selenium` ve `io.cucumber` paketleri `WARN` seviyesinde, sadece `com.dgcnrsln.automation` paketi `INFO` seviyesinde loglanır. Selenium DevTools'un CDP versiyon uyarıları (`java.util.logging` üzerinden gelir) `DriverFactory` içindeki bir static blokla ayrıca susturulmuştur.
- **Konsol formatter'ı:** Cucumber plugin listesi `pretty` yerine `summary` kullanır — paralel koşumda satırların karışması engellenir; detaylı adım çıktısı için Allure raporuna bakılmalıdır.

---

## Raporlama (Allure)

Cucumber senaryoları doğrudan Allure formatında raporlanır (`io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm` plugin'i).

```bash
mvn clean test "-DsuiteXmlFile=src/test/resources/testng-single.xml"
mvn allure:serve
```

- Sonuçlar `target/allure-results` altına yazılır (`allure.properties` ile yapılandırılmıştır) — `mvn clean` her koşumda bu klasörü de temizler, eski/stale sonuçlar birikmez.
- `mvn allure:serve` yerelde interaktif bir rapor sunucusu açar; CI'da `mvn allure:report` statik bir HTML raporu üretir (`target/site/allure-maven-plugin`).
- Başarısız adımların ekran görüntüleri, senaryo detayında **Test body** altında otomatik olarak görünür.
- Rerun sonrası ikinci bir koşum çalıştırılırsa, Allure'ın **Retries** sekmesinde aynı senaryonun önceki denemesi de görülebilir.

> **Önemli:** Rapor almadan önce her zaman `clean` ile başlayın (`mvn clean test ...`). Sadece `mvn test` (clean'siz) çalıştırırsanız, `target/allure-results` bir önceki koşumun sonuçlarını silmeden üzerine ekler ve aynı senaryolar raporda birden fazla kez görünür.

---

## CI/CD (GitHub Actions)

### `.github/workflows/ci.yml` — Ana pipeline

**Tetikleyiciler:** `push` (main), `pull_request` (main), ve **manuel** (`workflow_dispatch` — GitHub Actions sekmesinden "Run workflow").

**Adımlar:**
1. Checkout + JDK 25 kurulumu + Maven cache
2. `mvn clean test -Dheadless=true` ile testng-single.xml koşumu
3. `target/rerun.txt` doluysa **otomatik rerun** (`testng-rerun.xml`)
4. `mvn allure:report` ile statik Allure raporu üretimi
5. `allure-results`, `allure-report` ve `rerun.txt` artifact olarak yüklenir

### `.github/workflows/manual-rerun.yml` — Manuel rerun

Sadece **manuel** tetiklenir, bir `run_id` girişi ister (hangi geçmiş CI koşumunun `rerun.txt`'ini kullanacağını belirtir). O koşumun artifact'inden `rerun.txt`'i indirir, sadece o başarısız senaryoları çalıştırır, yeni bir Allure raporu üretir.

**Kullanım:** GitHub → Actions → "Manual Rerun" → "Run workflow" → önceki bir CI koşumunun ID'sini gir (URL'de görünür: `.../actions/runs/<run_id>`).

**Allure Raporunun GitHub Pages'e Yayınlanması:** `ci.yml`'deki `deploy-pages` job'ı, `mvn allure:report` ile üretilen statik raporu her `main` branch push'ından sonra otomatik olarak GitHub Pages'e deploy eder. Güncel rapora şu adresten ulaşılabilir: `https://dgcnrsln.github.io/bdd-web-automation-framework/`

---

## Gelecek Geliştirmeler

- **Flaky Test Toleransı:** TestNG `IRetryAnalyzer` ile geçici/kararsız başarısızlıklar için otomatik yeniden deneme mekanizması eklenebilir.
- **Kapsam Genişletmesi:** Sauce Demo üzerindeki ek modüller (checkout, sıralama, sepet sayfası vb.) için yeni `.feature` dosyaları ve ilgili JSON kaynakları eklenebilir.
- **Çoklu Tarayıcı Matrix'i:** GitHub Actions'ta `strategy.matrix` ile aynı testlerin Chrome/Firefox/Edge üzerinde paralel çalıştırılması.
