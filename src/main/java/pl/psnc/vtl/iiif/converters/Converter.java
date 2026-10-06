package pl.psnc.vtl.iiif.converters;

import pl.psnc.vtl.iiif.utils.ConversionSettings;

import java.util.List;

public interface Converter {

    List<String> allowedFormats();

    List<String> convert(ConversionSettings conversionSettings)
            throws ConversionException;
}
