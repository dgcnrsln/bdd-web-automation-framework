# Değerlendirme Raporu: BDD Web Automation Framework

Hiçbir dosyayı değiştirmedim. İzlenen 47 dosyanın tamamını okudum; testleri çalıştırmadım (çalıştırmak `target/` üretirdi), bu yüzden bulgular statik incelemeye dayanıyor. Belirsiz kalan noktalar sonradan Maven komutlarıyla kontrol edildi; sonuçlar sondaki bölümde.

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
| 1 | Kritik | "Tekli" suite sıralı değil; iki suite de 10 thread ile koşuyor | `TestRunner.java:23`, `testng-*.xml` |
| 2 | Kritik | CI, derleme/altyapı hatasında yeşil kalabiliyor | `ci.yml:32-57` |
| 3 | Orta | Aynı isimli locator/veri sessizce üzerine yazılıyor | `JsonDataReader.java:39,58` |
| 4 | Orta | `config.properties` içindeki `browser` hiç kullanılmıyor; IDE'den koşumda NPE | `Hooks.java:49-65` |
| 5 | Orta | "should have value" aslında "contains"; null değerde NPE | `VerificationActions.java:46-62` |
| 6 | Orta | Doğrulama hataları `RuntimeException`; Allure'da "broken" görünür | `VerificationActions.java:18,34` |
| 7 | Orta | Türkçe locale'de `toLowerCase()` hatası | `DriverFactory.java:34`, `JsonDataReader.java:89` |
| 8 | Orta | 21 adımın 15'i hiçbir senaryoda kullanılmıyor | `features/*.feature` |
| 9 | Orta | `src/main` kodu `src/test/resources` yoluna sabitlenmiş | `ResourcePaths.java:7-9` |
| 10 | Orta | Ortam değiştirme mekanizması yok | `saucedemo.json:3-4` |

## 3. Hatalar ve hataya açık kod

**[Kritik] Suite'ler iddia edilen thread sayısıyla koşmuyor.**
- **Konum:** `TestRunner.java:22-26`, `RerunRunner.java:20-24`, `testng-single.xml:4`, `testng-parallel.xml:5-6`.
- **Sorun:** Runner'lar `@DataProvider(parallel = true)` kullanıyor. Bu havuzun boyutunu `thread-count` değil `data-provider-thread-count` belirler ve varsayılanı 10'dur. Yani `testng-single.xml` de paralel koşar, `testng-parallel.xml` içindeki `thread-count="3"` ise senaryo sayısını etkilemez. CI, 4 çekirdekli runner'da aynı anda 10 Chrome açıp herkese açık bir siteye yüklenir; bu doğrudan flaky test kaynağıdır.
- **Öneri:** `testng-single.xml`'e `data-provider-thread-count="1"`, `testng-parallel.xml`'e `data-provider-thread-count="3"` ekleyin. Alternatif olarak surefire'da `dataproviderthreadcount` özelliğini `-Dthreads` ile parametrik yapın.

**[Orta] Aynı isimli kayıtlar sessizce eziliyor.**
- **Konum:** `JsonDataReader.java:39,58`; somut örnek `inventory.json:4` ve `product-detail.json:10` ("Cart Badge").
- **Sorun:** Tüm dosyalar tek düz bir map'e yükleniyor ve son okunan kazanıyor. `File.listFiles` sırası garanti değil. Bugün iki değer aynı; biri değişirse hangi locator'ın kullanılacağı belirsiz olur.
- **Öneri:** Yüklemede çakışmayı tespit edip iki dosya adını içeren bir hata fırlatın ya da isimleri sayfa bazında ayırın (`Inventory.Cart Badge`).

**[Orta] Tarayıcı çözümleme sırası `config.properties`'i ölü bırakıyor.**
- **Konum:** `Hooks.java:49-65`, `testng-*.xml` içindeki `<parameter name="browser" value="chrome"/>`.
- **Sorun:** Üç suite dosyası da `browser=chrome` verdiği için XML parametresi her zaman kazanır; `config.properties:1`'i `firefox` yapmak hiçbir şeyi değiştirmez. Ayrıca `Reporter.getCurrentTestResult()` TestNG dışı koşumda (ör. IntelliJ'de feature dosyasından) `null` döner ve `Hooks.java:55` NPE atar.
- **Öneri:** XML'lerden `browser` parametresini kaldırın ve yalnızca `ConfigReader.get("browser", "chrome")` kullanın. Bu, 50-53. satırlardaki tekrarlanan system property kontrolünü de siler.

**[Orta] Değer doğrulaması gevşek ve null'a açık.**
- **Konum:** `VerificationActions.java:46-62`, `VerificationSteps.java:51,59`.
- **Sorun:** Adım "should have value" diyor ama `contains` ile kontrol ediyor: beklenen `"a"`, gerçek `"abc"` iken geçer; beklenen `""` her zaman geçer. `getAttribute("value")` `null` dönerse 55. satır NPE atar. Hata mesajı da "Actual text" diyor (57. satır, kopyala-yapıştır).
- **Öneri:** `assertEquals` kullanın ya da adımı "should contain value" olarak yeniden adlandırın; `getDomProperty("value")`'ya geçip null kontrolü ekleyin.

**[Orta] Türkçe locale hatası.**
- **Konum:** `DriverFactory.java:34`, `JsonDataReader.java:68,89`.
- **Sorun:** Argümansız `toLowerCase()` JVM locale'ini kullanır. `tr-TR` makinede `"ID"` → `"ıd"`, `"FIREFOX"` → `"fırefox"` olur ve "Unsupported ..." hatası alınır. CI (İngilizce locale) geçerken yerel makinede kırılır.
- **Öneri:** `toLowerCase(Locale.ROOT)`.

**[Orta] `verifyElementInvisible` diğerleriyle tutarsız.**
- **Konum:** `VerificationActions.java:22-26`.
- **Sorun:** `waitActions.invisible` başarısızlıkta `false` dönmez, `TimeoutException` fırlatır. Bu yüzden 25. satırdaki assert mesajı hiç görünmez ve locator bağlamı kaybolur.
- **Öneri:** `TimeoutException`'ı yakalayıp locator içeren bir `AssertionError` fırlatın.

**[Düşük] Hook'lar kendi hatalarına karşı korumasız.**
- **Konum:** `Hooks.java:34-35`, `DriverManager.java:19-23`.
- **Sorun:** Açık bir alert ya da ölmüş oturum varken `getScreenshotAs` hata atar ve asıl hatanın üstüne ikinci bir hata biner. `quit()` hata atarsa `driver.remove()` çalışmaz.
- **Öneri:** Screenshot'ı `try/catch` içine alıp sadece loglayın; `quitDriver`'da `try { quit } finally { remove }` kullanın.

**[Düşük] Diğer hataya açık noktalar.**
- `AlertActions.java:8,13`: Alert beklenmeden `switchTo().alert()` çağrılıyor. `ExpectedConditions.alertIsPresent()` ile bekleyin.
- `BrowserActions.java:22`: Dosya adı yalnızca `currentTimeMillis`; paralel koşumda çakışırsa `Files.copy` hata atar. Thread adı ya da UUID ekleyin.
- `JsonDataReader.java:35-37,55-56`: Eksik alan çıplak NPE verir, hangi dosyada olduğu bilinmez. Dosya adını içeren bir hata fırlatın.
- `JsonDataReader.java:70-72`: Mesaj "not found or empty" diyor ama boş klasör hata vermez, boş map döner.
- `WaitActions.java:13`: `-Dtimeout=abc` verilirse statik init patlar ve anlaşılmaz bir `NoClassDefFoundError` çıkar.

## 4. Güvenlik sorunları

Kritik bir bulgu yok; gerçek gizli bilgi bulunmuyor.

- **[Orta] Workflow'larda `permissions` bloğu yok** (`ci.yml`, `manual-rerun.yml`). `test` job'ı deponun varsayılan token yetkisiyle koşar. Üst seviyeye `permissions: contents: read` ekleyin; `manual-rerun.yml` için ayrıca `actions: read` gerekir.
- **[Düşük] Action'lar tag ile sabitlenmiş** (`@v4`, `@v3`), commit SHA ile değil. SHA'ya sabitleyip Dependabot ile güncelleyin.
- **[Düşük] Kimlik bilgisi düz metin** (`testdata/login.json:6`). `secret_sauce` Sauce Demo'nun herkese açık parolası, yani sızıntı değil. Ancak çatı gerçek bir uygulamaya uyarlanırsa gizli veri için bir yol yok. `DataRepository`'ye `${ENV:NAME}` gibi ortam değişkeni çözümlemesi ekleyin.
- **[Düşük] Allure raporu herkese açık Pages'e gidiyor** (`ci.yml:81-103`). `I enter "{string}"` adımıyla yazılan literal değerler ve ekran görüntüleri rapora girer. Demo için sorun değil, gerçek projede dikkat edin.

## 5. Mimari ve kod organizasyonu

- **[Orta] "Kütüphane" test kaynaklarına bağımlı.** `ResourcePaths.java:7-9` `src/test/resources/...` yolunu çalışma dizinine göreli veriyor. `src/main` jar olarak paketlenip başka projede kullanılamaz ve Maven proje kökü dışından çalıştırılırsa bozulur. Classpath üzerinden okuyun ya da kökü `ConfigReader` ile yapılandırılabilir yapın.
- **[Orta] Ortam yönetimi gerçek anlamda yok.** `saucedemo.json:3-4` düz bir anahtar→tam URL listesi; envanter URL'i ayrıca elle yazılmış. Staging/prod arasında geçiş yapılamaz. `-Denv=...` ile seçilen bir `baseUrl` ve göreli path'ler kullanın.
- **[Orta] `JsonDataReader` iki iş yapıyor.** Her örnek aynı klasör için hem locator hem veri map'i yüklüyor (`JsonDataReader.java:23-24`); `EnvironmentRepository.java:14` de URL almak için `getTestData` çağırıyor ve ortam dosyasının kök anahtarı `testData`. Locator ve anahtar-değer okuyucularını ayırın.
- **[Düşük] DI ile statik erişim karışık.** Actions sınıfları `DriverManager.getDriver()`'ı statik çağırıyor. PicoContainer zaten senaryo başına örnek ürettiği için sürücüyü enjekte edilen bir sınıfta tutmak `ThreadLocal`'ı gereksiz kılar.
- **[Düşük] `VerificationActions` `src/main` içinde TestNG `Assert` kullanıyor**; bu yüzden TestNG compile scope'ta kalmak zorunda.
- **[Düşük] Tag filtresi runner'a gömülü** (`TestRunner.java:14`). `@excluded-from-ci` adına rağmen yerelde de dışlıyor. Filtreyi CI'da `-Dcucumber.filter.tags` ile verin.

## 6. Kod tekrarı ve okunabilirlik

- **[Düşük] Login adımları iki Background'da tekrarlanıyor** (`inventory.feature:5-8`, `product-detail.feature:5-8`). `Given I am logged in as "Standard User"` gibi bileşik bir adım yazın.
- **[Düşük] Aynı try/catch sarmalayıcısı** `ElementActions.java`'da 5, `VerificationActions.java`'da 3 kez tekrar ediyor. Tek bir yardımcı metoda ve özel bir exception tipine çekin.
- **[Düşük] Chrome ve Edge headless ayarları aynı** (`DriverFactory.java:61-65`, `83-87`). İkisi de `ChromiumOptions`'tan türediği için ortak metoda alın. `--window-position=-32000,-32000` headless'ta gereksiz; Firefox'ta pencere boyutu hiç ayarlanmıyor.
- **[Düşük] İsimlendirme:** `JsonDataReader.java:109`'da parametre `elementName` (aslında veri anahtarı); `ActionSteps.java:45`'te `text` (aslında anahtar); `NavigationActions.java:6-7`'de boş constructor.
- **[Düşük] Gherkin stili:** Ardışık `When ... When ...` yerine `And` kullanın. "Products Title" (`.title`) `login.json:7`'de duruyor ama envanter sayfasına ait.

## 7. Performans

- **[Kritik]** Kontrolsüz 10 thread (bölüm 3'ün ilk maddesi) aynı zamanda en büyük performans ve kararlılık riski.
- **[Düşük] Her senaryo yeni tarayıcı açıp UI'dan login oluyor** (`Hooks.java:24`). 19 senaryoda sorun değil; büyürse login'i `session-username` çerezi ile atlayın.
- **[Düşük] Metin doğrulaması tek seferlik okuma yapıyor** (`VerificationActions.java:32`). Geç güncellenen metinlerde flaky olur; `ExpectedConditions.textToBePresentInElementLocated` kullanın.
- **[Düşük] Her klasör iki kez parse ediliyor** (bölüm 5'teki çift yükleme). Etkisi ihmal edilebilir.
- **[Düşük] CI:** `ci.yml:24-29`'daki elle cache adımı yerine `setup-java`'nın `cache: maven` seçeneği yeter; job'larda `timeout-minutes` yok.

## 8. Test eksikleri

- **[Orta] 21 adımın 15'i hiçbir senaryoda kullanılmıyor.** Kullanılanlar: navigate, enter the data, click, should be visible, should contain, should contain the data. Refresh/back/forward, JS click, literal enter, clear, scroll, wait, should not be visible, iki value adımı, screenshot, Enter ve iki alert adımı hiç çalışmamış kod. Bunları örnekleyen senaryolar ekleyin; alert için Sauce Demo yetersiz, başka bir demo sayfası gerekir.
- **[Orta] Senaryo başlığı ile doğrulama uyuşmuyor.** `inventory.feature:14-17` "Cart badge disappears" diyor ama "Add To Cart" görünürlüğünü kontrol ediyor; `product-detail.feature:23-26` de aynı. `Then the "Cart Badge" should not be visible` ekleyin.
- **[Orta] Çatı kodunun birim testi yok.** `JsonDataReader` (eksik alan, çakışma, bilinmeyen locator tipi) ve `ConfigReader` (öncelik sırası) tarayıcısız test edilebilir.
- **[Düşük] LG-09'un adı ve doğrulaması çelişiyor** (`login.feature:49-54`): "should not show" diyor, "should be visible" bekliyor. Kasıtlı olduğu README'de yazıyor; `@demo-failure` gibi bir etiket ve ayrı feature dosyası daha net olur.
- **[Düşük] LG-01 yalnızca `.title` görünürlüğüne bakıyor**, metnin "Products" olduğunu doğrulamıyor.
- **[Düşük] Firefox ve Edge yolları hiç koşmuyor**; CI yalnızca Chrome kullanıyor.

## 9. Bağımlılıklar ve yapılandırma

**[Kritik] CI, senaryo dışı hatalarda yeşil kalabiliyor.**
- **Konum:** `ci.yml:31-57`.
- **Sorun:** Test adımı `continue-on-error: true`. Derleme hatası olursa `target/rerun.txt` hiç oluşmaz, `has_failures=false` çıkar ve rerun atlanır. `mvn allure:report` lifecycle çalıştırmadığı için derlemeyi denemez. Job yeşil biter ve Pages'teki rapor boş bir raporla ezilebilir. Ayrıca ilk koşumda düşüp rerun'da geçen senaryolar sessizce yeşil sayılıyor.
- **Öneri:** Test adımına `id` verin. Sonda `if: steps.test.outcome == 'failure' && steps.check_rerun.outputs.has_failures != 'true'` koşuluyla `exit 1` yapan bir adım ekleyin. Rerun'da geçen senaryoları job özetine yazdırın.

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

**[Orta] Allure raporu kütüphaneden eski bir CLI ile üretiliyor** (`pom.xml:94-101`). `reportVersion` tanımlı değil; `allure-maven` 2.12.0'ın koduna gömülü varsayılan 2.20.1, sonuçları yazan kütüphane ise 2.29.0. `<reportVersion>${allure.version}</reportVersion>` ekleyin.

**[Düşük] Diğer yapılandırma notları.**
- `pom.xml`: Asgari Maven sürümü tanımlı değil (`display-plugin-updates` bunu hata olarak raporluyor; eklentiler en az 3.6.3 istiyor). `maven-enforcer-plugin` ile `requireMavenVersion` ekleyin.
- `pom.xml:12-13`: `source`/`target` yerine `maven.compiler.release` kullanın.
- Maven Wrapper yok ama `.gitignore:2` onu referans alıyor; `mvnw` ekleyin ya da satırı silin.
- `.idea/misc.xml`, `vcs.xml`, `encodings.xml` commit edilmiş. `Reports/` (`BrowserActions.java:20`) `.gitignore`'da yok ve `mvn clean` ile silinmez; çıktıyı `target/screenshots`'a alın.
- `logback.xml:18`: Selenium SLF4J'ye bağımlı değil (bağımlılık ağacıyla doğrulandı) ve `java.util.logging` kullanıyor; bu satır `jul-to-slf4j` köprüsü olmadan etkisiz. Dosyanın `src/main/resources`'ta olması kütüphaneyi kullananlara log ayarı dayatır.
- `-Dbrowser`, `-Dheadless`, `-Dtimeout` çok genel anahtarlar; `automation.` gibi bir ön ek çakışmayı önler.
- `ci.yml:87-103`: Pages deploy için `concurrency` grubu yok.

## 10. Dokümantasyon

- **[Orta] README koşum modlarını yanlış anlatıyor.** `README.md:215` "Tekli (Sıralı)" ve `:223` "3 paralel thread" diyor; ikisi de kodla uyuşmuyor (bölüm 3'ün ilk maddesi).
- **[Orta] Tarayıcı önceliği yanıltıcı.** `README.md:249-254` sırayı doğru veriyor ama `config.properties`'in pratikte hiç devreye girmediğini söylemiyor.
- **[Düşük] Abartılı iddialar:** `README.md:49` "tüm etkileşimler WebDriverWait ile" (alert ve Enter hariç); `:430` "VerificationActions exception'ları sarmalar" (`verifyElementInvisible` hariç).
- **[Düşük] SLF4J notunun kaynağı yanlış** (`README.md:105`). Not, eski `slf4j-api` 1.7.x'i Selenium'un getirdiğini söylüyor. `mvn dependency:tree -Dverbose` çıktısına göre 1.7.36'yı TestNG 7.10.2 getiriyor; Selenium'un ağacında SLF4J yok. Sürümü sabitleme kararı doğru, yalnızca gerekçedeki kütüphane adını düzeltin.
- **[Düşük] Tutarsızlıklar:** `README.md:260` runner'ın `@run` koştuğunu, `:371` ise `@run and not @excluded-from-ci` koştuğunu söylüyor. `:207`'deki `cd BDDWebAutomationFramework`, depo adı `bdd-web-automation-framework` ile uyuşmuyor.
- **[Düşük] Eksikler:** Kodda hiç Javadoc yok, LICENSE dosyası yok, README yalnızca Türkçe.

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
| Boş sonuçla `mvn allure:report` davranışı | Doğrulanmadı | Çalıştırmak `target/` üretip Allure CLI indirirdi; istenen iki komutun kapsamı dışında |

Hâlâ açık tek nokta son satır: sonuç klasörü yokken `allure:report` hata verirse CI maskeleme senaryosu kısmen kendiliğinden yakalanır. Bölüm 9'daki öneri bu davranıştan bağımsız olarak geçerli.

İsterseniz ilk iki kritik maddeyle başlayıp düzeltmeleri uygulayabilirim.