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

package org.kie.cloud.openshift.operator.deployment;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.RouterUtil;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.KieAppDoneable;
import org.kie.cloud.openshift.operator.model.KieAppList;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.resource.Project;

public class KieServerOperatorDeployment extends KieServerDeploymentImpl {

    private NonNamespaceOperation<KieApp, KieAppList, KieAppDoneable, Resource<KieApp, KieAppDoneable>> kieAppClient;

    public KieServerOperatorDeployment(Project project, NonNamespaceOperation<KieApp, KieAppList, KieAppDoneable, Resource<KieApp, KieAppDoneable>> kieAppClient) {
        super(project);
        this.kieAppClient = kieAppClient;
    }

    @Override
    public void scale(int instances) {
        if (isReady()) {
            KieApp kieApp = kieAppClient.withName(OpenShiftConstants.getKieApplicationName()).get();
            Server server = getAssociatedServerObject(kieApp);
            server.setReplicas(instances);
            kieAppClient.createOrReplace(kieApp);
        }
    }

    @Override
    public void waitForScale() {
        KieApp kieApp = kieAppClient.withName(OpenShiftConstants.getKieApplicationName()).get();

        Integer replicas = getAssociatedServerObject(kieApp).getReplicas();
        if (replicas == null) {
            // If replicas are not set in custom resource then get them from deployment config.
            replicas = getOpenShift().getDeploymentConfig(getServiceName()).getSpec().getReplicas();
        }

        waitUntilAllPodsAreReadyAndRunning(replicas);
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }

    private Server getAssociatedServerObject(KieApp kieApp) {
        return Arrays.asList(kieApp.getSpec().getObjects().getServers()).stream().filter(s -> s.getName().equals(getServiceName()))
                                                                                 .findAny()
                                                                                 .orElseThrow(() -> new RuntimeException("Server with name " + getServiceName() + " not found. Available server names are: " + getAvailableServerNames(kieApp)));
    }

    private String getAvailableServerNames(KieApp kieApp) {
        String serverNames = Arrays.asList(kieApp.getSpec().getObjects().getServers()).stream().map(s -> s.getName())
                                                                                               .collect(Collectors.joining(", "));
        return serverNames;
    }
}