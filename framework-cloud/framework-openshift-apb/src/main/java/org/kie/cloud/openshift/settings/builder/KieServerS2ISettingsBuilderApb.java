/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.openshift.settings.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.protocol.Protocol;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsApb;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class KieServerS2ISettingsBuilderApb implements KieServerS2ISettingsBuilder {

    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.KIE_SERVER_HTTPS_S2I;
    private Map<String, String> extraVars;

    public KieServerS2ISettingsBuilderApb() {
        extraVars = new HashMap<>();

        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.IMMUTABLE_KIE);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.H2);

        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsApb(extraVars, appTemplate);
    }

    @Override
    public KieServerS2ISettingsBuilder withApplicationName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.APPLICATION_NAME, name);
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withKieServerUser(String kieServerUser, String kieServerPwd) {
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, kieServerUser);
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, kieServerPwd);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerUser(String controllerUser, String controllerPwd) {
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, controllerUser);
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, controllerPwd);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerProtocol(Protocol protocol) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_CONTROLLER_PROTOCOL, protocol.name());
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerConnection(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_CONTROLLER_SERVICE, serviceName);
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerConnection(String url, String port) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_CONTROLLER_HOST, url);
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_CONTROLLER_PORT, port);
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSmartRouterConnection(String url, String port) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_ROUTER_HOST, url);
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_ROUTER_PORT, port);
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSmartRouterConnection(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_ROUTER_SERVICE, serviceName);
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withContainerDeployment(String kieContainerDeployment) {
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir) {
        extraVars.put(OpenShiftApbConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        extraVars.put(OpenShiftApbConstants.SOURCE_REPOSITORY_REF, gitReference);
        extraVars.put(OpenShiftApbConstants.CONTEXT_DIR, gitContextDir);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs) {
        extraVars.put(OpenShiftApbConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        extraVars.put(OpenShiftApbConstants.SOURCE_REPOSITORY_REF, gitReference);
        extraVars.put(OpenShiftApbConstants.CONTEXT_DIR, gitContextDir);
        extraVars.put(OpenShiftApbConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withMavenRepoUrl(String url) {
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_URL, url);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withMavenRepoUser(String repoUser, String repoPassword) {
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_USER, repoUser);
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_PWD, repoPassword);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withKieServerSyncDeploy(boolean syncDeploy) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_SYNC_DEPLOY, Boolean.toString(syncDeploy));
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withHostame(String http) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_HOSTNAME_HTTP, http);
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSecuredHostame(String https) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_HOSTNAME_HTTPS, https);
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        //return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withKieServerSecret(String secret) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.KIE_SERVER_HTTPS_SECRET, secret);
        //return this;
    }
}
