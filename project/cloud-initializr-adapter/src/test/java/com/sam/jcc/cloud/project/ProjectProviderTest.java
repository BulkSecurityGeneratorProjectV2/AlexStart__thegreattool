package com.sam.jcc.cloud.project;

import static com.sam.jcc.cloud.project.ProjectMetadataHelper.emptyProject;
import static com.sam.jcc.cloud.project.ProjectMetadataHelper.gradleProject;
import static com.sam.jcc.cloud.project.ProjectMetadataHelper.mavenProject;
import static com.sam.jcc.cloud.project.ProjectStatus.POST_PROCESSED;
import static com.sam.jcc.cloud.project.ProjectStatus.PRE_PROCESSED;
import static com.sam.jcc.cloud.project.ProjectStatus.PROCESSED;
import static com.sam.jcc.cloud.project.ProjectStatus.UNPROCESSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sam.jcc.cloud.i.project.IProjectMetadata;
import com.sam.jcc.cloud.project.impl.GradleProjectProvider;
import com.sam.jcc.cloud.project.impl.MavenProjectProvider;
import com.sam.jcc.cloud.provider.UnsupportedTypeException;

/**
 * @author Alexey Zhytnik
 * @since 15.11.2016
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Ignore
public class ProjectProviderTest {

	@Autowired
	MavenProjectProvider mavenProvider;

	@Autowired
	GradleProjectProvider gradleProvider;

	@After
    public void reset(){
	    mavenProvider
                .findAll()
                .forEach(mavenProvider::delete);
	    gradleProvider
                .findAll()
                .forEach(gradleProvider::delete);
    }

	@Test
	public void getsName() {
		ProjectMetadata m = mavenProject();
		assertThat(mavenProvider.getName(m)).isEqualTo(m.getGroupId() + ":" + m.getArtifactId());
		m = gradleProject();
		assertThat(gradleProvider.getName(m)).isEqualTo(m.getGroupId() + ":" + m.getArtifactId());
	}

	@Test
	public void getsI18NName() {
		assertThat(mavenProvider.getI18NName()).isNotEmpty();
		assertThat(gradleProvider.getI18NName()).isNotEmpty();
	}

	@Test
	public void getsDescription() {
		assertThat(mavenProvider.getI18NDescription()).isNotEmpty();
		assertThat(gradleProvider.getI18NDescription()).isNotEmpty();
	}

	@Test
	public void supports() {
		assertThat(mavenProvider.supports(mavenProject())).isTrue();
		assertThat(mavenProvider.supports(gradleProject())).isFalse();
		assertThat(gradleProvider.supports(mavenProject())).isFalse();
		assertThat(gradleProvider.supports(gradleProject())).isTrue();
	}

	@Test(expected = ProjectAlreadyExistException.class)
	public void failsOnDuplicate() {
		final ProjectMetadata project = mavenProject();
		mavenProvider.create(project);
		mavenProvider.create(project);
	}

    @Test(expected = ProjectNotFoundException.class)
    public void failsOnReadUnknown() {
        mavenProvider.read(mavenProject());
    }

    @Test(expected = ProjectNotFoundException.class)
    public void failsOnUpdateUnknown() {
        gradleProvider.update(gradleProject());
    }

	@Test(expected = ProjectNotFoundException.class)
	public void failsOnDeleteUnknown() {
		gradleProvider.delete(gradleProject());
	}

	@Test
	public void changesMavenProjectStatus() {
		final ProjectMetadata project = spy(mavenProject());
		mavenProvider.create(project);

		final InOrder order = inOrder(project);
		order.verify(project).setStatus(UNPROCESSED);
		order.verify(project).setStatus(PRE_PROCESSED);
		order.verify(project).setStatus(PROCESSED);
		order.verify(project).setStatus(POST_PROCESSED);
	}

	@Test
	public void changesGradleProjectStatus() {
		final ProjectMetadata project = spy(gradleProject());
		gradleProvider.create(project);

		final InOrder order = inOrder(project);
		order.verify(project).setStatus(UNPROCESSED);
		order.verify(project).setStatus(PRE_PROCESSED);
		order.verify(project).setStatus(PROCESSED);
		order.verify(project).setStatus(POST_PROCESSED);
	}

	@Test
	public void isAlwaysEnabled() {
		assertThat(mavenProvider.isEnabled()).isTrue();
		assertThat(gradleProvider.isEnabled()).isTrue();
	}

	@Test(expected = UnsupportedTypeException.class)
	public void failsWithUnknownProjectTypeMaven() {
		assertThat(mavenProvider.create(emptyProject())).isNotNull();
	}

	@Test(expected = UnsupportedTypeException.class)
	public void failsWithUnknownProjectTypeGradle() {
		assertThat(gradleProvider.create(emptyProject())).isNotNull();
	}

	@Test
	public void processMaven() {
		final ProjectMetadata metadata = asProjectMetadata(mavenProvider.process(mavenProject()));
		assertThat(metadata.getStatus()).isEqualTo(PROCESSED);
		mavenProvider.postprocess(metadata);
	}

	@Test
	public void processGradle() {
		final ProjectMetadata metadata = asProjectMetadata(gradleProvider.process(gradleProject()));
		assertThat(metadata.getStatus()).isEqualTo(PROCESSED);
		gradleProvider.postprocess(metadata);
	}

	ProjectMetadata asProjectMetadata(IProjectMetadata metadata) {
		return (ProjectMetadata) metadata;
	}
}
