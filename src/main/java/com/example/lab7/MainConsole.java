package com.example.lab7;

import com.example.lab7.ui.UI;
//Java 8 features -> PrietenieValidator, UtilizatorValidator, PrietenieService, UtilizatorService, UI

public class MainConsole {

    public static void main(String[] args) {
        //runTests();

        UI ui = UI.getInstance();
        ui.run();
    }

}
