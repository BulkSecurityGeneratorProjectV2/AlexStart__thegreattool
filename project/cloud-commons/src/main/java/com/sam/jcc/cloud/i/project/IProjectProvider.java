package com.sam.jcc.cloud.i.project;

import com.sam.jcc.cloud.i.IProvider;

public interface IProjectProvider extends IProvider<IProjectMetadata> {

    String getName(IProjectMetadata metadata);
}
