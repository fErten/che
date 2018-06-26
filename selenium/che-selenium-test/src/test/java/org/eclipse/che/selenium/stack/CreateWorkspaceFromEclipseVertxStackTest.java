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

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
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

    stackHelper.createWorkspaceFromStackWithProjects(ECLIPSE_VERTX, WORKSPACE_NAME, projects);

    currentWindow = stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(VERTX_HEALTH_CHECKS_BOOSTER);
    stackHelper.waitProjectInitialization(VERTX_HTTP_BOOSTER);

    stackHelper.startCommandAndCheckResult(
        VERTX_HEALTH_CHECKS_BOOSTER, BUILD, "build", BUILD_SUCCESS);
    stackHelper.startCommandAndCheckResult(
        VERTX_HEALTH_CHECKS_BOOSTER, RUN, "run", "[INFO] INFO: Succeeded in deploying verticle");
    stackHelper.startCommandAndCheckApp(currentWindow, "//h2[@id='_vert_x_health_check_booster']");
    stackHelper.closeProcessTabWithAskDialog("run");
    stackHelper.startCommandAndCheckResult(
        VERTX_HEALTH_CHECKS_BOOSTER,
        DEBUG,
        "debug",
        "[INFO] Listening for transport dt_socket at address: 5005");
    stackHelper.closeProcessTabWithAskDialog("debug");

    stackHelper.startCommandAndCheckResult(VERTX_HTTP_BOOSTER, BUILD, "build", BUILD_SUCCESS);
    stackHelper.startCommandAndCheckResult(
        VERTX_HTTP_BOOSTER, RUN, "run", "[INFO] INFO: Succeeded in deploying verticle");
    stackHelper.startCommandAndCheckApp(currentWindow, "//h2[@id='_vert_x_health_check_booster']");
    stackHelper.startCommandAndCheckResult(
        VERTX_HTTP_BOOSTER,
        DEBUG,
        "debug",
        "[INFO] Listening for transport dt_socket at address: 5005");
  }
}
