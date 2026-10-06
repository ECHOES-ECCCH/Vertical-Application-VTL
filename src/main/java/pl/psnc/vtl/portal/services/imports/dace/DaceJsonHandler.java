package pl.psnc.vtl.portal.services.imports.dace;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class DaceJsonHandler {
    private static final Configuration jsonPathConfiguration = Configuration.builder()
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS)
            .jsonProvider(new JacksonJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();
    private static final String JSON_PATH_RECORD_BODY = "$.records[0].recordBody.body";
    private static final String JSON_PATH_DACE_ID = "$.records[0].daceId";
    private final DocumentContext documentContext;

    public DaceJsonHandler(String json) {
        documentContext = JsonPath.using(jsonPathConfiguration).parse(json);
    }


    public String getDaceId() {
        return documentContext.read(JSON_PATH_DACE_ID);
    }

    public String getRecordBody() {
        return documentContext.read(JSON_PATH_RECORD_BODY);
    }
}
