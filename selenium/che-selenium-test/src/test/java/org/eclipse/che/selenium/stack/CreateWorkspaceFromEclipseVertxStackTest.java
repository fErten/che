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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_VERTX;

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
public class CreateWorkspaceFromEclipseVertxStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String VERTX_HEALTH_CHECKS_BOOSTER = "vertx-health-checks-booster";
  private static final String VERTX_HTTP_BOOSTER = "vertx-http-booster";

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
    projects.add(VERTX_HEALTH_CHECKS_BOOSTER);
    projects.add(VERTX_HTTP_BOOSTER);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromEclipseVertxStack() {
    String currentWindow;

    createWorkspaceHelper.createWorkspaceFromStackWithProjects(
        ECLIPSE_VERTX, WORKSPACE_NAME, projects);

    currentWindow = ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(VERTX_HEALTH_CHECKS_BOOSTER);
    projectExplorer.waitProjectInitialization(VERTX_HTTP_BOOSTER);

    consoles.startCommandAndCheckResult(VERTX_HEALTH_CHECKS_BOOSTER, BUILD, "build", BUILD_SUCCESS);
    consoles.startCommandAndCheckResult(
        VERTX_HEALTH_CHECKS_BOOSTER, RUN, "run", "[INFO] INFO: Succeeded in deploying verticle");
    consoles.startCommandAndCheckApp(currentWindow, "//h2[@id='_vert_x_health_check_booster']");
    consoles.closeProcessTabWithAskDialog("run");
    consoles.startCommandAndCheckResult(
        VERTX_HEALTH_CHECKS_BOOSTER,
        DEBUG,
        "debug",
        "[INFO] Listening for transport dt_socket at address: 5005");
    consoles.closeProcessTabWithAskDialog("debug");

    consoles.startCommandAndCheckResult(VERTX_HTTP_BOOSTER, BUILD, "build", BUILD_SUCCESS);
    consoles.startCommandAndCheckResult(
        VERTX_HTTP_BOOSTER, RUN, "run", "[INFO] INFO: Succeeded in deploying verticle");
    consoles.startCommandAndCheckApp(currentWindow, "//h2[@id='_vert_x_health_check_booster']");
    consoles.startCommandAndCheckResult(
        VERTX_HTTP_BOOSTER,
        DEBUG,
        "debug",
        "[INFO] Listening for transport dt_socket at address: 5005");
  }
}
