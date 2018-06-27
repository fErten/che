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

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.DOT_NET;

import com.google.inject.Inject;
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
public class CreateWorkspaceFromNETStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "dotnet-web-simple";
  private static final String CS_FILE_NAME = "Program.cs";
  private static final String LS_INIT_MESSAGE =
      format(
          "Finished language servers initialization, file path '/%s/%s'",
          PROJECT_NAME, CS_FILE_NAME);

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromNETStack() {
    String currentWindow;

    createWorkspaceHelper.createWorkspaceFromStackWithProject(
        DOT_NET, WORKSPACE_NAME, PROJECT_NAME);

    currentWindow = ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(PROJECT_NAME);

    consoles.startCommandAndCheckResult(
        PROJECT_NAME, BUILD, "update dependencies", "Restore completed");
    consoles.startCommandAndCheckResult(
        PROJECT_NAME, BUILD, "dotnet-web-simple:update dependencies", "Restore completed");

    consoles.startCommandAndCheckResult(PROJECT_NAME, RUN, "run", "Application started.");
    consoles.startCommandAndCheckApp(currentWindow, "//pre[text()='Hello World!']");
    consoles.closeProcessTabWithAskDialog("run");

    consoles.startCommandAndCheckResult(
        PROJECT_NAME, RUN, "dotnet-web-simple:run", "Application started.");
    consoles.startCommandAndCheckApp(currentWindow, "//pre[text()='Hello World!']");

    createWorkspaceHelper.checkLanguageServerInitialization(
        PROJECT_NAME, CS_FILE_NAME, LS_INIT_MESSAGE);
  }
}
