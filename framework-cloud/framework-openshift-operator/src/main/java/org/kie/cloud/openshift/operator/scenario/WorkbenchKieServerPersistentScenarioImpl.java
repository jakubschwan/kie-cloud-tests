/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.operator.scenario;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.SupplierWaiter;
import cz.xtf.core.waiting.WaiterException;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.operator.deployment.KieServerOperatorDeployment;
import org.kie.cloud.openshift.operator.deployment.WorkbenchOperatorDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Auth;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.Sso;
import org.kie.cloud.openshift.scenario.ScenarioRequest;
import org.kie.cloud.openshift.util.Git;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerPersistentScenarioImpl extends OpenShiftOperatorScenario<WorkbenchKieServerScenario> implements WorkbenchKieServerPersistentScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private SsoDeployment ssoDeployment;
    private GitProvider gitProvider;
    private final ScenarioRequest request;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerPersistentScenarioImpl.class);

    public WorkbenchKieServerPersistentScenarioImpl(KieApp kieApp, ScenarioRequest request) {
        super(kieApp);
        this.request = request;
    }

    @Override
    protected void deployCustomResource() {

        if (request.isDeploySso()) {
            ssoDeployment = SsoDeployer.deploySecure(project);
            URL ssoSecureUrl = ssoDeployment.getSecureUrl().orElseThrow(() -> new RuntimeException("RH SSO secure URL not found."));

            Sso sso = new Sso();
            sso.setAdminUser(DeploymentConstants.getSsoServiceUser());
            sso.setAdminPassword(DeploymentConstants.getSsoServicePassword());
            sso.setUrl(SsoDeployer.createSsoEnvVariable(ssoSecureUrl.toString()));
            sso.setRealm(DeploymentConstants.getSsoRealm());
            sso.setDisableSSLCertValidation(true);

            Auth auth = new Auth();
            auth.setSso(sso);
            kieApp.getSpec().setAuth(auth);
        }

        if (request.getGitSettings() != null) {
            gitProvider = Git.createProvider(project, request.getGitSettings());
        }

        if (!isCustomTrustedSecretRegistered(kieApp.getSpec().getObjects().getConsole())) {
            registerTrustedSecret(kieApp.getSpec().getObjects().getConsole());
        }
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            if (!isCustomTrustedSecretRegistered(server)) {
                registerTrustedSecret(server);
            }
        }

        // deploy application
        getKieAppClient().createOrReplace(kieApp);
        new SupplierWaiter<KieApp>(() -> getKieAppClient().withName(OpenShiftConstants.getKieApplicationName()).get(), kieApp -> kieApp.getStatus() != null).reason("Waiting for reconciliation to initialize all fields.").timeout(TimeUnit.MINUTES,1).waitFor();
        new SimpleWaiter(this::isKieAppDeployed).reason("Waiting for KieApp to be deployed.")
                                                .timeout(TimeUnit.MINUTES, 1)
                                                .waitFor();

        workbenchDeployment = new WorkbenchOperatorDeployment(project, getKieAppClient());
        workbenchDeployment.setUsername(kieApp.getSpec().getCommonConfig().getAdminUser());
        workbenchDeployment.setPassword(kieApp.getSpec().getCommonConfig().getAdminPassword());

        kieServerDeployment = new KieServerOperatorDeployment(project, getKieAppClient());
        kieServerDeployment.setUsername(kieApp.getSpec().getCommonConfig().getAdminUser());
        kieServerDeployment.setPassword(kieApp.getSpec().getCommonConfig().getAdminPassword());

        logger.info("Waiting until all services are created.");
        try {
            new SimpleWaiter(() -> workbenchDeployment.isReady()).reason("Waiting for Workbench service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
            new SimpleWaiter(() -> kieServerDeployment.isReady()).reason("Waiting for Kie server service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
        } catch (WaiterException e) {
            throw new RuntimeException("Timeout while deploying application.", e);
        }

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logNodeNameOfAllInstances();

        // Used to track persistent volume content due to issues with volume cleanup
        storeProjectInfoToPersistentVolume(workbenchDeployment, "/opt/eap/standalone/data/kie");
    }

    private boolean isKieAppDeployed() {
        return getKieAppClient().withName(OpenShiftConstants.getKieApplicationName()).get().getStatus().getPhase().equals("Deployed");
    }

    @Override
    public WorkbenchDeployment getWorkbenchDeployment() {
        return workbenchDeployment;
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchDeployment, kieServerDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private void storeProjectInfoToPersistentVolume(Deployment deployment, String persistentVolumeMountPath) {
        String storeCommand = "echo \"Project " + projectName + ", time " + Instant.now() + "\" > " + persistentVolumeMountPath + "/info.txt";
        workbenchDeployment.getInstances().get(0).runCommand("/bin/bash", "-c", storeCommand);
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Collections.singletonList(workbenchDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Collections.singletonList(kieServerDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Collections.emptyList();
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
    }

    @Override
    public GitProvider getGitProvider() {
        return gitProvider;
    }

    @Override
    public void changeUsernameAndPassword(String username, String password) {
        if(getDeployments().stream().allMatch(Deployment::isReady)) {
            List<String> oldInstances = workbenchDeployment.getInstances().stream().map(Instance::getName).collect(Collectors.toList());
            oldInstances.addAll(kieServerDeployment.getInstances().stream().map(Instance::getName).collect(Collectors.toList()));

            kieApp = getKieAppClient().withName(OpenShiftConstants.getKieApplicationName()).get();
            kieApp.getSpec().getCommonConfig().setAdminUser(username);
            kieApp.getSpec().getCommonConfig().setAdminPassword(password);
            deployCustomResource();

            try {
                new SimpleWaiter(() -> areInstancesDeleted(oldInstances)).timeout(TimeUnit.MINUTES, 5).interval(TimeUnit.SECONDS, 5).waitFor();
            } catch (WaiterException e) {
                throw new RuntimeException("Timeout while deploying application.", e);
            }
            logger.info("Waiting for Workbench deployment to become ready.");
            workbenchDeployment.waitForScale();

            logger.info("Waiting for Kie server deployment to become ready.");
            kieServerDeployment.waitForScale();
        } else{
            throw new RuntimeException("Application is not ready for Username and password change. Please check first that application is ready.");
        }
    }

    private boolean areInstancesDeleted(Collection<String> oldInstancesNames) {
        return project.getOpenShift().getPods().stream().map(Pod::getMetadata)
                                                        .map(ObjectMeta::getName)
                                                        .noneMatch(oldInstancesNames::contains);
    }
}
