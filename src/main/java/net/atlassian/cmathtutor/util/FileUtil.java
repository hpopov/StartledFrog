package net.atlassian.cmathtutor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

    public static void emptyDirectory(@NonNull File directory, FileVisitOption... fileVisitOptions) {
	if (false == directory.isDirectory()) {
	    throw new IllegalArgumentException("Specified File must be a directory");
	}
	Path baseDirPath = directory.toPath();
	try (Stream<Path> stream = Files.walk(baseDirPath, fileVisitOptions)) {
	    stream.sequential()
		    .filter(path -> !path.equals(baseDirPath))
		    .map(Path::toFile)
		    .sorted(Comparator.reverseOrder())
		    .forEach(File::delete);
	} catch (IOException e) {
	    log.error("Exception occured performing recursive file deletion in the folder {}:{}", directory, e);
	}
    }

    public static void createDirectory(File directory) {
	if (directory.exists()) {
	    return;
	}
	createDirectoryInternal(directory);
    }

    private static void createDirectoryInternal(File directory) {
	if (directory.getParentFile().exists()) {
	    log.debug("Parent file exists, created {} ? {}", directory.getAbsolutePath(), directory.mkdir());
	    return;
	}
	createDirectory(directory.getParentFile());
	log.debug("After parent had been created, created {} ? {}", directory.getAbsolutePath(), directory.mkdir());
    }
}
