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

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.kie.wb.test.rest.client.RestWorkbenchClient;
import org.kie.wb.test.rest.client.WorkbenchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

public class Basic {

    private static final Logger logger = LoggerFactory.getLogger(Basic.class);

    @Test
    public void testCreateAndDeleteProjects() throws InterruptedException,ExecutionException {
        

        String url = "http://insecure-myapp-rhpamcentr-goldfinger.project.openshiftdomain";
        
        WorkbenchClient testCli = RestWorkbenchClient.createWorkbenchClient(url, "jschwan", "jschwan", 120, 120, 120);

        testCli.getSpaces().forEach(space -> System.out.println(space.getName()));
        testCli.createSpace("aaabc-rest", "jschwan");
        testCli.getSpaces().forEach(space -> System.out.println(space.getName()));
        logger.info("aaaaaa");
        testCli.getProjects("aaabc-rest").forEach(project -> System.out.println(project.getName()));
        logger.info("aaaaaa+BBBBB");
        logger.info("Strat creating project.");
        testCli.createProject("aaabc-rest", "rest-project", "org.kie.jschwan", "1.0.0-SNAPSHOT");
        logger.info("Project should be created.");
        testCli.getProjects("aaabc-rest").forEach(project -> System.out.println(project.getName()));
        logger.info("Proejcts in space: "+testCli.getProject("aaabc-rest", "rest-project"));

    }


}
