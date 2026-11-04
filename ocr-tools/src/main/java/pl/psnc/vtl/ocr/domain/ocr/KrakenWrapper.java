package pl.psnc.vtl.ocr.domain.ocr;

import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.ocr.api.ocr.VerseBboxesDto.VerseBboxDto;
import pl.psnc.vtl.ocr.domain.ocr.exception.OcrUnsupportedLanguageException;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * OCR wrapper around the kraken CLI.
 * <p>
 * Pages are 1-based; {@code null} means the first page. Region coordinates are
 * pixel coordinates of the page image at its native resolution (for PDFs:
 * rasterized at {@link Config#pdfDensity()}), with x2/y2 exclusive.
 */
@Slf4j
public class KrakenWrapper implements OcrWrapper {

    private static final Set<String> PASSTHROUGH_EXTENSIONS = Set.of("jpg", "jpeg", "png", "tif", "tiff");
    private static final Set<String> PASSTHROUGH_FORMATS = Set.of("JPEG", "PNG", "TIFF");
    private static final Set<String> PASSTHROUGH_COLORSPACES = Set.of("sRGB", "RGB", "Gray");
    private static final Set<String> PASSTHROUGH_DEPTHS = Set.of("1", "8");
    /** IM6 reports False, IM7 reports Undefined for images without alpha. */
    private static final Set<String> NO_ALPHA = Set.of("False", "Undefined");
    private static final Set<String> UPRIGHT_ORIENTATIONS = Set.of("Undefined", "TopLeft");

    private static final String IDENTIFY_PREFIX = "PAGE|";
    private static final String IDENTIFY_FORMAT =
            IDENTIFY_PREFIX + "%m|%A|%z|%[colorspace]|%[orientation]\\n";

    /**
     * hOCR title properties holding absolute x/y coordinate pairs that must be
     * shifted to page space.
     */
    private static final Set<String> COORDINATE_PROPERTIES = Set.of("bbox", "x_bboxes");

    /**
     * Max regions per kraken invocation. Keeps the command line within OS limits
     * (relative file names, roughly 40 chars per region, stay well below the 32K
     * limit on Windows).
     */
    private static final int REGION_BATCH_SIZE = 500;

    /**
     * Chars of process output kept at the beginning and at the end when
     * abbreviating.
     */
    private static final int OUTPUT_KEEP_CHARS = 1000;

    private final Path modelPath;
    private final boolean isHocr;
    private final Config config;
    private final String formatFlag;
    private final String outputExtension;

    public KrakenWrapper(Path modelPath, boolean isHocr) {
        this(modelPath, isHocr, Config.defaults());
    }

    public KrakenWrapper(Path modelPath, boolean isHocr, Config config) {
        Objects.requireNonNull(modelPath, "modelPath");
        if (!Files.isRegularFile(modelPath))
            throw new OcrUnsupportedLanguageException("Kraken model not found: " + modelPath);
        this.modelPath = modelPath;
        this.isHocr = isHocr;
        this.config = Objects.requireNonNull(config, "config");
        this.formatFlag = isHocr ? "--hocr" : "--native";
        this.outputExtension = isHocr ? ".hocr" : ".txt";
    }

    // ------------------------------------------------------------------ full page

    @Override
    public String doOcr(Path imagePath, Integer page) throws IOException {
        int pageNumber = validate(imagePath, page);
        return inWorkDir(workDir -> {
            Path input = preparePage(imagePath, pageNumber, workDir);
            String outputName = "page" + outputExtension;

            List<String> command = krakenBase();
            command.addAll(List.of(
                    "--input", processPath(input, workDir), outputName,
                    "segment", "--baseline",
                    "ocr", "--model", modelPath.toString()));
            runProcess(command, workDir, false);

            Path output = workDir.resolve(outputName);
            if (!Files.exists(output))
                throw new IOException("Kraken produced no output for " + imagePath);
            return readText(output);
        });
    }

    // ------------------------------------------------------------------ regions

    /**
     * OCRs each region as a single line (no segmentation).
     * @return per-region results in input order: plain text, or in hOCR mode the
     *         {@code ocr_line} element(s) with coordinates translated to page
     *         space. Empty string if the region lies outside the page or kraken
     *         produced nothing for it.
     */
    @Override
    public List<String> doOcr(Path imagePath, Integer page, List<VerseBboxDto> regions) throws IOException {
        if (regions == null || regions.isEmpty())
            return List.of();
        int pageNumber = validate(imagePath, page);
        return inWorkDir(workDir -> {
            List<Crop> crops = writeCrops(imagePath, pageNumber, regions, workDir);

            List<Integer> valid = new ArrayList<>(regions.size());
            for (int i = 0; i < crops.size(); i++)
                if (crops.get(i) != null)
                    valid.add(i);

            for (int start = 0; start < valid.size(); start += REGION_BATCH_SIZE) {
                List<String> command = krakenBase();
                for (int i : valid.subList(start, Math.min(start + REGION_BATCH_SIZE, valid.size())))
                    command.addAll(List.of("--input", regionInputName(i), regionOutputName(i)));
                command.addAll(List.of("ocr", "--model", modelPath.toString(), "--no-segmentation"));
                runProcess(command, workDir, false);
            }

            List<String> results = new ArrayList<>(regions.size());
            for (int i = 0; i < regions.size(); i++) {
                Crop crop = crops.get(i);
                if (crop == null) {
                    results.add("");
                    continue;
                }
                Path output = workDir.resolve(regionOutputName(i));
                if (!Files.exists(output)) {
                    log.warn("Kraken produced no output for region {}", regions.get(i).getId());
                    results.add("");
                    continue;
                }
                String content = readText(output);
                results.add(isHocr ? toPageHocr(content, crop) : content.strip());
            }
            return results;
        });
    }

    // ------------------------------------------------------------------ page
    // preparation

    /** Top-left corner of a region crop in page coordinates. */
    private static final class Crop {
        final int x;
        final int y;

        Crop(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /** Result of {@code identify} for a single frame. */
    private static final class FrameInfo {
        final String format;
        final String alpha;
        final String depth;
        final String colorspace;
        final String orientation;

        FrameInfo(String format, String alpha, String depth, String colorspace, String orientation) {
            this.format = format;
            this.alpha = alpha;
            this.depth = depth;
            this.colorspace = colorspace;
            this.orientation = orientation;
        }

        boolean canPassThrough(boolean autoOrient) {
            return PASSTHROUGH_FORMATS.contains(format)
                    && NO_ALPHA.contains(alpha)
                    && PASSTHROUGH_DEPTHS.contains(depth)
                    && PASSTHROUGH_COLORSPACES.contains(colorspace)
                    && (!autoOrient || UPRIGHT_ORIENTATIONS.contains(orientation));
        }

        @Override
        public String toString() {
            return String.join("|", format, alpha, depth, colorspace, orientation);
        }
    }

    /**
     * Returns the original file if kraken can read it directly, otherwise an
     * extracted PNG in workDir.
     */
    private Path preparePage(Path imagePath, int pageNumber, Path workDir) throws IOException {
        if (canPassThrough(imagePath, pageNumber, workDir)) {
            log.debug("Passing {} directly to kraken", imagePath);
            return imagePath;
        }
        return extractPage(imagePath, pageNumber, workDir);
    }

    private BufferedImage loadPageImage(Path imagePath, int pageNumber, Path workDir) throws IOException {
        Path prepared = preparePage(imagePath, pageNumber, workDir);
        BufferedImage image = readImage(prepared);
        if (image == null && prepared.equals(imagePath)) {
            log.debug("ImageIO cannot decode {}, falling back to extraction", imagePath);
            prepared = extractPage(imagePath, pageNumber, workDir);
            image = readImage(prepared);
        }
        if (image == null)
            throw new IOException("Cannot decode page image " + prepared);
        return image;
    }

    /**
     * True if the file is a single-frame JPEG/PNG/TIFF that both Pillow and ImageIO
     * read correctly without any conversion.
     */
    private boolean canPassThrough(Path imagePath, int pageNumber, Path workDir) {
        if (pageNumber != 1 || !PASSTHROUGH_EXTENSIONS.contains(extension(imagePath)))
            return false;

        List<FrameInfo> frames;
        try {
            frames = identify(imagePath, workDir);
        } catch (IOException e) {
            log.debug("identify failed for {}, will extract page", imagePath, e);
            return false;
        }
        if (frames.size() != 1)
            return false;

        FrameInfo frame = frames.get(0);
        boolean ok = frame.canPassThrough(config.autoOrient());
        if (!ok)
            log.debug("{} needs conversion: {}", imagePath, frame);
        return ok;
    }

    /** Returns one entry per frame. */
    private List<FrameInfo> identify(Path imagePath, Path workDir) throws IOException {
        List<String> command = new ArrayList<>(config.identifyCommand());
        command.addAll(List.of("-ping", "-format", IDENTIFY_FORMAT, processPath(imagePath, workDir) + "[0-1]"));
        String output = runProcess(command, workDir, true);

        List<FrameInfo> frames = new ArrayList<>();
        for (String line : output.split("\\R")) {
            if (!line.startsWith(IDENTIFY_PREFIX))
                continue;
            String[] p = line.substring(IDENTIFY_PREFIX.length()).trim().split("\\|", -1);
            if (p.length != 5)
                throw new IOException("Unexpected identify output: " + line);
            frames.add(new FrameInfo(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim()));
        }
        if (frames.isEmpty())
            throw new IOException("Empty identify output");
        return frames;
    }

    private Path extractPage(Path imagePath, int pageNumber, Path workDir) throws IOException {
        String outputName = "page.png";
        List<String> command = new ArrayList<>(config.convertCommand());
        command.addAll(List.of(
                "-density", String.valueOf(config.pdfDensity()),
                processPath(imagePath, workDir) + "[" + (pageNumber - 1) + "]"));
        if (config.autoOrient())
            command.add("-auto-orient");
        command.addAll(List.of(
                "-background", "white", "-alpha", "remove", "-alpha", "off",
                "-depth", "8",
                "-define", "png:compression-level=1",
                outputName));
        runProcess(command, workDir, false);

        Path output = workDir.resolve(outputName);
        if (!Files.isRegularFile(output))
            throw new IOException("ImageMagick produced no output for " + imagePath + " page " + pageNumber);
        return output;
    }

    private static BufferedImage readImage(Path path) {
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException | RuntimeException e) {
            log.debug("ImageIO failed to read {}", path, e);
            return null;
        }
    }

    // ------------------------------------------------------------------ regions
    // helpers

    /**
     * Writes one PNG per region. The full page image is only referenced inside this
     * method, so it becomes collectable before kraken runs.
     * @return crop offsets in input order; {@code null} for regions outside the
     *         page
     */
    private List<Crop> writeCrops(Path imagePath, int pageNumber, List<VerseBboxDto> regions, Path workDir)
            throws IOException {
        BufferedImage image = loadPageImage(imagePath, pageNumber, workDir);
        List<Crop> crops = new ArrayList<>(regions.size());
        for (int i = 0; i < regions.size(); i++)
            crops.add(cropRegion(image, regions.get(i), workDir.resolve(regionInputName(i))));
        return crops;
    }

    /** @return crop offset, or {@code null} if the region lies outside the image */
    private Crop cropRegion(BufferedImage image, VerseBboxDto region, Path output) throws IOException {
        int padding = config.regionPadding();
        int x1 = Math.max(0, Math.min(region.getX1(), region.getX2()) - padding);
        int y1 = Math.max(0, Math.min(region.getY1(), region.getY2()) - padding);
        int x2 = Math.min(image.getWidth(), Math.max(region.getX1(), region.getX2()) + padding);
        int y2 = Math.min(image.getHeight(), Math.max(region.getY1(), region.getY2()) + padding);
        if (x2 <= x1 || y2 <= y1) {
            log.warn("Region {} is outside image bounds {}x{}, skipping",
                    region.getId(), image.getWidth(), image.getHeight());
            return null;
        }

        BufferedImage sub = image.getSubimage(x1, y1, x2 - x1, y2 - y1);
        if (!ImageIO.write(sub, "png", output.toFile()))
            throw new IOException("No PNG writer for image type " + image.getType()
                    + " (region " + region.getId() + ")");
        return new Crop(x1, y1);
    }

    private static String regionInputName(int index) {
        return "region_" + index + ".png";
    }

    private String regionOutputName(int index) {
        return "region_" + index + outputExtension;
    }

    /**
     * Extracts the ocr_line element(s) from a kraken hOCR document, shifts all
     * coordinate properties ({@code bbox}, {@code x_bboxes}) from crop to page
     * coordinates and wraps the lines in ocr_page / ocr_carea / ocr_par elements.
     * Baselines are line-relative and stay as is.
     * @return the ocr_page element, or an empty string if kraken found no lines
     */
    private static String toPageHocr(String hocr, Crop crop) {
        Elements lines = Jsoup.parse(hocr).select(".ocr_line");
        if (lines.isEmpty())
            return "";

        for (Element element : lines.select("[title]")) // the lines themselves and all descendants
            element.attr("title", shiftTitle(element.attr("title"), crop));

        String bbox = unionBbox(lines);

        Element page = new Element("div")
                .addClass("ocr_page")
                .attr("id", "page_1")
                .attr("title", "image \"unknown\"; bbox " + bbox + "; ppageno 0");
        Element carea = page.appendElement("div")
                .addClass("ocr_carea")
                .attr("id", "block_1_1")
                .attr("title", "bbox " + bbox);
        Element par = carea.appendElement("p")
                .addClass("ocr_par")
                .attr("id", "par_1_1")
                .attr("title", "bbox " + bbox);

        int lineNo = 1;
        int wordNo = 1;
        for (Element line : lines) {
            line.attr("id", "line_1_" + lineNo++);
            for (Element word : line.select(".ocrx_word"))
                word.attr("id", "word_1_" + wordNo++);
            par.appendChild(line); // moves the line out of the parsed document
        }
        return page.outerHtml();
    }

    /**
     * @return "x1 y1 x2 y2" enclosing all line bboxes, or "0 0 0 0" if none is
     *         parseable
     */
    private static String unionBbox(Elements lines) {
        int x1 = Integer.MAX_VALUE, y1 = Integer.MAX_VALUE;
        int x2 = Integer.MIN_VALUE, y2 = Integer.MIN_VALUE;
        boolean found = false;
        for (Element line : lines) {
            int[] b = parseBbox(line.attr("title"));
            if (b == null)
                continue;
            found = true;
            x1 = Math.min(x1, b[0]);
            y1 = Math.min(y1, b[1]);
            x2 = Math.max(x2, b[2]);
            y2 = Math.max(y2, b[3]);
        }
        return found ? x1 + " " + y1 + " " + x2 + " " + y2 : "0 0 0 0";
    }

    /** @return {x1, y1, x2, y2} from the title's bbox property, or {@code null} */
    private static int[] parseBbox(String title) {
        for (String property : title.split(";")) {
            String[] tokens = property.strip().split("\\s+");
            if (tokens.length == 5 && tokens[0].equals("bbox")) {
                try {
                    return new int[] {
                        Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]),
                        Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]) };
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /** title = "bbox 1 2 3 4; baseline 0.01 -5; x_bboxes ..." */
    private static String shiftTitle(String title, Crop crop) {
        return Arrays.stream(title.split(";"))
                .map(String::strip)
                .filter(property -> !property.isEmpty())
                .map(property -> shiftProperty(property, crop))
                .collect(Collectors.joining("; "));
    }

    private static String shiftProperty(String property, Crop crop) {
        String[] tokens = property.split("\\s+");
        if (!COORDINATE_PROPERTIES.contains(tokens[0]))
            return property;
        try {
            for (int i = 1; i < tokens.length; i++)
                tokens[i] = String.valueOf(Integer.parseInt(tokens[i]) + ((i - 1) % 2 == 0 ? crop.x : crop.y));
        } catch (NumberFormatException e) {
            log.warn("Cannot shift hOCR property '{}', leaving unchanged", property);
            return property;
        }
        return String.join(" ", tokens);
    }

    // ------------------------------------------------------------------
    // infrastructure

    @FunctionalInterface
    private interface WorkDirTask<T> {
        T run(Path workDir) throws IOException;
    }

    /**
     * Runs the task in a fresh temp directory, deletes it afterwards, wraps
     * IOExceptions.
     */
    private static <T> T inWorkDir(WorkDirTask<T> task) throws IOException {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("kraken");
            return task.run(workDir);
        } finally {
            try {
                FileUtils.deleteDirectory(workDir.toFile());
            } catch (IOException e) {
                log.warn("Failed to delete workDir " + workDir, e);
            }
        }
    }

    private List<String> krakenBase() {
        return new ArrayList<>(List.of(config.krakenBinary(), formatFlag));
    }

    /**
     * Path convention for external commands (which run with workDir as the working
     * directory): files inside workDir are passed by relative name, everything else
     * as an absolute path.
     */
    private static String processPath(Path path, Path workDir) {
        Path absolute = path.toAbsolutePath().normalize();
        Path absoluteWorkDir = workDir.toAbsolutePath().normalize();
        return absolute.startsWith(absoluteWorkDir)
                ? absoluteWorkDir.relativize(absolute).toString()
                : absolute.toString();
    }

    private static int validate(Path imagePath, Integer page) {
        if (imagePath == null || !Files.isRegularFile(imagePath))
            throw new IllegalArgumentException("Image file not found: " + imagePath);
        int pageNumber = page == null ? 1 : page;
        if (pageNumber < 1)
            throw new IllegalArgumentException("Page must be >= 1, got " + page);
        return pageNumber;
    }

    private static String extension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** Lenient UTF-8 read: malformed bytes are replaced instead of throwing. */
    private static String readText(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    /**
     * Keeps the first and the last {@value #OUTPUT_KEEP_CHARS} chars of long
     * process output.
     */
    private static String abbreviate(String text) {
        if (text.length() <= 2 * OUTPUT_KEEP_CHARS)
            return text;
        int omitted = text.length() - 2 * OUTPUT_KEEP_CHARS;
        return text.substring(0, OUTPUT_KEEP_CHARS)
                + "\n... [" + omitted + " chars omitted] ...\n"
                + text.substring(text.length() - OUTPUT_KEEP_CHARS);
    }

    /**
     * Runs a process with stdout/stderr redirected to files (no pipe deadlocks,
     * reliable timeout).
     * @param captureStdout whether the caller needs stdout; otherwise it is only
     *                      read for debug logging
     * @return stdout if captured, otherwise possibly an empty string
     */
    private String runProcess(List<String> command, Path workDir, boolean captureStdout) throws IOException {
        Path stdoutFile = Files.createTempFile(workDir, "proc", ".out");
        Path stderrFile = Files.createTempFile(workDir, "proc", ".err");
        ProcessBuilder pb = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .redirectOutput(stdoutFile.toFile())
                .redirectError(stderrFile.toFile());
        String name = command.get(0);
        log.debug("Running command: {}", command);

        Process process = pb.start();
        try {
            if (!process.waitFor(config.processTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                killTree(process);
                throw new IOException("Process timed out after " + config.processTimeout() + ": " + name);
            }
        } catch (InterruptedException e) {
            killTree(process);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for " + name, e);
        }

        String stdout = captureStdout || log.isDebugEnabled() ? readText(stdoutFile) : "";
        String stderr = readText(stderrFile);
        if (!stdout.isBlank())
            log.debug("{} stdout: {}", name, abbreviate(stdout));
        if (!stderr.isBlank())
            log.debug("{} stderr: {}", name, abbreviate(stderr));

        if (process.exitValue() != 0) {
            if (stdout.isBlank())
                stderr = readText(stdoutFile);
            throw new IOException(name + " exited with code " + process.exitValue() + ": " + abbreviate(stderr));
        }
        return stdout;
    }

    /**
     * Kills the process and all its descendants (kraken may spawn workers,
     * ImageMagick may spawn Ghostscript). Waits briefly so that open files are
     * released before the work dir is deleted.
     */
    private static void killTree(Process process) {
        List<ProcessHandle> descendants = process.descendants().collect(Collectors.toList());
        process.destroyForcibly();
        descendants.forEach(ProcessHandle::destroyForcibly);
        try {
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** External tool configuration. Immutable. */
    @Value
    @Accessors(fluent = true)
    public static final class Config {
        private final List<String> convertCommand;
        private final List<String> identifyCommand;
        private final String krakenBinary;
        private final int pdfDensity;
        private final int regionPadding;
        private final boolean autoOrient;
        private final Duration processTimeout;
    
        /**
         * @param convertCommand  ImageMagick convert command: {@code [magick]} for IM7,
         *                        {@code [convert]} for IM6
         * @param identifyCommand ImageMagick identify command:
         *                        {@code [magick, identify]} for IM7, {@code [identify]}
         *                        for IM6
         * @param krakenBinary    kraken executable
         * @param pdfDensity      DPI used to rasterize PDFs; must match the DPI the
         *                        region bboxes were produced at
         * @param regionPadding   margin in pixels added around each region crop
         * @param autoOrient      apply EXIF orientation; enable only if bboxes refer to
         *                        EXIF-rotated images
         * @param processTimeout  timeout for every external process
         */
        public Config(List<String> convertCommand,
                List<String> identifyCommand,
                String krakenBinary,
                int pdfDensity,
                int regionPadding,
                boolean autoOrient,
                Duration processTimeout) {
            this.convertCommand = nonEmpty(convertCommand, "convertCommand");
            this.identifyCommand = nonEmpty(identifyCommand, "identifyCommand");
            this.krakenBinary = Objects.requireNonNull(krakenBinary, "krakenBinary");
            if (pdfDensity <= 0)
                throw new IllegalArgumentException("pdfDensity must be > 0, got " + pdfDensity);
            if (regionPadding < 0)
                throw new IllegalArgumentException("regionPadding must be >= 0, got " + regionPadding);
            Objects.requireNonNull(processTimeout, "processTimeout");
            if (processTimeout.isNegative() || processTimeout.isZero())
                throw new IllegalArgumentException("processTimeout must be positive, got " + processTimeout);
            this.pdfDensity = pdfDensity;
            this.regionPadding = regionPadding;
            this.autoOrient = autoOrient;
            this.processTimeout = processTimeout;
        }
    
        public static Config defaults() {
            return new Config(List.of("convert"), List.of("identify"), "kraken",
                    300, 4, false, Duration.ofMinutes(10));
        }
    
        private static List<String> nonEmpty(List<String> list, String name) {
            Objects.requireNonNull(list, name);
            if (list.isEmpty())
                throw new IllegalArgumentException(name + " must not be empty");
            return List.copyOf(list);
        }
    
        @Override
        public String toString() {
            return "Config[convertCommand=" + convertCommand + ", identifyCommand=" + identifyCommand
                    + ", krakenBinary=" + krakenBinary + ", pdfDensity=" + pdfDensity
                    + ", regionPadding=" + regionPadding + ", autoOrient=" + autoOrient
                    + ", processTimeout=" + processTimeout + "]";
        }
    }
}