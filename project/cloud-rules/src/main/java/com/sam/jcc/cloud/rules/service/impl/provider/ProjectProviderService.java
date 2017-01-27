/**
 * 
 */
package com.sam.jcc.cloud.rules.service.impl.provider;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sam.jcc.cloud.app.AppProvider;
import com.sam.jcc.cloud.i.app.IAppMetadata;
import com.sam.jcc.cloud.i.project.IProjectMetadata;
import com.sam.jcc.cloud.persistence.data.ProjectData;
import com.sam.jcc.cloud.persistence.data.ProjectDataRepository;
import com.sam.jcc.cloud.project.ProjectMetadata;
import com.sam.jcc.cloud.project.ProjectProvider;
import com.sam.jcc.cloud.rules.service.IService;

/**
 * @author olegk
 * 
 *         TODO
 *
 */
@Service
public class ProjectProviderService implements IService<IProjectMetadata> {

	@Autowired
	private AppProvider appProvider;

	@Autowired
	private List<ProjectProvider> projectProviders;

	@Autowired
	private ProjectDataRepository repository;

	@Override
	public IProjectMetadata create(IProjectMetadata projectMetadata) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProjectMetadata read(IProjectMetadata projectMetadata) {
		IProjectMetadata result = null;
		
		ProjectData findOne = repository.findOne(projectMetadata.getId());

		for (ProjectProvider projectProvider : projectProviders) {
			ProjectMetadata metadata = new ProjectMetadata();
			metadata.setId(findOne.getId());
			metadata.setProjectName(findOne.getName());
			metadata.setArtifactId(metadata.getName());
			metadata.setProjectType(projectProvider.getType());
			if ((result = projectProvider.read(metadata)) != null) {
				return result;
			}
		}
		return result;
	}

	@Override
	public IProjectMetadata update(IProjectMetadata projectMetadata) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(IProjectMetadata projectMetadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<? super IProjectMetadata> findAll() {
		List<? super IProjectMetadata> projects = new ArrayList<>();

		// 1. Iterate through providers
		Map<String, IProjectMetadata> projectsNamesToMetadata = new HashMap<>();
		for (ProjectProvider projectProvider : projectProviders) {
			for (IProjectMetadata projectMetadata : projectProvider.findAll()) {
				projectsNamesToMetadata.put(projectMetadata.getName(), projectMetadata);
			}
		}

		// 2. Iterate through all apps
		for (Object o : appProvider.findAll()) {
			IAppMetadata appMetadata = (IAppMetadata) o;
			IProjectMetadata projectMetadata = projectsNamesToMetadata.get(appMetadata.getProjectName());
			if (projectMetadata != null) {
				projects.add(projectMetadata);
			} else {
				IProjectMetadata emptyMetadata = new IProjectMetadata() {

					@Override
					public boolean hasSources() {
						return false;
					}

					@Override
					public String getName() {
						return appMetadata.getProjectName();
					}

					@Override
					public Long getId() {
						return appMetadata.getId();
					}

					@Override
					public boolean hasVCS() {
						return false;
					}

					@Override
					public boolean hasCI() {
						return false;
					}

					@Override
					public boolean hasDb() {
						// TODO Auto-generated method stub
						return false;
					}

				};
				projects.add(emptyMetadata);
			}
		}

		return projects;
	}

	@Override
	public IProjectMetadata create(Map<?, ?> props) {
		Long providerId = (Long) props.get("providerId");
		ProjectProvider targetProvider = projectProviders.stream().filter(p -> p.getId().equals(providerId)).findAny()
				.orElse(null);

		if (targetProvider != null) {
			ProjectMetadata metadata = new ProjectMetadata();
			metadata.setJavaVersion("1.8"); // TODO property or dynamic calc
			metadata.setGroupId("com.samsolutions"); // TODO property
			metadata.setArtifactId((String) props.get("projectName"));
			metadata.setProjectType(targetProvider.getType());
			metadata.setBootVersion("1.4.3.RELEASE"); // TODO property or
														// dynamic calc

			metadata.setBasePackage(metadata.getGroupId() + "." + metadata.getArtifactId());
			metadata.setDependencies(singletonList("web"));

			metadata.setProjectName(metadata.getArtifactId());
			metadata.setVersion("0.0.1-SNAPSHOT"); // TODO property
			metadata.setWebAppPackaging(false);
			metadata.setDescription("Project generated by SaM Java Cloud"); // TODO
																			// property
			IProjectMetadata updated = targetProvider.update(metadata);

			return updated;
		}

		return null;
	}

	@Override
	public void delete(Map<String, String> props) {
		// TODO Auto-generated method stub

	}

	@Override
	public IProjectMetadata update(Map<?, ?> props) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void findAndDelete(Map<String, String> props) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Long, String> getNames() {
		projectProviders.sort((p1, p2) -> {
			if (p1 != null && p2 != null && p1.getId() != null && p2.getId() != null) {
				return p1.getId().compareTo(p2.getId());
			}
			return 0;
		});

		Map<Long, String> names = new LinkedHashMap<>();
		projectProviders.forEach(p -> names.put(p.getId(), p.getI18NName()));

		return names;
	}

}
