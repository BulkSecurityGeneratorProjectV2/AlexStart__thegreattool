package com.sam.jcc.cloud.tool;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static java.nio.file.Files.setAttribute;
import static org.apache.commons.io.FileUtils.copyDirectory;

/**
 * @author Alexey Zhytnik
 * @since 21.11.2016
 */
@Component
public class FileManager {

    public File getFileByUri(String uri) {
        if (!uri.startsWith("file://")) {
            throw new RuntimeException("Wrong " + uri);
        }
        return new File(uri.substring(7));
    }

    public void delete(File dir) {
        if (dir.isDirectory()) {
            cleanDir(dir);
        }

        if (!dir.delete()) {
            throw new RuntimeException("Can't delete " + dir);
        }
    }

    public void cleanDir(File dir) {
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createHiddenDir(File file) {
        createDir(file);
        try {
            setAttribute(file.toPath(), "dos:hidden", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createDir(File file) {
        final boolean removed = !file.exists() || file.delete();

        if (!removed || !file.mkdir()) {
            throw new RuntimeException("Can't create folder " + file);
        }
    }

    public void copyDir(File src, File dest) {
        try {
            copyDirectory(src, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException("Can't create temp file!");
        }
    }

    public void write(byte[] content, File target) {
        try {
            Files.write(content, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
