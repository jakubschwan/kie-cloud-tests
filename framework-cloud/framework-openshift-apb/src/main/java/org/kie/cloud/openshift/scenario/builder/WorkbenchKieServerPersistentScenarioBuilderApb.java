/*
 * Copyright 2018 JBoss by Red Hat.
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
package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.ProjectApbSpecificPropertyNames;
import org.kie.cloud.openshift.scenario.WorkbenchKieServerPersistentScenarioApb;

public class WorkbenchKieServerPersistentScenarioBuilderApb implements WorkbenchKieServerPersistentScenarioBuilder {

    private final Map<String, String> extraVars = new HashMap<>();
    private boolean deploySSO = false;
    private final ProjectApbSpecificPropertyNames propertyNames = ProjectApbSpecificPropertyNames.create();

    public WorkbenchKieServerPersistentScenarioBuilderApb() {
        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.AUTHORING);
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_DB_TYPE, ApbConstants.DbType.H2);

        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, DeploymentConstants.getControllerUser());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());
        extraVars.put(propertyNames.workbenchMavenUserName(), DeploymentConstants.getWorkbenchMavenUser());
        extraVars.put(propertyNames.workbenchMavenPassword(), DeploymentConstants.getWorkbenchMavenPassword());
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_USER, DeploymentConstants.getWorkbenchUser());
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_PWD, DeploymentConstants.getWorkbenchPassword());
        extraVars.put(propertyNames.workbenchHttpsSecret(), OpenShiftConstants.getKieApplicationSecretName());
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_SECRET_NAME, OpenShiftConstants.getKieApplicationSecretName());
    }

    @Override
    public WorkbenchKieServerPersistentScenario build() {
        return new WorkbenchKieServerPersistentScenarioApb(extraVars, deploySSO);
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_URL, repoUrl);
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_USER, repoUserName);
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_PWD, repoPassword);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder deploySso() {
        deploySSO = true;
        extraVars.put(OpenShiftApbConstants.SSO_USER, DeploymentConstants.getSsoServiceUser());
        extraVars.put(OpenShiftApbConstants.SSO_PWD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withBusinessCentralMavenUser(String user, String password) {
        extraVars.put(propertyNames.workbenchMavenUserName(), user);
        extraVars.put(propertyNames.workbenchMavenPassword(), password);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withKieServerId(String kieServerId) {
//        extraVars.put(OpenShiftApbConstants.KIE_SERVER_ID, kieServerId);
// TODO
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        extraVars.put(propertyNames.workbenchHostnameHttp(), hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        extraVars.put(propertyNames.workbenchHostnameHttps(), hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpKieServerHostname(String hostname) {
        extraVars.put(OpenShiftApbConstants.APB_KIESERVER_HOSTNAME, hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsKieServerHostname(String hostname) {
        //extraVars.put(OpenShiftApbConstants.EXECUTION_SERVER_HOSTNAME_HTTP, hostname);
        // TODO
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        extraVars.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
