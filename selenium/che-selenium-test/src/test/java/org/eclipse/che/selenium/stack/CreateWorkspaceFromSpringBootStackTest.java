/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.SPRING_BOOT;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromSpringBootStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String SPRING_BOOT_HEALTH_CHECK_BOOSTER = "spring-boot-health-check-booster";
  private static final String SPRING_BOOT_HTTP_BOOSTER = "spring-boot-http-booster";

  private ArrayList<String> projects = new ArrayList<>();

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projects.add(SPRING_BOOT_HEALTH_CHECK_BOOSTER);
    projects.add(SPRING_BOOT_HTTP_BOOSTER);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromSpringBootStack() {
    String currentWindow;

    createWorkspaceHelper.createWorkspaceFromStackWithProjects(
        SPRING_BOOT, WORKSPACE_NAME, projects);

    currentWindow = ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(SPRING_BOOT_HEALTH_CHECK_BOOSTER);
    projectExplorer.waitProjectInitialization(SPRING_BOOT_HTTP_BOOSTER);

    consoles.startCommandAndCheckResult(
        SPRING_BOOT_HEALTH_CHECK_BOOSTER, BUILD, "build", BUILD_SUCCESS);

    consoles.startCommandAndCheckResult(
        SPRING_BOOT_HEALTH_CHECK_BOOSTER, BUILD, "clean build", BUILD_SUCCESS);

    consoles.startCommandAndCheckResult(
        SPRING_BOOT_HEALTH_CHECK_BOOSTER, RUN, "run", "Started BoosterApplication in");
    consoles.startCommandAndCheckApp(currentWindow, "//h2[text()='Health Check Booster']");
    consoles.closeProcessTabWithAskDialog("run");

    consoles.startCommandAndCheckResult(
        SPRING_BOOT_HEALTH_CHECK_BOOSTER,
        DEBUG,
        "debug",
        "Listening for transport dt_socket at address: 5005");
    consoles.closeProcessTabWithAskDialog("debug");

    consoles.startCommandAndCheckResult(SPRING_BOOT_HTTP_BOOSTER, BUILD, "build", BUILD_SUCCESS);

    consoles.startCommandAndCheckResult(
        SPRING_BOOT_HTTP_BOOSTER, BUILD, "clean build", BUILD_SUCCESS);

    consoles.startCommandAndCheckResult(
        SPRING_BOOT_HTTP_BOOSTER, RUN, "run", "INFO: Setting the server's publish address to be");
    consoles.startCommandAndCheckApp(currentWindow, "//h2[text()='HTTP Booster']");
    consoles.closeProcessTabWithAskDialog("run");

    consoles.startCommandAndCheckResult(
        SPRING_BOOT_HTTP_BOOSTER,
        DEBUG,
        "debug",
        "Listening for transport dt_socket at address: 5005");
    consoles.closeProcessTabWithAskDialog("debug");
  }
}
