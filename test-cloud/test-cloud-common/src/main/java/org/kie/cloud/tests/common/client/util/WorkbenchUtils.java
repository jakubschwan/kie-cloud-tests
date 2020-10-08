/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.tests.common.client.util;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.NotFoundException;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.WaiterException;
import org.guvnor.rest.client.CloneProjectRequest;
import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.wb.test.rest.client.ClientRequestTimedOutException;
import org.kie.wb.test.rest.client.WorkbenchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchUtils {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchUtils.class);

    private static final String SPACE_NAME = "mySpace";

    private static final Duration WAIT_STEP = Duration.ofSeconds(1);
    private static final Duration MAX_WAIT_DURATION = Duration.ofSeconds(15);

    public static void deployProjectToWorkbench(String repositoryName,
                                                KieDeploymentScenario<?> deploymentScenario,
                                                String projectName) {
        GitProvider gitProvider = deploymentScenario.getGitProvider();
        WorkbenchDeployment workbenchDeployment = deploymentScenario.getWorkbenchDeployments().get(0);

        deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), workbenchDeployment, projectName);
    }

    public static void deployProjectToWorkbench(String repositoryUrl,
                                                WorkbenchDeployment workbenchDeployment,
                                                String projectName) {
        CloneProjectRequest cloneProjectRequest = createCloneProjectRequest(repositoryUrl, projectName);

        WorkbenchClient workbenchClient = WorkbenchClientProvider.getWorkbenchClient(workbenchDeployment);
        workbenchClient.createSpace(SPACE_NAME, workbenchDeployment.getUsername());
        for (int tries = 0; tries < 5; tries++) {
            try {
                logger.info("Cloning project. Try: " + tries);
                workbenchClient.cloneRepository(SPACE_NAME, cloneProjectRequest);
                logger.info("Project was cloned to the workbench after " + (tries + 1) + " attempts.");
                break;
            } catch (ClientRequestTimedOutException ex) {
                logger.warn("Caught exception during cloning repository to the Workbench. Waiting for 5 minutes to see if the project was created.", ex);
                try {
                    new SimpleWaiter(() -> workbenchClient.getProjects(SPACE_NAME).stream().map(
                                                                                                ProjectResponse::getName)
                                                          .anyMatch(projectName::equals)).reason("Waiting for "
                                                                  + projectName + " to be cloned into Workbench")
                                                                                         .timeout(TimeUnit.MINUTES, 2)
                                                                                         .interval(TimeUnit.SECONDS, 5)
                                                                                         .waitFor();
                    logger.info("Container after cloning timeout found");
                } catch (NotFoundException e) {
                    handleNotFoundException(workbenchClient, e);
                } catch (WaiterException e) {
                    handleWaiterException(tries, e);
                }
            }
        }
        workbenchClient.deployProject(SPACE_NAME, projectName);
    }

    private static CloneProjectRequest createCloneProjectRequest(String repositoryUrl, String projectName) {
        CloneProjectRequest cloneProjectRequest = new CloneProjectRequest();
        cloneProjectRequest.setGitURL(repositoryUrl);
        cloneProjectRequest.setName(projectName);
        return cloneProjectRequest;
    }

    private static void handleNotFoundException(WorkbenchClient workbenchClient, Exception originalException) {
        logger.debug("Exception handled when workbench client throws 404 when is tried to search for all spaces.");
        boolean workbenchWorks = false;
        for (int i = 0; i < 10; i++) {
            try {
                if (workbenchClient.getSpaces().stream().map(Space::getName).anyMatch(SPACE_NAME::equals)) {
                    logger.debug("Container URL works.");
                    workbenchWorks = true;
                    break;
                } else {
                    logger.debug("Container URL works, but space was not found. Waiting for 30 seconds and trying again.");
                    waitFor30Seconds();
                }
            } catch (NotFoundException ee) {
                logger.debug("Container URL do not work. Waiting for 30 seconds and trying again.", ee);
                waitFor30Seconds();
            }
        }
        if (workbenchWorks) {
            logger.info("Workbench client works. NotFoundException can be ignored.");
        } else {
            throw new RuntimeException("Workbench client does not works", originalException);
        }
    }

    private static void handleWaiterException(int tries, Exception originalException) {
        if (tries < 4) {
            logger.warn("Timeout while cloning project to the Workbench application.", originalException);
        } else {
            throw new RuntimeException("Timeout while cloning project to the Workbench application.",
                                       originalException);
        }
    }

    private static void waitFor30Seconds() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting.", e1);
        }
    }

    public static void waitForContainerRegistration(KieServerControllerClient kieControllerClient, String serverTemplate, String containerId) {
        TimeUtils.wait(MAX_WAIT_DURATION, WAIT_STEP, () -> {
            Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
            return containersSpec.stream().anyMatch(n -> n.getId().equals(containerId));
        });
    }

    public static void saveContainerSpec(KieServerControllerClient kieControllerClient, String serverTemplateId, String serverTemplateName, String containerId, String containerAlias, Kjar kjar, KieContainerStatus status) {
        saveContainerSpec(kieControllerClient, serverTemplateId, serverTemplateName, containerId, containerAlias, kjar, status, Collections.emptyMap());
    }

    public static void saveContainerSpec(KieServerControllerClient kieControllerClient, String serverTemplateId, String serverTemplateName, String containerId, String containerAlias, Kjar kjar, KieContainerStatus status, Map<Capability, ContainerConfig> configs) {
        ServerTemplateKey serverTemplateKey = new ServerTemplateKey(serverTemplateId, serverTemplateName);
        ReleaseId releasedId = new ReleaseId(kjar.getGroupId(), kjar.getArtifactName(), kjar.getVersion());
        ContainerSpec containerSpec = new ContainerSpec(containerId, containerAlias, serverTemplateKey, releasedId, status, configs);
        kieControllerClient.saveContainerSpec(serverTemplateId, containerSpec);
    }
}
