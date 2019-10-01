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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.wb.test.rest.client.RestWorkbenchClient;
import org.kie.wb.test.rest.client.WorkbenchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectCreateFailIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ProjectCreateFailIntegrationTest.class);
    private static final String SPACE_NAME = "rest-reproducer";
    private static final String URL = "http://insecure-myapp-rhpamcentr-goldfinger.project.openshiftdomain";

    private WorkbenchClient defaultWorkbenchClient;

    @Before
    public void createTestingSpace() {
        defaultWorkbenchClient = RestWorkbenchClient.createWorkbenchClient(URL, "jschwan", "jschwan", 120, 120, 120);
        defaultWorkbenchClient.createSpace(SPACE_NAME, "jschwan");
    }

    @After
    public void deleteTestingSpace(){
        defaultWorkbenchClient.deleteSpace(SPACE_NAME);
    }

    @Test
    public void testCreateAndDeleteProjects() throws InterruptedException,ExecutionException {
        // Create Runners with different users.
        List<ProjectRunner> runners = new ArrayList<>();
        runners.add(new ProjectRunner(URL, "tdavid", "tdavid"));
        runners.add(new ProjectRunner(URL, "aparedes", "aparedes"));
        // ... TODO add more if needed

        // Create executor service to run every tasks in own thread
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        // Create task to create projects for all users
        List<Callable<Collection<String>>> createTasks = runners.stream()
                                                                .map(runner -> runner.createProjects(SPACE_NAME, UUID.randomUUID().toString().substring(0, 6), 1, 5))
                                                                .collect(Collectors.toList());
        List<Future<Collection<String>>> futures = executorService.invokeAll(createTasks);
        logger.info("All create task threads were executed");
        
        List<String> expectedList = getAllStringFromFutures(futures);

        // Check that all projects where created
        checkProjectsWereCreated(SPACE_NAME, expectedList, runners.size(), 5);

        logger.info("All spaces created and asserts passed");
        
        // GET ALL

        List<Callable<Collection<ProjectResponse>>> getAllProjects = runners.stream()
                                                                            .map(pr -> pr.getProjects(SPACE_NAME))
                                                                            .collect(Collectors.toList());
        List<Future<Collection<ProjectResponse>>> futuresProjects = executorService.invokeAll(getAllProjects);
        futuresProjects.forEach(futureProjects -> {
            try {
                assertThat(futureProjects.get().stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList())))
                    .isNotNull()
                    .isNotEmpty()
                    .containsExactlyInAnyOrder(expectedList.stream().toArray(String[]::new));
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        
        // DELETE ALL

        // Create tasks to delete projects
        List<Callable<Void>> deleteTasks = new ArrayList<>(runners.size());
        // Add list to delete from previous create task
        assertThat(runners).as("Check size of iterating lists.").hasSameSizeAs(futures);
        Iterator runnersIterator = runners.iterator();
        Iterator futureIterator = futures.iterator();
        while (runnersIterator.hasNext() && futureIterator.hasNext()) {
            ProjectRunner sr = (ProjectRunner) runnersIterator.next();
            Future<Collection<String>> f = (Future<Collection<String>>) futureIterator.next();

            deleteTasks.add(sr.deleteProjects(SPACE_NAME, f.get()));
        }

        // Execute task and wait for all threads to finished
        List<Future<Void>> deleteFutures = executorService.invokeAll(deleteTasks);
        getAllDeleteDone(deleteFutures);

        // Check all projects was deleted
        assertThat(defaultWorkbenchClient.getProjects(SPACE_NAME)).isNotNull().isEmpty();
    }

    protected void checkSpacesWereCreated(Collection<String> expectedSpaceNames, int runnersSize, int retries) {
        assertThat(expectedSpaceNames).isNotEmpty().hasSize(runnersSize * retries);
        Collection<Space> spaces = defaultWorkbenchClient.getSpaces();
        assertThat(spaces).isNotNull();
        List<String> resultSpaceNameList = spaces.stream().collect(Collectors.mapping(Space::getName, Collectors.toList()));
        assertThat(resultSpaceNameList).containsExactlyInAnyOrder(expectedSpaceNames.stream().toArray(String[]::new));
    }

    protected void checkProjectsWereCreated(String spaceName, Collection<String> expectedProjectNames, int runnersSize, int retries) {
        assertThat(expectedProjectNames).isNotEmpty().hasSize(runnersSize * retries);        
        Collection<ProjectResponse> projects = defaultWorkbenchClient.getProjects(spaceName);
        assertThat(projects).isNotNull();
        List<String> resultList = projects.stream().collect(Collectors.mapping(ProjectResponse::getName, Collectors.toList()));
        assertThat(resultList).containsExactlyInAnyOrder(expectedProjectNames.stream().toArray(String[]::new));
    }

    protected List<String> getAllStringFromFutures(List<Future<Collection<String>>> futures) {
        List<String> list = new ArrayList<>();

        //Wait to all threads finish
        futures.forEach(future -> {
            try {
                list.addAll(future.get());
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        return list;
    }

    protected void getAllDeleteDone(List<Future<Void>> deleteFutures) {
        //Wait to all delete threads finish
        deleteFutures.forEach(t -> {
            try {
                t.get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }


    // Generic function to split a list into two sublists
    protected <T> List[] split(List<T> list) {
        int size = list.size();

        List<T> first = new ArrayList<>(list.subList(0, (size + 1) / 2));
        List<T> second = new ArrayList<>(list.subList((size + 1) / 2, size));

        return new List[] { first, second };
    }

}
