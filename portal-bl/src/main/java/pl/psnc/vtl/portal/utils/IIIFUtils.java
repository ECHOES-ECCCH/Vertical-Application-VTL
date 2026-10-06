package pl.psnc.vtl.portal.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@UtilityClass
public class IIIFUtils {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private static final Configuration conf = Configuration.builder()
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS)
            .jsonProvider(new JacksonJsonProvider(mapper))
            .mappingProvider(new JacksonMappingProvider(mapper))
            .build();

    private static final JsonPath JSON_PATH_WIDTH = JsonPath.compile("$.width");

    private static final JsonPath JSON_PATH_HEIGHT = JsonPath.compile("$.height");

    private static final String IIIF_CONTEXT_PATH = "$.@context";

    private static final String EXTERNAL_LINKS_PATH_V2 = "$.sequences[0].canvases[*].images[0].resource.@id";

    private static final String EXTERNAL_LINKS_PATH_V3 = "$.items[*].items[0].items[0].body.id";

    private static final String IMAGE_PROPERTY_PATH_V3 = "$.items[{0}].items[0].items[0].body.{1}";

    private static final String CANVAS_PROPERTY_PATH_V3 = "$.items[{0}].{1}";

    private static final String IMAGE_PROPERTY_PATH_V2 = "$.sequences[0].canvases[{0}].images[0].resource.{1}";

    private static final String CANVAS_PROPERTY_PATH_V2 = "$.sequences[0].canvases[{0}].{1}";

    private static final String INFO_JSON_URL_V3 =
            "$.items[{0}].items[0].items[0].body.service[?(@.type == ''ImageService2'' || @.type == ''ImageService3'')].id";

    private static final String INFO_JSON_URL_V2 = "$.sequences[0].canvases[{0}].images[0].resource.service.@id";

    private static final String IIIF_PRESENTATION_V3_CONTEXT = "http://iiif.io/api/presentation/3/context.json";
    private static final String IIIF_IMAGE_V3_CONTEXT = "http://iiif.io/api/image/3/context.json";

    private static final String JSON_PATH_LABEL_PATH_V2 = "$.label";

    private static final String JSON_PATH_LABEL_PATH_V3 = "$.label[*].[0]";

    private static final String JSON_PATH_ID_PATH_V2 = "$.@id";

    private static final String JSON_PATH_ID_PATH_V3 = "$.id";

    private static final String JSON_PATH_DESCRIPTION_PATH_V2 = "$.description";

    private static final String JSON_PATH_DESCRIPTION_PATH_V3 = "$.summary[*].[0]";

    private static final String JSON_PATH_DESCRIPTION_WITH_ARRAY_PATH = "$.description[0].['@value']";

    private static final JsonPath SIGNATURE_PATH = JsonPath.compile("$.metadata[?(@.label=='Sygnatura')].value");

    public static BoundingBox getBoundingBoxFromInfoJson(String infoJson) {
        if (StringUtils.isBlank(infoJson)) {
            return new BoundingBox();
        }
        try {
            return new BoundingBox(
                    0,
                    0,
                    JSON_PATH_WIDTH.read(infoJson),
                    JSON_PATH_HEIGHT.read(infoJson));
        } catch (Exception e) {
            log.error("info.json file does not contain required path", e);
        }
        return new BoundingBox();
    }

    public static List<String> getImagesURLsFromManifestJson(String manifestJson) {
        DocumentContext document = parseJson(manifestJson);
        return document.read(isPresentationApiVersion3(document) ? EXTERNAL_LINKS_PATH_V3 : EXTERNAL_LINKS_PATH_V2);
    }

    public static String getLabelFromManifestJson(String manifestJson) {
        DocumentContext document = parseJson(manifestJson);
        String label = null;
        if (isPresentationApiVersion3(document)) {
            TypeRef<List<String>> typeRef = new TypeRef<>() {
                /* empty */ };
            List<String> labels = document.read(JSON_PATH_LABEL_PATH_V3, typeRef);
            if (labels != null && !labels.isEmpty() && labels.get(0) != null) {
                label = labels.get(0);
            }
        } else {
            label = document.read(JSON_PATH_LABEL_PATH_V2);
        }

        return label;
    }

    public static String getIdFromManifestJson(String manifestJson) {
        DocumentContext document = parseJson(manifestJson);
        return document.read(isPresentationApiVersion3(document) ? JSON_PATH_ID_PATH_V3 : JSON_PATH_ID_PATH_V2);
    }

    public static String getSummaryFromManifestJson(String manifestJson) {
        DocumentContext document = parseJson(manifestJson);
        String summary = null;
        if (isPresentationApiVersion3(document)) {
            TypeRef<List<String>> typeRef = new TypeRef<>() {
                /* empty */ };

            List<String> summaries = document.read(JSON_PATH_DESCRIPTION_PATH_V3, typeRef);
            if (summaries != null && !summaries.isEmpty() && summaries.get(0) != null) {
                summary = summaries.get(0);
            }
        } else {
            String summaryFromArray = document.read(JSON_PATH_DESCRIPTION_WITH_ARRAY_PATH);
            if (summaryFromArray != null) {
                summary = summaryFromArray;
            } else {
                summary = document.read(JSON_PATH_DESCRIPTION_PATH_V2);
            }
        }

        return summary;
    }

    public static boolean isPresentationApiVersion3(DocumentContext document) {
        return isContextEqual(document, IIIF_PRESENTATION_V3_CONTEXT);
    }

    public static boolean isImageApiVersion3(DocumentContext document) {
        return isContextEqual(document, IIIF_IMAGE_V3_CONTEXT);
    }

    private static boolean isContextEqual(DocumentContext document, String context) {
        try {
            String iiifVersion;
            try {
                iiifVersion = document.read(IIIF_CONTEXT_PATH);
            } catch (ClassCastException e) {
                TypeRef<List<String>> typeRef = new TypeRef<>() {
                    /* empty */ };
                List<String> contextArray = document.read(IIIF_CONTEXT_PATH, typeRef);
                iiifVersion = contextArray.get(contextArray.size() - 1); //Version must be the last element according to specification
            }

            return iiifVersion.equals(context);
        } catch (InvalidJsonException e) {
            throw TextFormatValidator.getWrongTypeXMLException();
        }
    }

    public static Integer getImagePropertyInt(DocumentContext document, String propertyName, int index) {
        String value = getImagePropertyString(document, propertyName, index);
        return value == null ? null : Integer.parseInt(value);
    }

    public static String getImagePropertyString(DocumentContext document, String propertyName, int index) {
        String imagePath, canvasPath;
        if (isPresentationApiVersion3(document)) {
            imagePath = MessageFormat.format(IMAGE_PROPERTY_PATH_V3, String.valueOf(index), propertyName);
            canvasPath = MessageFormat.format(CANVAS_PROPERTY_PATH_V3, String.valueOf(index), propertyName);
        } else {
            imagePath = MessageFormat.format(IMAGE_PROPERTY_PATH_V2, String.valueOf(index), propertyName);
            canvasPath = MessageFormat.format(CANVAS_PROPERTY_PATH_V2, String.valueOf(index), propertyName);
        }

        String value = document.read(imagePath, String.class);
        if (value == null) {
            value = document.read(canvasPath, String.class);
        }

        return value;
    }

    public static String getInfoJsonURL(String manifestJson, int index) {
        DocumentContext document = parseJson(manifestJson);
        boolean isV3 = isPresentationApiVersion3(document);
        String infoJsonUrl;
        if (isV3) {
            LinkedList<String> urls = document.read(MessageFormat.format(INFO_JSON_URL_V3, index));
            infoJsonUrl = urls.get(0);
        } else {
            infoJsonUrl = document.read(MessageFormat.format(INFO_JSON_URL_V2, index));
        }
        if (!infoJsonUrl.toLowerCase().endsWith(".json"))
            infoJsonUrl = infoJsonUrl.replaceFirst("/?$", "/info.json");
        return infoJsonUrl;
    }

    public static String getSignatureFromManifestJson(String manifestJson) {
        List<Object> signature = parseJson(manifestJson).read(SIGNATURE_PATH);
        if (!signature.isEmpty()) {
            return signature.get(0).toString();
        }
        return null;
    }

    public DocumentContext parseJson(String manifestJson) {
        return JsonPath.using(conf).parse(manifestJson);
    }
}
