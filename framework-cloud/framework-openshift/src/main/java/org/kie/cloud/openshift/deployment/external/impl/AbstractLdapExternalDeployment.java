/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.openshift.deployment.external.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.openshift.api.model.SecurityContextConstraintsBuilder;
import org.kie.cloud.api.deployment.LdapDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.deployment.LdapDeploymentImpl;
import org.kie.cloud.openshift.deployment.external.AbstractExternalDeployment;
import org.kie.cloud.openshift.resource.CloudProperties;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLdapExternalDeployment<U> extends AbstractExternalDeployment<LdapDeployment, U> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLdapExternalDeployment.class);
    private static final String LDAP_TEMPLATE = "/deployments/ldap.yaml";

    private static final String LDAP_SERVICE_ACCOUNT = "ldap-sa";
    private static final String SCC_USER_FORMAT = "system:serviceaccount:%s:%s";

    public AbstractLdapExternalDeployment(Map<String, String> deploymentConfig) {
        super(deploymentConfig);
    }

    @Override
    public ExternalDeploymentID getKey() {
        return ExternalDeploymentID.LDAP;
    }

    @Override
    protected LdapDeployment deployToProject(Project project) {
        logger.info("Creating LDAP Service account");
        ServiceAccount ldapServiceAccount = new ServiceAccountBuilder().withNewMetadata().withName(LDAP_SERVICE_ACCOUNT)
                .withNamespace(project.getName()).endMetadata().build();
        project.getOpenShift().serviceAccounts().create(ldapServiceAccount);
        logger.debug("Set LDAP Service account to the SecurityContextConstrain anyuid policy. oc-cli command: 'oc adm policy add-scc-to-user anyuid -z ldap-sa -n <project name>'");
        project.getOpenShiftAdmin().securityContextConstraints().withName("anyuid")
                .edit(scc -> new SecurityContextConstraintsBuilder(scc)
                        .addToUsers(String.format(SCC_USER_FORMAT, project.getName(), LDAP_SERVICE_ACCOUNT))
                        .build());

        logger.info("Creating internal LDAP instance.");
        project.processTemplateAndCreateResources(getLdapTemplate(), getLdapProperties(project));

        return new LdapDeploymentImpl(project);
    }

    @Override
    public void beforeProjectDelete(Project project) {
        logger.debug("Remove LDAP Service account from SecurityContextConstrain anyuid.");
        project.getOpenShiftAdmin().securityContextConstraints().withName("anyuid")
                .edit(scc -> new SecurityContextConstraintsBuilder(scc)
                        .removeFromUsers(String.format(SCC_USER_FORMAT, project.getName(), LDAP_SERVICE_ACCOUNT))
                        .build());
    }

    private URL getLdapTemplate() {
        try {
            return new URL("file://" + getClass().getResource(LDAP_TEMPLATE).getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong LDAP template location", e);
        }
    }

    private Map<String, String> getLdapProperties(Project project) {
        Map<String, String> properties = new HashMap<>();
        properties.put("APPLICATION_NAME", project.getName());
        properties.put("LDAP_DOCKER_IMAGE", CloudProperties.getInstance().getLdapDockerImage());
        properties.put("LDAP_USER_NAME", DeploymentConstants.getAppUser());
        properties.put("LDAP_USER_PWD", DeploymentConstants.getAppPassword());
        properties.put("LDAP_SERVICE_ACCOUNT", LDAP_SERVICE_ACCOUNT);
        return properties;
    }

    @Override
    public void removeConfiguration(U object) {
        // Nothing done
    }

}
