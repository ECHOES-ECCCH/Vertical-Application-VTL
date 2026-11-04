package pl.psnc.vtl.portal.repositories.ocrmodels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.psnc.vtl.portal.model.ocrmodels.OcrModel;
import pl.psnc.vtl.portal.model.users.User;

import java.util.List;

public interface OcrModelRepository extends JpaRepository<OcrModel, Long> {

    @Query("SELECT m FROM OcrModel m JOIN FETCH m.owner WHERE m.isPublic = true")
    List<OcrModel> findAllPublic();

    @Query("SELECT m FROM OcrModel m JOIN FETCH m.owner WHERE m.isPublic = true OR m.owner = :owner")
    List<OcrModel> findAllPublicOrOwnedBy(@Param("owner") User owner);

}
