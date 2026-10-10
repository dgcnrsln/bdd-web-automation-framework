package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sayfa elementleri üzerindeki etkileşimler (tıklama, yazma, temizleme, kaydırma).
 * Her işlem öncesinde element için beklenir; hata durumunda locator bilgisini içeren bir exception fırlatılır.
 */
public class ElementActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementActions.class);

    private final WaitActions waitActions;

    /**
     * @param waitActions element beklemeleri için kullanılacak yardımcı
     */
    public ElementActions(WaitActions waitActions) {
        this.waitActions = waitActions;
    }

    /**
     * Element tıklanabilir olana kadar bekler ve tıklar.
     *
     * @param locator tıklanacak elementin locator'ı
     * @throws RuntimeException element tıklanamazsa
     */
    public void click(By locator) {
        LOGGER.debug("Clicking element: {}", locator);

        try {
            waitActions.clickable(locator).click();
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be clicked. Locator: " + locator, e);
        }
    }

    /**
     * Element tıklanabilir olana kadar bekler ve JavaScript ile tıklar.
     *
     * @param locator tıklanacak elementin locator'ı
     * @throws RuntimeException element tıklanamazsa
     */
    public void javascriptClick(By locator) {
        LOGGER.debug("Clicking element via JavaScript: {}", locator);

        try {
            WebElement element = waitActions.clickable(locator);

            ((JavascriptExecutor) DriverManager.getDriver())
                    .executeScript("arguments[0].click();", element);
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be clicked via JavaScript. Locator: " + locator, e);
        }
    }

    /**
     * Element görünür olana kadar bekler, içeriğini temizler ve verilen metni yazar.
     * Yazılan metin gizli veri içerebileceği için loglanmaz.
     *
     * @param text    yazılacak metin
     * @param locator metnin yazılacağı elementin locator'ı
     * @throws RuntimeException metin yazılamazsa
     */
    public void type(String text, By locator) {
        LOGGER.debug("Typing into element: {}", locator);

        try {
            WebElement element = waitActions.visible(locator);
            element.clear();
            element.sendKeys(text);
        } catch (WebDriverException e) {
            throw new RuntimeException("Text could not be entered. Locator: " + locator, e);
        }
    }

    /**
     * Element görünür olana kadar bekler ve içeriğini temizler.
     *
     * @param locator temizlenecek elementin locator'ı
     * @throws RuntimeException element temizlenemezse
     */
    public void clear(By locator) {
        LOGGER.debug("Clearing element: {}", locator);

        try {
            waitActions.visible(locator).clear();
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be cleared. Locator: " + locator, e);
        }
    }

    /**
     * Element görünür olana kadar bekler ve sayfayı element ortada kalacak şekilde kaydırır.
     *
     * @param locator kaydırılacak elementin locator'ı
     * @throws RuntimeException elemente kaydırılamazsa
     */
    public void scroll(By locator) {
        LOGGER.debug("Scrolling to element: {}", locator);

        try {
            WebElement element = waitActions.visible(locator);

            ((JavascriptExecutor) DriverManager.getDriver())
                    .executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});",
                            element
                    );
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be scrolled into view. Locator: " + locator, e);
        }
    }
}
