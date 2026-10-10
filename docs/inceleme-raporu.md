# Değerlendirme Raporu: BDD Web Automation Framework

Bu rapor 2026-10-10 tarihinde, `c1c5671` commit'indeki kod üzerinde yapılan incelemeyi ve ardından uygulanan düzeltmeleri içerir.

- **İnceleme (bölüm 1-10):** İzlenen 47 dosyanın tamamı okundu; bulgular statik incelemeye dayanıyor ve o aşamada hiçbir dosya değiştirilmedi. Bu bölümlerdeki dosya ve satır numaraları inceleme anındaki koda göredir.
- **Doğrulama (bölüm 11):** Belirsiz kalan noktalar sonradan Maven komutlarıyla ve testler çalıştırılarak kontrol edildi.
- **Düzeltmeler (bölüm 12-14):** Bulguların bir kısmı `docs/inceleme-raporu` dalında ayrı commit'lerle düzeltildi. Düzeltilen maddeler yerinde commit koduyla işaretli; işaretsiz maddeler hâlâ açık. Son durumda `mvn clean test -Dheadless=true` 19 senaryonun 19'unu geçiriyor. Hiçbir şey push edilmedi.

## 1. Özet

**Ne yapıyor:** Sauce Demo sitesini test eden bir Selenium + Cucumber + TestNG çatısı. Senaryolar 21 generic Gherkin adımıyla yazılıyor; locator, test verisi ve URL'ler JSON dosyalarından isimle çözülüyor. 3 feature dosyasında 20 senaryo var, 19'u CI'da koşuyor.

**Teknolojiler:** Java 25, Maven, Selenium 4.45.0, Cucumber 7.34.4 (java, testng, picocontainer), TestNG 7.10.2, Jackson 2.17.2, SLF4J + Logback, Allure 2.29.0, GitHub Actions (test, otomatik rerun, Pages'e rapor).

**Mimari:**

```
.feature → stepdefinitions (PicoContainer DI) → actions → DriverManager (ThreadLocal) → DriverFactory
                     └→ repositories → JsonDataReader (statik cache) → JSON dosyaları
```

- `src/main/java`: `actions`, `drivers`, `repositories`, `utils` (yeniden kullanılabilir "kütüphane").
- `src/test/java`: `hooks`, `runners`, `stepdefinitions`.
- `src/test/resources`: `features`, `locators`, `testdata`, `environments`, üç TestNG suite dosyası.

Katmanlar temiz ve kod küçük. Asıl sorunlar paralel koşum ayarında, CI'ın hata maskelemesinde ve README'nin kodla uyuşmayan iddialarında.

## 2. Öncelik özeti

| # | Öncelik | Bulgu | Konum |
|---|---|---|---|
| 1 | Kritik — Düzeltildi (`921b6e1`) | "Tekli" suite sıralı değil; iki suite de 10 thread ile koşuyor | `TestRunner.java:23`, `testng-*.xml` |
| 2 | Kritik — Düzeltildi (`5fc7999`) | CI, derleme/altyapı hatasında yeşil kalabiliyor | `ci.yml:32-57` |
| 3 | Orta — Düzeltildi (`dabb0f8`) | Aynı isimli locator/veri sessizce üzerine yazılıyor | `JsonDataReader.java:39,58` |
| 4 | Orta — Düzeltildi (`74389b7`) | `config.properties` içindeki `browser` hiç kullanılmıyor; IDE'den koşumda NPE | `Hooks.java:49-65` |
| 5 | Orta — Düzeltildi (`d738eef`) | "should have value" aslında "contains"; null değerde NPE | `VerificationActions.java:46-62` |
| 6 | Orta — Düzeltildi (`e1ca570`) | Doğrulama hataları `RuntimeException`; Allure'da "broken" görünür | `VerificationActions.java:18,34` |
| 7 | Orta — Düzeltildi (`da8ad45`, `dcb4186`) | Türkçe locale'de `toLowerCase()` hatası | `DriverFactory.java:34`, `JsonDataReader.java:89` |
| 8 | Orta — Ertelendi (bilinçli karar) | 21 adımın 15'i hiçbir senaryoda kullanılmıyor | `features/*.feature` |
| 9 | Orta — Ertelendi (bilinçli karar) | `src/main` kodu `src/test/resources` yoluna sabitlenmiş | `ResourcePaths.java:7-9` |
| 10 | Orta — Ertelendi (bilinçli karar) | Ortam değiştirme mekanizması yok | `saucedemo.json:3-4` |

## 3. Hatalar ve hataya açık kod

**[Kritik — DÜZELTİLDİ] Suite'ler iddia edilen thread sayısıyla koşmuyor.**
- **Konum:** `TestRunner.java:22-26`, `RerunRunner.java:20-24`, `testng-single.xml:4`, `testng-parallel.xml:5-6`.
- **Sorun:** Runner'lar `@DataProvider(parallel = true)` kullanıyor. Bu havuzun boyutunu `thread-count` değil `data-provider-thread-count` belirler ve varsayılanı 10'dur. Yani `testng-single.xml` de paralel koşar, `testng-parallel.xml` içindeki `thread-count="3"` ise senaryo sayısını etkilemez. CI, 4 çekirdekli runner'da aynı anda 10 Chrome açıp herkese açık bir siteye yüklenir; bu doğrudan flaky test kaynağıdır.
- **Öneri:** `testng-single.xml`'e `data-provider-thread-count="1"`, `testng-parallel.xml`'e `data-provider-thread-count="3"` ekleyin. Alternatif olarak surefire'da `dataproviderthreadcount` özelliğini `-Dthreads` ile parametrik yapın.
- **Düzeltme (2026-10-10):** Commit `921b6e1` (`fix: set data-provider-thread-count in TestNG suites`), `docs/inceleme-raporu` dalında.
  - **Değişiklik:** `testng-single.xml`'e `data-provider-thread-count="1"`, `testng-parallel.xml`'e `data-provider-thread-count="3"`, `testng-rerun.xml`'e `data-provider-thread-count="1"` eklendi. Java koduna dokunulmadı.
  - **Doğrulama (headless Chrome):** Tekli suite 19/19 geçti, 28,9 sn, log'da 1 thread. Paralel suite 19/19 geçti, 12,9 sn, log'da 3 thread.
  - **Doğrulanmayan:** Rerun suite koşmadı (başarısız senaryo yoktu). Değişiklik öncesindeki 10 thread ölçülmedi; değer TestNG'nin varsayılanından biliniyor.

**[Orta — DÜZELTİLDİ, `dabb0f8`] Aynı isimli kayıtlar sessizce eziliyor.**
- **Konum:** `JsonDataReader.java:39,58`; somut örnek `inventory.json:4` ve `product-detail.json:10` ("Cart Badge").
- **Sorun:** Tüm dosyalar tek düz bir map'e yükleniyor ve son okunan kazanıyor. `File.listFiles` sırası garanti değil. Bugün iki değer aynı; biri değişirse hangi locator'ın kullanılacağı belirsiz olur.
- **Öneri:** Yüklemede çakışmayı tespit edip iki dosya adını içeren bir hata fırlatın ya da isimleri sayfa bazında ayırın (`Inventory.Cart Badge`).

**[Orta — DÜZELTİLDİ, `74389b7`] Tarayıcı çözümleme sırası `config.properties`'i ölü bırakıyor.**
- **Konum:** `Hooks.java:49-65`, `testng-*.xml` içindeki `<parameter name="browser" value="chrome"/>`.
- **Sorun:** Üç suite dosyası da `browser=chrome` verdiği için XML parametresi her zaman kazanır; `config.properties:1`'i `firefox` yapmak hiçbir şeyi değiştirmez. Ayrıca `Reporter.getCurrentTestResult()` TestNG dışı koşumda (ör. IntelliJ'de feature dosyasından) `null` döner ve `Hooks.java:55` NPE atar.
- **Öneri:** XML'lerden `browser` parametresini kaldırın ve yalnızca `ConfigReader.get("browser", "chrome")` kullanın. Bu, 50-53. satırlardaki tekrarlanan system property kontrolünü de siler.

**[Orta — DÜZELTİLDİ, `d738eef`] Değer doğrulaması gevşek ve null'a açık.**
- **Konum:** `VerificationActions.java:46-62`, `VerificationSteps.java:51,59`.
- **Sorun:** Adım "should have value" diyor ama `contains` ile kontrol ediyor: beklenen `"a"`, gerçek `"abc"` iken geçer; beklenen `""` her zaman geçer. `getAttribute("value")` `null` dönerse 55. satır NPE atar. Hata mesajı da "Actual text" diyor (57. satır, kopyala-yapıştır).
- **Öneri:** `assertEquals` kullanın ya da adımı "should contain value" olarak yeniden adlandırın; `getDomProperty("value")`'ya geçip null kontrolü ekleyin.

**[Orta — DÜZELTİLDİ, `da8ad45` ve `dcb4186`] Türkçe locale hatası.**
- **Konum:** `DriverFactory.java:34`, `JsonDataReader.java:68,89`.
- **Sorun:** Argümansız `toLowerCase()` JVM locale'ini kullanır. `tr-TR` makinede `"ID"` → `"ıd"`, `"FIREFOX"` → `"fırefox"` olur ve "Unsupported ..." hatası alınır. CI (İngilizce locale) geçerken yerel makinede kırılır.
- **Öneri:** `toLowerCase(Locale.ROOT)`.
- **Kısmi düzeltme (2026-10-10):** Commit `da8ad45`. Tarayıcı adı artık `BrowserType.fromString` içinde `Locale.ROOT` ile çözülüyor; `DriverFactory` tarafı kapandı. `JsonDataReader.java`'daki iki `toLowerCase()` çağrısı (locator tipi ve `.json` uzantısı) commit `dcb4186` ile `Locale.ROOT`'a çevrildi; madde kapandı.

**[Orta — DÜZELTİLDİ, `e1ca570`] `verifyElementInvisible` diğerleriyle tutarsız.**
- **Konum:** `VerificationActions.java:22-26`.
- **Sorun:** `waitActions.invisible` başarısızlıkta `false` dönmez, `TimeoutException` fırlatır. Bu yüzden 25. satırdaki assert mesajı hiç görünmez ve locator bağlamı kaybolur.
- **Öneri:** `TimeoutException`'ı yakalayıp locator içeren bir `AssertionError` fırlatın.

**[Düşük — DÜZELTİLDİ, `46f530e`, `0eb3693` ve `768743a`] Hook'lar kendi hatalarına karşı korumasız.**
- **Konum:** `Hooks.java:34-35`, `DriverManager.java:19-23`.
- **Sorun:** Açık bir alert ya da ölmüş oturum varken `getScreenshotAs` hata atar ve asıl hatanın üstüne ikinci bir hata biner. `quit()` hata atarsa `driver.remove()` çalışmaz.
- **Öneri:** Screenshot'ı `try/catch` içine alıp sadece loglayın; `quitDriver`'da `try { quit } finally { remove }` kullanın.
- **Kısmi düzeltme (2026-10-10):** `quitDriver` tarafı commit `0eb3693` ile kapandı. Commit `46f530e` ile `@AfterStep` driver hiç yoksa screenshot'ı atlayıp uyarı logluyor. Açık bir alert ya da ölmüş oturum varken `getScreenshotAs`'in hata atması durumu commit `768743a` ile kapandı: çağrı `try/catch` içinde, hata yalnızca uyarı olarak loglanıyor.

**[Düşük] Diğer hataya açık noktalar.**
- `AlertActions.java:8,13`: Alert beklenmeden `switchTo().alert()` çağrılıyor. `ExpectedConditions.alertIsPresent()` ile bekleyin.
- `BrowserActions.java:22`: Dosya adı yalnızca `currentTimeMillis`; paralel koşumda çakışırsa `Files.copy` hata atar. Thread adı ya da UUID ekleyin.
- `JsonDataReader.java:35-37,55-56`: Eksik alan çıplak NPE verir, hangi dosyada olduğu bilinmez. Dosya adını içeren bir hata fırlatın.
- `JsonDataReader.java:70-72`: Mesaj "not found or empty" diyor ama boş klasör hata vermez, boş map döner.
- `WaitActions.java:13`: `-Dtimeout=abc` verilirse statik init patlar ve anlaşılmaz bir `NoClassDefFoundError` çıkar.

## 4. Güvenlik sorunları

Kritik bir bulgu yok; gerçek gizli bilgi bulunmuyor.

- **[Orta — Düzeltildi, `edf8198`] Workflow'larda `permissions` bloğu yok** (`ci.yml`, `manual-rerun.yml`). `test` job'ı deponun varsayılan token yetkisiyle koşar. Üst seviyeye `permissions: contents: read` ekleyin; `manual-rerun.yml` için ayrıca `actions: read` gerekir.
- **[Düşük] Action'lar tag ile sabitlenmiş** (`@v4`, `@v3`), commit SHA ile değil. SHA'ya sabitleyip Dependabot ile güncelleyin.
- **[Düşük] Kimlik bilgisi düz metin** (`testdata/login.json:6`). `secret_sauce` Sauce Demo'nun herkese açık parolası, yani sızıntı değil. Ancak çatı gerçek bir uygulamaya uyarlanırsa gizli veri için bir yol yok. `DataRepository`'ye `${ENV:NAME}` gibi ortam değişkeni çözümlemesi ekleyin.
- **[Düşük] Allure raporu herkese açık Pages'e gidiyor** (`ci.yml:81-103`). `I enter "{string}"` adımıyla yazılan literal değerler ve ekran görüntüleri rapora girer. Demo için sorun değil, gerçek projede dikkat edin.

## 5. Mimari ve kod organizasyonu

- **[Orta — Ertelendi, bkz. bölüm 15] "Kütüphane" test kaynaklarına bağımlı.** `ResourcePaths.java:7-9` `src/test/resources/...` yolunu çalışma dizinine göreli veriyor. `src/main` jar olarak paketlenip başka projede kullanılamaz ve Maven proje kökü dışından çalıştırılırsa bozulur. Classpath üzerinden okuyun ya da kökü `ConfigReader` ile yapılandırılabilir yapın.
- **[Orta — Ertelendi, bkz. bölüm 15] Ortam yönetimi gerçek anlamda yok.** `saucedemo.json:3-4` düz bir anahtar→tam URL listesi; envanter URL'i ayrıca elle yazılmış. Staging/prod arasında geçiş yapılamaz. `-Denv=...` ile seçilen bir `baseUrl` ve göreli path'ler kullanın.
- **[Orta] `JsonDataReader` iki iş yapıyor.** Her örnek aynı klasör için hem locator hem veri map'i yüklüyor (`JsonDataReader.java:23-24`); `EnvironmentRepository.java:14` de URL almak için `getTestData` çağırıyor ve ortam dosyasının kök anahtarı `testData`. Locator ve anahtar-değer okuyucularını ayırın.
- **[Düşük] DI ile statik erişim karışık.** Actions sınıfları `DriverManager.getDriver()`'ı statik çağırıyor. PicoContainer zaten senaryo başına örnek ürettiği için sürücüyü enjekte edilen bir sınıfta tutmak `ThreadLocal`'ı gereksiz kılar.
- **[Düşük — Giderildi, `00c2e5b`] `VerificationActions` `src/main` içinde TestNG `Assert` kullanıyor**; bu yüzden TestNG compile scope'ta kalmak zorunda. Ekran görüntüsü değişikliğiyle (bölüm 14) `Assert` çağrıları kalktı; `src/main` artık TestNG kullanmıyor. `pom.xml`'de TestNG'nin scope'u değiştirilmedi.
- **[Düşük] Tag filtresi runner'a gömülü** (`TestRunner.java:14`). `@excluded-from-ci` adına rağmen yerelde de dışlıyor. Filtreyi CI'da `-Dcucumber.filter.tags` ile verin.

## 6. Kod tekrarı ve okunabilirlik

- **[Düşük] Login adımları iki Background'da tekrarlanıyor** (`inventory.feature:5-8`, `product-detail.feature:5-8`). `Given I am logged in as "Standard User"` gibi bileşik bir adım yazın.
- **[Düşük] Aynı try/catch sarmalayıcısı** `ElementActions.java`'da 5, `VerificationActions.java`'da 3 kez tekrar ediyor. Tek bir yardımcı metoda ve özel bir exception tipine çekin.
- **[Düşük] Chrome ve Edge headless ayarları aynı** (`DriverFactory.java:61-65`, `83-87`). İkisi de `ChromiumOptions`'tan türediği için ortak metoda alın. `--window-position=-32000,-32000` headless'ta gereksiz; Firefox'ta pencere boyutu hiç ayarlanmıyor.
- **[Düşük] İsimlendirme:** `JsonDataReader.java:109`'da parametre `elementName` (aslında veri anahtarı); `ActionSteps.java:45`'te `text` (aslında anahtar); `NavigationActions.java:6-7`'de boş constructor.
- **[Düşük] Gherkin stili:** Ardışık `When ... When ...` yerine `And` kullanın. "Products Title" (`.title`) `login.json:7`'de duruyor ama envanter sayfasına ait.

## 7. Performans

- **[Kritik — Düzeltildi]** Kontrolsüz 10 thread (bölüm 3'ün ilk maddesi) aynı zamanda en büyük performans ve kararlılık riskiydi. `921b6e1` ile giderildi.
- **[Düşük] Her senaryo yeni tarayıcı açıp UI'dan login oluyor** (`Hooks.java:24`). 19 senaryoda sorun değil; büyürse login'i `session-username` çerezi ile atlayın.
- **[Düşük] Metin doğrulaması tek seferlik okuma yapıyor** (`VerificationActions.java:32`). Geç güncellenen metinlerde flaky olur; `ExpectedConditions.textToBePresentInElementLocated` kullanın.
- **[Düşük] Her klasör iki kez parse ediliyor** (bölüm 5'teki çift yükleme). Etkisi ihmal edilebilir.
- **[Düşük] CI:** `ci.yml:24-29`'daki elle cache adımı yerine `setup-java`'nın `cache: maven` seçeneği yeter; job'larda `timeout-minutes` yok.

## 8. Test eksikleri

- **[Orta — Ertelendi, bkz. bölüm 15] 21 adımın 15'i hiçbir senaryoda kullanılmıyor.** Kullanılanlar: navigate, enter the data, click, should be visible, should contain, should contain the data. Refresh/back/forward, JS click, literal enter, clear, scroll, wait, should not be visible, iki value adımı, screenshot, Enter ve iki alert adımı hiç çalışmamış kod. Bunları örnekleyen senaryolar ekleyin; alert için Sauce Demo yetersiz, başka bir demo sayfası gerekir.
- **[Orta — Düzeltildi, `79f20ef`] Senaryo başlığı ile doğrulama uyuşmuyor.** `inventory.feature:14-17` "Cart badge disappears" diyor ama "Add To Cart" görünürlüğünü kontrol ediyor; `product-detail.feature:23-26` de aynı. `Then the "Cart Badge" should not be visible` ekleyin.
- **[Orta] Çatı kodunun birim testi yok.** `JsonDataReader` (eksik alan, çakışma, bilinmeyen locator tipi) ve `ConfigReader` (öncelik sırası) tarayıcısız test edilebilir.
- **[Düşük] LG-09'un adı ve doğrulaması çelişiyor** (`login.feature:49-54`): "should not show" diyor, "should be visible" bekliyor. Kasıtlı olduğu README'de yazıyor; `@demo-failure` gibi bir etiket ve ayrı feature dosyası daha net olur.
- **[Düşük] LG-01 yalnızca `.title` görünürlüğüne bakıyor**, metnin "Products" olduğunu doğrulamıyor.
- **[Düşük] Firefox ve Edge yolları hiç koşmuyor**; CI yalnızca Chrome kullanıyor.

## 9. Bağımlılıklar ve yapılandırma

**[Kritik — DÜZELTİLDİ] CI, senaryo dışı hatalarda yeşil kalabiliyor.**
- **Konum:** `ci.yml:31-57`.
- **Sorun:** Test adımı `continue-on-error: true`. Derleme hatası olursa `target/rerun.txt` hiç oluşmaz, `has_failures=false` çıkar ve rerun atlanır. `mvn allure:report` lifecycle çalıştırmadığı için derlemeyi denemez. Job yeşil biter ve Pages'teki rapor boş bir raporla ezilebilir. Ayrıca ilk koşumda düşüp rerun'da geçen senaryolar sessizce yeşil sayılıyor.
- **Öneri:** Test adımına `id` verin. Sonda `if: steps.test.outcome == 'failure' && steps.check_rerun.outputs.has_failures != 'true'` koşuluyla `exit 1` yapan bir adım ekleyin. Rerun'da geçen senaryoları job özetine yazdırın.
- **Düzeltme (2026-10-10):** Commit `5fc7999` (`ci: fail the job on non-scenario test failures`), `docs/inceleme-raporu` dalında. Yalnızca `.github/workflows/ci.yml` değişti.
  - **Koruma adımı:** Test adımına `id: run_tests` verildi. Job'ın sonuna eklenen "Fail on non-scenario failure" adımı, test adımı başarısız olup `rerun.txt` boş ya da yoksa `exit 1` yapıyor.
  - **Job özeti:** İlk koşumda düşen senaryolar `GITHUB_STEP_SUMMARY`'ye yazılıyor; rerun'da geçenler artık görünür.
  - **Pages koruması:** Rapor yalnızca `target/allure-results/*-result.json` varsa Pages'e yükleniyor; `deploy-pages` yalnızca yükleme başarılıysa çalışıyor.
  - **Doğrulama (yerel):** YAML ayrıştırıldı. Job'ın kabuk mantığı üç durumda simüle edildi: olmayan suite dosyasıyla senaryo dışı hata (koruma devreye girdi, Pages yüklemesi atlandı), normal koşum (19/19 geçti, koruma devreye girmedi), yalnızca LG-09 ile senaryo hatası (rerun tetiklendi, koruma devreye girmedi, senaryo özete yazıldı).
  - **Doğrulanmayan:** Workflow GitHub Actions üzerinde koşmadı (push yapılmadı). `if:` ifadeleri, `hashFiles` ve job çıktısı (`pages_uploaded`) yalnızca okunarak kontrol edildi; `actionlint` kurulu değildi.
  - **Bilinen sınır:** Koşum, bazı senaryolar düştükten sonra çökerse `rerun.txt` dolu olur ve koruma devreye girmez; rerun geçerse job yeşil kalır.

**[Orta] Yalnızca testte kullanılan bağımlılıklar compile scope'ta** (`pom.xml:38-54,74-78`). `cucumber-java`, `cucumber-testng`, `cucumber-picocontainer` ve `allure-cucumber7-jvm`'e `<scope>test</scope>` ekleyin.

**[Orta] Bağımlılıkların çoğu güncel değil** (`pom.xml:15-21`, `:97`). `mvn versions:display-dependency-updates` ve `display-plugin-updates` çıktısı (2026-10-10):

| Bağımlılık | Mevcut | En yeni | Not |
|---|---|---|---|
| `selenium-java` | 4.45.0 | 4.51.0 | Minor; düşük riskli |
| `testng` | 7.10.2 | 7.12.0 | Minor |
| `jackson-databind` | 2.17.2 | 2.22.3 | Beş minor geride |
| `logback-classic` | 1.5.13 | 1.6.5 | Yeni minor hat; geçiş notlarına bakın |
| `cucumber-*` | 7.34.4 | 8.0.4 | Major; kırıcı değişiklik olabilir |
| `allure-cucumber7-jvm` | 2.29.0 | 3.0.0 | Major; Cucumber 8 ile uyumu ayrıca kontrol edilmeli |
| `slf4j-api` | 2.0.16 | 2.1.0-alpha1 | Alpha; geçmeyin |
| `allure-maven` (eklenti) | 2.12.0 | 3.1.0 | Major |
| `maven-surefire-plugin` | 3.5.4 | 3.6.0 | Minor |

Önce minor güncellemeleri (Selenium, TestNG, Jackson, Surefire) yapın; Cucumber 8 ve Allure 3'ü ayrı bir işte, birlikte ele alın. Bu sürümlerdeki bilinen güvenlik açıklarını (CVE) kontrol etmedim; araç yalnızca yeni sürüm olup olmadığını söylüyor.

**[Orta — Düzeltildi, `22cd55e`] Allure raporu kütüphaneden eski bir CLI ile üretiliyor** (`pom.xml:94-101`). `reportVersion` tanımlı değil; `allure-maven` 2.12.0'ın koduna gömülü varsayılan 2.20.1, sonuçları yazan kütüphane ise 2.29.0. `<reportVersion>${allure.version}</reportVersion>` ekleyin.

**[Düşük] Diğer yapılandırma notları.**
- `pom.xml`: Asgari Maven sürümü tanımlı değil (`display-plugin-updates` bunu hata olarak raporluyor; eklentiler en az 3.6.3 istiyor). `maven-enforcer-plugin` ile `requireMavenVersion` ekleyin.
- `pom.xml:12-13`: `source`/`target` yerine `maven.compiler.release` kullanın.
- Maven Wrapper yok ama `.gitignore:2` onu referans alıyor; `mvnw` ekleyin ya da satırı silin.
- `.idea/misc.xml`, `vcs.xml`, `encodings.xml` commit edilmiş. `Reports/` (`BrowserActions.java:20`) `.gitignore`'da yok ve `mvn clean` ile silinmez; çıktıyı `target/screenshots`'a alın.
- `logback.xml:18`: Selenium SLF4J'ye bağımlı değil (bağımlılık ağacıyla doğrulandı) ve `java.util.logging` kullanıyor; bu satır `jul-to-slf4j` köprüsü olmadan etkisiz. Dosyanın `src/main/resources`'ta olması kütüphaneyi kullananlara log ayarı dayatır.
- `-Dbrowser`, `-Dheadless`, `-Dtimeout` çok genel anahtarlar; `automation.` gibi bir ön ek çakışmayı önler.
- `ci.yml:87-103`: Pages deploy için `concurrency` grubu yok.

## 10. Dokümantasyon

- **[Orta — Giderildi] README koşum modlarını yanlış anlatıyor.** `README.md:215` "Tekli (Sıralı)" ve `:223` "3 paralel thread" diyor; ikisi de kodla uyuşmuyordu (bölüm 3'ün ilk maddesi). Suite'ler `921b6e1` ile düzeltildiği için bu ifadeler artık kodla uyuşuyor; README'de değişiklik gerekmedi.
- **[Orta — Giderildi, `74389b7` ve `b44b395`] Tarayıcı önceliği yanıltıcı.** `README.md:249-254` sırayı doğru veriyor ama `config.properties`'in pratikte hiç devreye girmediğini söylemiyor.
- **[Düşük — Kısmen giderildi, `e1ca570`] Abartılı iddialar:** `README.md:49` "tüm etkileşimler WebDriverWait ile" (alert ve Enter hariç); `:430` "VerificationActions exception'ları sarmalar" (`verifyElementInvisible` hariç). İkinci iddia `e1ca570` ile doğru hale geldi; `README.md:49`'daki ilki (alert ve Enter beklemesiz) hâlâ açık.
- **[Düşük — Giderildi, `b44b395`] SLF4J notunun kaynağı yanlış** (`README.md:105`). Not, eski `slf4j-api` 1.7.x'i Selenium'un getirdiğini söylüyor. `mvn dependency:tree -Dverbose` çıktısına göre 1.7.36'yı TestNG 7.10.2 getiriyor; Selenium'un ağacında SLF4J yok. Sürümü sabitleme kararı doğru, yalnızca gerekçedeki kütüphane adını düzeltin.
- **[Düşük — Giderildi, `b44b395`] Tutarsızlıklar:** `README.md:260` runner'ın `@run` koştuğunu, `:371` ise `@run and not @excluded-from-ci` koştuğunu söylüyor. `:207`'deki `cd BDDWebAutomationFramework`, depo adı `bdd-web-automation-framework` ile uyuşmuyor.
- **[Düşük — Kısmen giderildi] Eksikler:** Kodda hiç Javadoc yok, LICENSE dosyası yok, README yalnızca Türkçe. Commit `45e4d48` ile `src/main` altındaki tüm public sınıf ve metotlara Türkçe Javadoc eklendi; `src/test` altında Javadoc yok, diğer iki eksik duruyor.

## 11. Doğrulama sonuçları (2026-10-10)

İlk sürümde "emin olmadığım noktalar" olarak listelenen maddeler `mvn dependency:tree`, `mvn versions:display-dependency-updates`, `mvn versions:display-plugin-updates`, `mvn help:describe` ve yerel Maven deposundaki jar'ların `javap` ile incelenmesiyle kontrol edildi. Testler yine çalıştırılmadı.

| Madde | Sonuç | Kanıt |
|---|---|---|
| Thread havuzu varsayılanı 10 | Doğrulandı | TestNG 7.10.2 `XmlSuite.DEFAULT_DATA_PROVIDER_THREAD_COUNT = 10` (`DEFAULT_THREAD_COUNT = 5`) |
| Allure'da `RuntimeException` → "broken" | Doğrulandı | `allure-java-commons` 2.29.0 `ResultsUtils.getStatus`: `AssertionError` ise `FAILED`, değilse `BROKEN`; `AllureCucumber7Jvm.translateTestCaseStatus` bu metodu çağırıyor |
| Sürümler eski | Doğrulandı | Bölüm 9'daki tablo |
| `allure-maven` rapor sürümü farklı | Doğrulandı | `reportVersion` parametresinin varsayılanı yok; eklenti 2.12.0 kodunda gömülü sürüm 2.20.1, kütüphane 2.29.0 |
| README'deki SLF4J notu | Kısmen yanlış | `slf4j-api` 1.7.36'yı Selenium değil TestNG getiriyor; Selenium 4.45.0'ın ağacında SLF4J yok |
| Selenium SLF4J kullanmıyor (`logback.xml:18` etkisiz) | Doğrulandı | Aynı bağımlılık ağacı; `jul-to-slf4j` köprüsü de yok |
| Boş sonuçla `mvn allure:report` davranışı | Doğrulandı (2. madde düzeltilirken) | Sonuç klasörü yokken `Allure report was skipped` uyarısı verip `BUILD SUCCESS` ile bitiyor; yani eski pipeline bu durumda gerçekten yeşil kalıyordu |

Açık kalan doğrulama maddesi yok.

## 12. Sağlamlaştırma değişiklikleri (2026-10-10)

İnceleme sonrasında istenen altı sağlamlaştırma değişikliği, `docs/inceleme-raporu` dalında ayrı commit'ler olarak uygulandı. Mimari, paket yapısı, step definition'lar ve feature dosyaları değişmedi.

| # | Commit | Değişiklik | Rapordaki karşılığı |
|---|---|---|---|
| 1 | `46f530e` | `DriverManager.getDriver()` driver yoksa açıklayıcı mesajla `IllegalStateException` fırlatıyor. `DriverManager.hasDriver()` eklendi; `@AfterStep` driver yoksa screenshot'ı atlayıp uyarı logluyor | Bölüm 3, "Hook'lar korumasız" (kısmen) |
| 2 | `0eb3693` | `DriverManager.quitDriver()`: `quit()` `try` içinde, `ThreadLocal.remove()` `finally` içinde | Bölüm 3, "Hook'lar korumasız" (`quitDriver` kısmı kapandı) |
| 3 | `da8ad45` | `drivers` paketine `BrowserType` enum'u (CHROME, FIREFOX, EDGE) eklendi; `fromString` geçersiz değerde desteklenen tarayıcıları listeliyor. `DriverFactory.createDriver` `BrowserType` alıyor, `Hooks` güncellendi | Bölüm 2 madde 7 ve bölüm 3, "Türkçe locale hatası" (kısmen) |
| 4 | `782f08f` | `ConfigReader`: `config.properties` classpath'te yoksa `IllegalStateException` | Raporda bulgu olarak yer almıyordu |
| 5 | `5c2f836` | `config.properties`'e `page.load.timeout=30` eklendi; `DriverFactory` her driver'a uyguluyor | Raporda bulgu olarak yer almıyordu |
| 6 | `45e4d48` | `ElementActions` ve `NavigationActions` metotlarına DEBUG logları; `src/main` altındaki tüm public sınıf ve metotlara Türkçe Javadoc | Bölüm 10, "Eksikler" (Javadoc, kısmen) |

**Doğrulama:** Her commit ayrı ayrı derlendi. Altı değişiklikten sonra `mvn clean test -Dheadless=true` 19/19 geçti (29,0 sn); değişiklik öncesinde de 19/19 geçiyordu (28,9 sn).

**Bilinmesi gerekenler:**
- Yeni hata yollarını (driver'ın olmaması, `quit()` hatası, geçersiz tarayıcı adı, eksik `config.properties`) tetikleyen bir test yok; bunlar yalnızca derlendi.
- `ConfigReader` kontrolü statik blokta olduğu için eksik dosya hatası `ExceptionInInitializerError` içinde, neden olarak `IllegalStateException` ile çıkar.
- DEBUG logları varsayılan ayarda görünmez; `logback.xml` proje paketini INFO seviyesinde tutuyor ve değiştirilmedi. `ElementActions.type` yazılan metni loglamıyor.
- `DriverFactory` her senaryoda INFO seviyesinde `Page load timeout: 30s` satırı yazıyor.
- Bu raporun önceki bölümlerindeki satır numaraları inceleme anındaki koda (`c1c5671`) göredir; Javadoc eklendiği için `src/main` dosyalarında satırlar kaydı.

## 13. İkinci düzeltme turu (2026-10-10)

İstenen on bir düzeltme `docs/inceleme-raporu` dalında ayrı commit'ler olarak uygulandı. Mimari ve paket yapısı değişmedi.

| # | Commit | Değişiklik | Rapordaki karşılığı |
|---|---|---|---|
| 1 | `dcb4186` | `JsonDataReader`'daki iki `toLowerCase()` çağrısı `Locale.ROOT` kullanıyor | Bölüm 2 madde 7, bölüm 3 "Türkçe locale hatası" (kapandı) |
| 2 | `768743a` | `@AfterStep` içinde `getScreenshotAs` `try/catch` içinde; hata yalnızca uyarı olarak loglanıyor | Bölüm 3 "Hook'lar korumasız" (kapandı) |
| 3 | `d738eef` | `verifyValueContains` → `verifyValueEquals`: tam eşitlik, `getDomProperty("value")`, null değerde `AssertionError`, mesajda "Actual value". `VerificationSteps` içindeki iki çağrı güncellendi | Bölüm 2 madde 5, bölüm 3 "Değer doğrulaması gevşek" |
| 4 | `e1ca570` | `VerificationActions` doğrulama hatalarında locator içeren `AssertionError` fırlatıyor; `verifyElementInvisible` `TimeoutException`'ı aynı şekilde çeviriyor | Bölüm 2 madde 6, bölüm 3 "`verifyElementInvisible` tutarsız" |
| 5 | `79f20ef` | INV-02 ve PD-04'e `Then the "Cart Badge" should not be visible` eklendi | Bölüm 8 "Senaryo başlığı ile doğrulama uyuşmuyor" |
| 6 | `dabb0f8` | `JsonDataReader`: aynı isim farklı değerle tanımlıysa iki dosya adını içeren `IllegalStateException`; aynı değerli tekrarlar kabul ediliyor | Bölüm 2 madde 3, bölüm 3 "Aynı isimli kayıtlar" |
| 7 | `74389b7` | Üç suite dosyasından `browser` parametresi kaldırıldı; `Hooks` yalnızca `ConfigReader.get("browser", "chrome")` kullanıyor, `Reporter.getCurrentTestResult()` silindi | Bölüm 2 madde 4, bölüm 3 "Tarayıcı çözümleme sırası" |
| 8 | `22cd55e` | `allure-maven` eklentisine `<reportVersion>${allure.version}</reportVersion>` eklendi | Bölüm 9 "Allure raporu eski bir CLI ile üretiliyor" |
| 9 | `edf8198` | `ci.yml` ve `manual-rerun.yml`'e üst seviye `permissions: contents: read` (manual-rerun'da ayrıca `actions: read`); `deploy-pages` izinleri aynı | Bölüm 4 "Workflow'larda `permissions` bloğu yok" |
| 10 | `b44b395` | README: tarayıcı önceliği, SLF4J notu (TestNG), tag filtresi, `cd` klasör adı. Ek olarak bu turdaki kod değişikliklerinin sonuçları: `AssertionError` örneği, `@AfterStep` kod örneği, proje ağacında `BrowserType.java` | Bölüm 10 "Tarayıcı önceliği", "SLF4J notu", "Tutarsızlıklar" |
| 11 | Bu bölümü ekleyen commit'ten sonraki commit | Raporun giriş paragrafı ve kapanış cümlesi güncel duruma göre yeniden yazıldı | Yok |

**Doğrulama:**
- Her commit ayrı ayrı derlendi.
- On maddeden sonra `mvn clean test -Dheadless=true`: 19 senaryonun 19'u geçti (25,7 sn). Adım sayısı 252'den 256'ya çıktı; fark, 5. maddede eklenen iki adım ve bunların `@AfterStep` hook'ları.
- Madde 4: Kasıtlı başarısız LG-09 tek başına koşturuldu; hata `java.lang.AssertionError: Element is not visible. Locator: By.cssSelector: .title` olarak çıktı ve Allure sonuç dosyasında durum `failed` (önceden `broken` olurdu).
- Madde 1 ve 6: Depo dışındaki geçici bir programla denendi. `tr-TR` locale'de `"ID"` locator tipi doğru çözüldü; aynı isim iki dosyada farklı değerle tanımlıyken `Duplicate locator 'Cart Badge' is defined with different values in 'a.json' and 'b.json'.` hatası alındı; aynı değerli tekrar hata vermedi.
- Madde 7: `-Dbrowser=opera` ile `Unsupported browser: 'opera'. Supported browsers: chrome, firefox, edge` hatası alındı.
- Madde 8: `mvn allure:report`, `Generate Allure report (report) with version 2.29.0` yazıp raporu başarıyla üretti.
- Madde 9: İki workflow dosyası YAML olarak ayrıştırıldı; `deploy-pages` job'ının `pages: write` ve `id-token: write` izinleri yerinde.

**Doğrulanmayanlar ve bilinmesi gerekenler:**
- Madde 2 (screenshot hatası) ve madde 3 (`verifyValueEquals`) hiçbir senaryoda tetiklenmiyor; yalnızca derlendi. "should have value" adımlarını kullanan bir feature yok.
- Madde 3 bir davranış değişikliğidir: "should have value" adımları artık kısmi eşleşmeyi kabul etmez.
- Madde 4: İlk haliyle (`e1ca570`) `VerificationActions` içindeki üç `catch (WebDriverException)` bloğunun hepsi `AssertionError`'a çevrilmişti; bu, altyapı kaynaklı hataların da (örn. kopan oturum) "failed" görünmesi demekti. Bu yan etki aşağıdaki "Madde 4'e ek düzeltme" ile giderildi.
- Madde 6: Hata mesajı değerleri içermez (test verisi gizli olabilir), yalnızca isim ve iki dosya adını verir.
- Madde 9: Workflow'lar GitHub Actions üzerinde koşmadı (push yapılmadı). `test` job'ının `contents: read` ile artifact yükleme ve cache adımlarını çalıştırması beklenir, ancak denenmedi.

**Madde 4'e ek düzeltme (2026-10-10, commit `9fbc764`):**

`VerificationActions` içindeki dört `catch` bloğu daraltıldı:
- **`TimeoutException`** (element beklenen sürede görünmedi ya da kaybolmadı): locator bilgisini içeren `AssertionError`; Allure'da "failed".
- **Diğer `WebDriverException` türleri** (kopan oturum, çöken tarayıcı vb.): locator bilgisini içeren `RuntimeException` (`Verification could not be performed. Locator: ...`); Allure'da "broken".

Bu değişiklikten sonraki doğrulama koşumları:

| Koşum | Sonuç | Not |
|---|---|---|
| `mvn clean test -Dheadless=true` | 19/19 geçti, 256 adım, 25,3 sn | Log'da 1 thread |
| Aynı komut, `-DsuiteXmlFile=src/test/resources/testng-parallel.xml` | 19/19 geçti, 256 adım, 14,1 sn | Log'da 3 thread |
| Yalnızca LG-09 (`-Dcucumber.filter.tags=@excluded-from-ci`) | 1/1 düştü (beklenen) | Hata `java.lang.AssertionError: Element is not visible. Locator: By.cssSelector: .title`; Allure sonuç dosyasında senaryo ve adım durumu `failed`, sonuçta 1 ek (ekran görüntüsü) var |

README'de `VerificationActions`'ı anlatan kısım ("Bağlamsal Hata Mesajları") bu değişiklikle yanlış kalıyordu: tüm doğrulama hatalarının `AssertionError` olduğunu söylüyordu. Commit `d6394cf` ile zaman aşımı ve diğer WebDriver hataları ayrımını anlatacak şekilde güncellendi. LG-09 açıklamasındaki "timeout sonrası `AssertionError` fırlar" ifadesi doğru olduğu için değişmedi.

Doğrulanmayan: `RuntimeException` yolu (zaman aşımı dışındaki WebDriver hataları) hiçbir koşumda tetiklenmedi; kopan oturum ya da çöken tarayıcı durumu denenmedi, yalnızca derlendi.

## 14. Hata ekran görüntüsünün başarısız adıma bağlanması (2026-10-10)

**Sorun:** Allure raporunda hata ekran görüntüsü başarısız adımın içinde değil, ondan sonraki yeşil `attachScreenshotOnFailure` hook satırında görünüyordu ve bu hook satırı her adımdan sonra tekrar ediyordu. Neden: ek, bir `@AfterStep` hook'u içinde üretiliyordu; Allure her hook'u ayrı bir adım olarak raporluyor. Bu, inceleme raporunda bulgu olarak yer almıyordu.

**Değişiklik** (commit `00c2e5b`, README güncellemesi `f912345`):
- **Yeni yardımcı sınıf:** `utils/FailureScreenshot`. Driver varsa ekran görüntüsünü alıp `Allure.addAttachment` ile "Failure Screenshot" adıyla (`image/png`) ekliyor. Kendi hatalarını yutup yalnızca uyarı logluyor. Thread'e özel bir bayrakla senaryo başına en fazla bir ek üretiyor.
- **Action sınıfları:** `ElementActions`, `VerificationActions`, `WaitActions`, `NavigationActions`, `AlertActions` ve `BrowserActions` hata fırlatmadan hemen önce yardımcıyı çağırıyor. Daha önce hatayı yakalamayan sınıflara (`WaitActions`, `NavigationActions`, `AlertActions`, `BrowserActions`) yalnızca "ekle ve aynı hatayı yeniden fırlat" yapan bir `catch (WebDriverException)` eklendi; fırlatılan hata türleri değişmedi.
- **`VerificationActions`:** TestNG `Assert` çağrıları açık `if` + `AssertionError`'a çevrildi, çünkü ekran görüntüsünün hatadan önce alınması gerekiyor. Hata mesajları artık TestNG'nin eklediği `expected [..] but found [..]` son ekini içermiyor.
- **`Hooks`:** `@AfterStep` metodu kaldırıldı. `@After`, senaryo başarısızsa ve henüz ek yoksa yedek olarak bir tane ekliyor; bayrak `@Before` ve `@After` içinde sıfırlanıyor.

**Doğrulama:**

| Koşum | Sonuç |
|---|---|
| `mvn clean test "-Dcucumber.filter.tags=@excluded-from-ci"` (yalnızca LG-09) | 1/1 düştü (beklenen). Allure sonuç dosyasında "Failure Screenshot" eki (34.734 bayt PNG) başarısız `Then the "Products Title" should be visible` adımının `attachments` listesinde; senaryo düzeyinde ve `teardown` fixture'ında ek yok; `attachScreenshotOnFailure` adlı adım yok |
| `mvn clean test -Dheadless=true` | 19/19 geçti, 26,0 sn, log'da 1 thread |
| Paralel suite (`testng-parallel.xml`) | 19/19 geçti, 14,3 sn, log'da 3 thread |

Cucumber'ın adım sayısı 256'dan 147'ye indi; fark, artık çalışmayan `@AfterStep` hook'ları (adım başına bir tane).

**Doğrulanmayanlar:**
- `@After` içindeki yedek yol hiçbir koşumda tetiklenmedi; action sınıflarından geçmeyen bir hata üreten senaryo yok.
- Yardımcının kendi hatasını yutması (açık alert, kopmuş oturum) ve `WaitActions` dışındaki yeni `catch` blokları tetiklenmedi; yalnızca derlendi.
- Raporun tarayıcıdaki görünümüne bakılmadı; kontrol Allure sonuç JSON dosyası üzerinden yapıldı.

Bölüm 12 ve 13'te `@AfterStep` hook'una yapılan düzeltmeler (`46f530e`, `768743a`) o hook kaldırıldığı için artık kodda yok; aynı korumalar `FailureScreenshot` içinde duruyor.

## 15. Güncel durum ve açık kalanlar

Öncelik tablosundaki on maddenin yedisi düzeltildi (iki kritik madde ve 3-7 numaralı orta öncelikli maddeler). Kalan üç madde proje sahibinin bilinçli kararıyla ertelendi; bunlar unutulmuş açık işler değil, koşulu gerçekleşince ele alınacak maddelerdir.

| Madde | Karar | Gerekçe | Ne zaman ele alınacak |
|---|---|---|---|
| 8: Adımların çoğu hiçbir senaryoda kullanılmıyor | Ertelendi | Adımlar genel amaçlı bir kütüphane olarak tasarlandı. Sırf kapsam için senaryo yazılmayacak | İhtiyaç oldukça; bir adım gerçek bir senaryoda gerektiğinde kullanılacak |
| 9: `src/main` kodu `src/test/resources` yoluna sabitlenmiş | Ertelendi | `src/main` ayrı bir jar olarak dağıtılmıyor | Jar olarak dağıtılması gündeme gelirse |
| 10: Ortam değiştirme mekanizması yok | Ertelendi | Şu an tek ortam var | Çatı, birden fazla ortamı olan bir uygulamaya uyarlanırken |

Madde 8 için güncel sayı: "should not be visible" adımı artık INV-02 ve PD-04'te kullanılıyor; hiçbir senaryoda kullanılmayan adım sayısı 15'ten 14'e indi.

Bunların dışında bölüm 4-10'da işaretsiz duran orta ve düşük öncelikli maddeler hâlâ açık ve haklarında bir karar verilmedi. Öne çıkanlar: yalnızca testte kullanılan bağımlılıkların `test` scope'una alınması, bağımlılık güncellemeleri, çatı kodu için birim testleri ve workflow'ların ilk push'ta GitHub Actions üzerinde doğrulanması.