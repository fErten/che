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
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.NODE;

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
public class CreateWorkspaceFromNodeStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String ANGULAR_PATTERNFLY_STARTER = "angular-patternfly-starter";
  private static final String NODEJS_HELLO_WORLD = "nodejs-hello-world";
  private static final String WEB_NODEJS_SIMPLE = "web-nodejs-simple";

  private ArrayList<String> projects = new ArrayList<>();
  private String currentWindow;

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projects.add(ANGULAR_PATTERNFLY_STARTER);
    projects.add(NODEJS_HELLO_WORLD);
    projects.add(WEB_NODEJS_SIMPLE);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromNodeStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(NODE, WORKSPACE_NAME, projects);

    currentWindow = ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(ANGULAR_PATTERNFLY_STARTER);
    projectExplorer.waitProjectInitialization(NODEJS_HELLO_WORLD);
    projectExplorer.waitProjectInitialization(WEB_NODEJS_SIMPLE);
  }

  @Test(priority = 1)
  public void checkAngularPatternfyStarterCommands() {
    consoles.startCommandAndCheckResult(
        ANGULAR_PATTERNFLY_STARTER,
        BUILD,
        "angular-patternfly-starter:install dependencies",
        "bower_components/font-awesome");
    consoles.startCommandAndCheckResult(
        ANGULAR_PATTERNFLY_STARTER, RUN, "angular-patternfly-starter:run", "Waiting...");
    consoles.startCommandAndCheckApp(currentWindow, "//*[@id='pf-app']");
    consoles.closeProcessTabWithAskDialog("angular-patternfly-starter:run");
  }

  @Test(priority = 1)
  public void checkNodejsHelloWorldCommands() {
    consoles.startCommandAndCheckResult(
        NODEJS_HELLO_WORLD, RUN, "nodejs-hello-world:run", "Example app listening on port 3000!");
    consoles.startCommandAndCheckApp(currentWindow, "//*[text()='Hello World!']");
    consoles.closeProcessTabWithAskDialog("nodejs-hello-world:run");
  }

  @Test(priority = 1)
  public void checkWebNodejsSimpleCommands() {
    consoles.startCommandAndCheckResult(
        WEB_NODEJS_SIMPLE,
        BUILD,
        "web-nodejs-simple:install dependencies",
        "bower_components/angular");
    consoles.startCommandAndCheckResult(
        WEB_NODEJS_SIMPLE, RUN, "web-nodejs-simple:run", "Started connect web server");
    consoles.startCommandAndCheckApp(currentWindow, "//p[text()=' from the Yeoman team']");
  }
}
