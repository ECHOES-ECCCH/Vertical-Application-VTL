package pl.psnc.vtl.portal.services.parsers.utils;

import org.springframework.stereotype.Component;

/**
 * Supplies verses numbers during conversion to WLT model
 */
@Component
public class VerseNumberSupplier {

    private int currentVerseNumber = 1;

    public int getNextVerseNumber() {
        return currentVerseNumber++;
    }

    public int reset(){
        this.currentVerseNumber = 1;
        return this.currentVerseNumber;
    }
}
