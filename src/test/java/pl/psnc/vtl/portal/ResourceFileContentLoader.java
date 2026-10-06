package pl.psnc.vtl.portal;

import lombok.SneakyThrows;

public class ResourceFileContentLoader {

      @SneakyThrows
      public static String loadFileContentAsString(String fileName){
        return new String(loadFileContentAsBytes(fileName));
    }

    @SneakyThrows
    public static byte[] loadFileContentAsBytes(String fileName){
        return ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(fileName).readAllBytes();
    }

}
