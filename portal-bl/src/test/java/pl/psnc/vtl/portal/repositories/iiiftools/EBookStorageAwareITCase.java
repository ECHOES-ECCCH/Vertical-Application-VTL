package pl.psnc.vtl.portal.repositories.iiiftools;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.services.eBook.EBookStorageAware;
import pl.psnc.vtl.portal.services.eBook.Ebook;
import pl.psnc.vtl.portal.services.eBook.EbookDto;
import pl.psnc.vtl.portal.services.eBook.Status;
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EBookStorageAwareITCase {

    @Autowired
    EBookStorageAware eBookStorageAware;

    @Test
    public void shouldReturnEmptyWhenFileDoesntExist(){
        String hashCode = "imposibleHash";

        EbookDto dto = new EbookDto(Ebook.MOBI, 1L, 1L, false, null);
        ExportedEBook eBookLocation = eBookStorageAware.getEBookLocation(hashCode, dto);
        Assert.assertTrue(eBookLocation.getStatus() == Status.NOT_EXISTS);
    }

}