package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;

public class NavigationActions {
    public NavigationActions() {
    }

    public void goTo(String url) {
        DriverManager.getDriver().get(url);
    }

    public void refresh() {
        DriverManager.getDriver()
                .navigate()
                .refresh();
    }

    public void back() {
        DriverManager.getDriver()
                .navigate()
                .back();
    }

    public void forward() {
        DriverManager.getDriver()
                .navigate()
                .forward();
    }
}
