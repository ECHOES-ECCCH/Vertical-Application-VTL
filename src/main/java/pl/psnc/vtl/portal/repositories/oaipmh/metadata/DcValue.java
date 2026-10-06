package pl.psnc.vtl.portal.repositories.oaipmh.metadata;

import lombok.Data;

@Data
public class DcValue {
    private final String value;
    private final String xmlLanguage;
}
