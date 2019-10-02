/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.cloud.workbench.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.guvnor.rest.client.ProjectResponse;
import org.kie.wb.test.rest.client.RestWorkbenchClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

public class ProjectRunner {

    protected WorkbenchClient workbenchClient;

    public ProjectRunner(String url, String user, String password) {
        workbenchClient = RestWorkbenchClient.createWorkbenchClient(url, user, password, 120, 120, 120);
    }


    private static final String GROUP_ID = "org.kie.cloud.testing";
    private static final String VERSION = "1.0.0";
    
    public Callable<Collection<String>> createProjects(String spaceName, String projectName, int startSuffix, int retries) {
        return new Callable<Collection<String>>() {
            @Override
            public Collection<String> call() {
                if (workbenchClient.getSpace(spaceName) == null) {
                    throw new RuntimeException("Space " + spaceName + " not founded.");
                }
                List<String> createdProjects = new ArrayList<>(retries);
                for (int i = startSuffix; i < startSuffix + retries; i++) {
                    workbenchClient.createProject(spaceName, projectName + "-" + i, GROUP_ID, VERSION);
                    createdProjects.add(projectName + "-" + i);
                }
                return createdProjects;
            }
        };
    }

    public Callable<Collection<ProjectResponse>> getProjects(String spaceName) {
        return new Callable<Collection<ProjectResponse>>() {
            @Override
            public Collection<ProjectResponse> call() {
                return workbenchClient.getProjects(spaceName);
            }
        };
    }

    public Callable<Void> deleteProjects(String spaceName, Collection<String> projectNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                projectNames.forEach(name -> {
                    workbenchClient.deleteProject(spaceName, name);
                });
                return null;
            }
        };
    }


}