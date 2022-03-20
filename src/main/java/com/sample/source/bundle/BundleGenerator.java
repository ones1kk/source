package com.sample.source.bundle;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.FileSystemUtils;

@Slf4j
@RequiredArgsConstructor
public class BundleGenerator<T> {

    public static final String DYNAMIC_RESOURCE_DIRECTORY_NAME = "dynamic";

    private final String bundleName;
    private final Set<T> set;
    private final Map<Locale, Function<T, String>[]> converterMap;

    public static <T> Builder<T> builder(Bundle bundle, Set<T> set) {
        return new Builder<>(bundle.getBundleName(), set);
    }

    public List<File> make() {
        List<File> files = Collections.emptyList();
        if (this.converterMap.isEmpty()) {
            return files;
        }

        for (PackagingType type : PackagingType.values()) {
            if (type.isSupported()) {
                files = type.createProperties(this);
                break;
            }
        }

        return files;
    }

    private List<File> createProperties(Path resourcesDir) throws IOException {
        Path path = resourcesDir.resolve(DYNAMIC_RESOURCE_DIRECTORY_NAME).resolve(this.bundleName);

        // Clears all files in the bundle directory.
        FileSystemUtils.deleteRecursively(path.toFile());
        Files.createDirectories(path);

        List<File> files = new ArrayList<>();

        this.converterMap.forEach(((locale, functions) -> {
            Function<T, String> keyFunc = functions[0];
            Function<T, String> valFunc = functions[1];

            List<String> lines = this.set.stream()
                .map(it -> keyFunc.apply(it) + '=' + valFunc.apply(it))
                .sorted(String::compareTo).collect(toList());
            Path filePath = writeProperties(path, lines, locale);
            files.add(filePath.toFile());
        }));

        if (!files.isEmpty()) {
            log.info("Generated properties: {} to '{}'",
                files.stream().map(File::getName).collect(toList()), path);
        }

        return files;
    }

    @SneakyThrows
    private static Path writeProperties(Path path, Iterable<? extends CharSequence> lines,
        @Nullable Locale locale) {
        if (!Files.isWritable(path)) {
            throw new FileSystemException("Path is not writable: " + path);
        }

        String dirName = path.getFileName().toString();
        String filename = locale == null || locale == Locale.ROOT ? dirName + ".properties"
            : String.format("%s_%s.properties", dirName, locale);
        Path filePath = path.resolve(filename);

        Files.write(filePath, lines, StandardCharsets.UTF_8);

        return filePath;
    }

    @RequiredArgsConstructor
    @SuppressWarnings("unchecked")
    public static class Builder<T> {

        private final String bundleName;
        private final Set<T> set;
        private final Map<Locale, Function<T, String>[]> converterMap = new HashMap<>();

        public Builder<T> addLocale(Locale locale, Function<T, String> keyFunc,
            Function<T, String> valFunc) {
            this.converterMap.put(locale, new Function[]{keyFunc, valFunc});
            return this;
        }

        public Builder<T> addDefault(Function<T, String> keyFunc, Function<T, String> valFunc) {
            return addLocale(Locale.ROOT, keyFunc, valFunc);
        }

        public BundleGenerator<T> build() {
            return new BundleGenerator<>(bundleName, set, converterMap);
        }
    }

    private enum PackagingType {
        LOCAL {
            private final List<Path> segments = Stream.of("src", "main", "resources")
                .map(Paths::get).collect(toList());

            @Override
            boolean isSupported() {
                Path resourcesDir = getResourcesPath();
                if (resourcesDir == null) {
                    return false;
                }

                return Files.exists(resourcesDir);
            }

            @Override
            @SneakyThrows
            <T> List<File> createProperties(BundleGenerator<T> generator) {
                Path resourcesDir = getResourcesPath();
                if (resourcesDir == null) {
                    return Collections.emptyList();
                }

                List<File> files = WAR.createProperties(generator);
                files.addAll(generator.createProperties(resourcesDir));

                return files;
            }

            @SneakyThrows
            private Path getResourcesPath() {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL url = classLoader.getResource(".");
                if (url == null || !url.getProtocol().equals("file")) {
                    return null;
                }

                // Maven: {project}/target/classes/
                // Gradle: {project}/build/libs/
                Path projectPath = new File(url.toURI()).getParentFile().getParentFile().toPath();
                if (Files.notExists(projectPath)) {
                    return null;
                }
                projectPath = projectPath.toRealPath();

                return segments.stream().reduce(projectPath, Path::resolve);
            }
        },

        JAR {
            @Override
            boolean isSupported() {
                return false; // Not supported.
            }

            @Override
            @SneakyThrows
            <T> List<File> createProperties(BundleGenerator<T> generator) {
                return Collections.emptyList();
            }
        },

        WAR {
            @Override
            boolean isSupported() {
                return getResourcesPath() != null && !LOCAL.isSupported();
            }

            @Override
            @SneakyThrows
            <T> List<File> createProperties(BundleGenerator<T> generator) {
                Path resourcesDir = getResourcesPath();
                if (resourcesDir == null) {
                    return Collections.emptyList();
                }

                return generator.createProperties(resourcesDir);
            }

            @SneakyThrows
            private Path getResourcesPath() {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL url = classLoader.getResource(".");
                if (url == null || !url.getProtocol().equals("file")) {
                    return null;
                }

                return new File(url.toURI()).toPath();
            }
        };

        abstract boolean isSupported();

        abstract <T> List<File> createProperties(BundleGenerator<T> generator);
    }

}
