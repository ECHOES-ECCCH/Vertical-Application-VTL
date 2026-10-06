package pl.psnc.vtl.iiif.converters;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConverterFactory {

    private final List<Converter> allConverters;


    public Converter getConverter(String filename)
            throws ConversionException {

        String fileExtension = FilenameUtils.getExtension(filename);
        if (fileExtension == null || fileExtension.isEmpty())
            throw new IllegalArgumentException("Cannot get file extension!");

        //try assign converter by input file extension
        Optional<Converter> potentialConverter = allConverters.stream()
                .filter(converter -> converter.allowedFormats().contains(fileExtension.toLowerCase())).findFirst();
        if (potentialConverter.isPresent())
            return potentialConverter.get();
        else
            throw new ConversionException("Cannot assign converter for file extension: " + fileExtension);
    }

}
