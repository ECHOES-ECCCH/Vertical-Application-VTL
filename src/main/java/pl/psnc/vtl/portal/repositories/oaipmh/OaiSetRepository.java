package pl.psnc.vtl.portal.repositories.oaipmh;

import org.dspace.xoai.dataprovider.handlers.results.ListSetsResult;
import org.dspace.xoai.dataprovider.repository.SetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OaiSetRepository implements SetRepository {
    @Override
    public boolean supportSets() {
        return false;
    }

    @Override
    public ListSetsResult retrieveSets(int offset, int length) {
        return new ListSetsResult(false, List.of());
    }

    @Override
    public boolean exists(String setSpec) {
        return false;
    }
}
