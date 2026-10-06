package pl.psnc.vtl.portal.services.pages;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.services.pages.HOCRPageRecognizer;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class HOCRPageRecognizerTest {

    @Mock
    private PageRepository repo;

    @InjectMocks
    private HOCRPageRecognizer recognizer;
    
    @Test
    public void shouldReturnByPageNoPage(){
        String hocr = "<div class='ocr_page' id='page_1' title='image \"./vtl-storage/originals/t1/page0249.tif\"; bbox 0 0 2596 3760; ppageno 0'>";
        Integer t = recognizer.getPageNoFromHOCRBody(hocr).get();
        Assert.assertEquals(Integer.valueOf(0), t);
    }

    @Test
    public void shouldNotFailWhenThereIsNoPageNo(){
        String hocr = "<div class='ocr_page' id='page_1' title='image \"./vtl-storage/originals/t1/page0249.tif\"; bbox 0 0 2596 3760;'>";
        assertEquals(Optional.empty(),recognizer.getPageNoFromHOCRBody(hocr));
    }

    @Test
    public void shouldReturnPathAndRelativeNumber(){
        String fileName = "path_Page0003.html";
        var pair = new ImmutablePair<>("path", 3);
        Assert.assertEquals(pair, recognizer.getPagePathAndRelativeNo(fileName));
    }


}
