package com.sam.jcc.cloud.utils.files;

import static com.sam.jcc.cloud.utils.SystemUtils.isWindowsOS;
import static java.nio.file.Files.setAttribute;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.commons.io.filefilter.FileFileFilter.FILE;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.sam.jcc.cloud.PropertyResolver;
import com.sam.jcc.cloud.i.OSDependent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;
import com.sam.jcc.cloud.exception.InternalCloudException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexey Zhytnik
 * @since 21.11.2016
 */
@Slf4j
@Component
public class FileManager {

	private final String fileProtocol = PropertyResolver.getProperty("protocols.file");

	public File getFileByUri(String uri) {
		if (!uri.startsWith(fileProtocol)) {
			log.error("File URI can't start with {}", uri);
			throw new InternalCloudException();
		}
		return new File(uri.substring(fileProtocol.length()));
	}

	public void delete(File dir) {
		if (dir.isDirectory()) {
			cleanDir(dir);
		}

		if (!dir.delete()) {
			log.error("Can't delete {}", dir);
			throw new InternalCloudException();
		}
	}

	public void cleanDir(File dir) {
		try {
			FileUtils.cleanDirectory(dir);
		} catch (IOException e) {
			throw new InternalCloudException(e);
		}
	}

	@OSDependent(
	        "Currently supported only for Windows OS, " +
            "Unix hidden files must to start with '.'"
    )
	public void createHiddenDir(File file) {
		createDir(file);
		if (!isWindowsOS()) return;

		try {
			setAttribute(file.toPath(), "dos:hidden", true);
		} catch (IOException e) {
			log.warn("Maybe OS doesn't support this way of creation of hidden directory, {0}", e);
		}
	}

	public void createDir(File file) {
		final boolean removed = !file.exists() || file.delete();

		if (!removed || !file.mkdir()) {
			log.error("Can't create folder {}", file);
			throw new InternalCloudException();
		}
	}

	public void copyDir(File src, File dest) {
		log.info("Copy from {} to {}", src, dest);
		try {
			copyDirectory(src, dest);
		} catch (IOException e) {
			throw new InternalCloudException(e);
		}
	}

	public TempFile createTempDir() {
		return new TempFile(Files.createTempDir());
	}

	public TempFile createTempFile(String prefix, String suffix) {
		try {
			final File temp = File.createTempFile(prefix, suffix);
			return new TempFile(temp);
		} catch (IOException e) {
			throw new InternalCloudException(e);
		}
	}

	public void write(byte[] content, File target) {
		try {
			Files.write(content, target);
		} catch (IOException e) {
			throw new InternalCloudException(e);
		}
	}

	public File getResource(Class<?> clazz, String path) {
		try {
			final ClassPathResource resource = new ClassPathResource(path, clazz);
			return resource.getFile();
		} catch (IOException e) {
			throw new InternalCloudException(e);
		}
	}

	List<File> getDirectoryFiles(File dir) {
		return (List<File>) listFiles(dir, FILE, DIRECTORY);
	}

	int getNesting(File file) {
		try {
			final String path = file.getCanonicalPath();
			return StringUtils.countMatches(path, '/');
		} catch (IOException e) {
			throw new InternalCloudException(e);
		}
	}
}
