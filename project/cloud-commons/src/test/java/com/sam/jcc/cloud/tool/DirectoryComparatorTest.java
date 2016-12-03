package com.sam.jcc.cloud.tool;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.sam.jcc.cloud.tool.TestFileUtils.createFolder;
import static com.sam.jcc.cloud.tool.TestFileUtils.fileWithRand;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexey Zhytnik
 * @since 02.12.2016
 */
public class DirectoryComparatorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    DirectoryComparator comparator = new DirectoryComparator();

    @Test
    public void comparesOnlyByFileContent() throws IOException {
        assertThat(comparator.areEquals(buildFileStructure(), buildFileStructure()));
    }

    File buildFileStructure() throws IOException {
        final File root = temp.newFolder();

        fileWithRand(new File(root, "1"));

        createFolder(new File(root, "a"));
        fileWithRand(new File(root, "a/1"));

        createFolder(new File(root, "a/b"));
        fileWithRand(new File(root, "a/b/1"));
        return root;
    }
}