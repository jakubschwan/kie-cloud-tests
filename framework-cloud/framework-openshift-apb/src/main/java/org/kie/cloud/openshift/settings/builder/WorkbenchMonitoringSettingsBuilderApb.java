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
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.openshift.constants.ApbConstants;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsApb;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class WorkbenchMonitoringSettingsBuilderApb implements WorkbenchMonitoringSettingsBuilder {

    private Map<String, String> extraVars;
    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.CONSOLE;

    public WorkbenchMonitoringSettingsBuilderApb() {
        extraVars = new HashMap<>();

        extraVars.put(OpenShiftApbConstants.APB_PLAN_ID, ApbConstants.Plans.IMMUTABLE_MON);
        extraVars.put(OpenShiftApbConstants.APB_IMAGE_STREAM_TAG, "1.1");

        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, DeploymentConstants.getControllerUser());
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());

        extraVars.put(OpenShiftApbConstants.APB_BUSINESSCENTRAL_REPLICAS, "1");
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsApb(extraVars, appTemplate);
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withAdminUser(String user, String password) {
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_USER, user);
        extraVars.put(OpenShiftApbConstants.KIE_ADMIN_PWD, password);
        return this;
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withApplicationName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
        //extraVars.put(OpenShiftApbConstants.APPLICATION_NAME, name);
        //return this;
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withControllerUser(String username, String password) {
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_USER, username);
        extraVars.put(OpenShiftApbConstants.KIE_CONTROLLER_PWD, password);
        return this;
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withKieServerUser(String username, String password) {
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_USER, username);
        extraVars.put(OpenShiftApbConstants.KIE_SERVER_PWD, password);
        return this;
    }
}
