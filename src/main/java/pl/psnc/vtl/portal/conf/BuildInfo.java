package pl.psnc.vtl.portal.conf;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class BuildInfo {

    public static final BuildInfo instance = new BuildInfo();

    private Properties props = new Properties();

    private BuildInfo() {
        try {
            props = new Properties();
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(".buildinfo")) {
                props.load(is);
            }
        } catch (IOException | NullPointerException e) {
            log.error("BuildInfo failed to load. Was buildinfo.sh run?", e);
        }
    }

    /**
     * get commit short id
     *
     * @return
     */
    public String getCommitId() {
        return this.props.getProperty("commitId");
    }

    /**
     * get  build epoch time
     *
     * @return
     */
    public Long getBuildDate() {
        String bd = this.props.getProperty("buildDate");
        if (bd != null) {
            return Long.parseLong(bd);
        }
        return null;
    }


}
