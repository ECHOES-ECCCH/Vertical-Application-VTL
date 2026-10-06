package pl.psnc.vtl.iiif.converters;

import pl.psnc.vtl.iiif.utils.ConversionSettings;

import java.util.List;

public abstract class BaseConverter implements Converter {

    abstract protected List<String> doConversion(ConversionSettings conversionSettings)
            throws ConversionException;


    protected void basicValidation(ConversionSettings conversionSettings)
            throws ConversionException {
        conversionSettings.haveProperPaths();
    }


    @Override
    public List<String> convert(ConversionSettings conversionSettings)
            throws ConversionException {
        basicValidation(conversionSettings);
        List<String> filesNames = doConversion(conversionSettings);
        return  filesNames;
    }

}
