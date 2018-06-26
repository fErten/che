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
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PYTHON;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromPythonStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "console-python3-simple";
  private static final String PYTHON_FILE_NAME = "main.py";
  private static final String LS_INIT_MESSAGE =
      format(
          "Finished language servers initialization, file path '/%s/%s'",
          PROJECT_NAME, PYTHON_FILE_NAME);

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private NewWorkspace newWorkspace;
  @Inject private DefaultTestUser defaultTestUser;
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
  public void checkWorkspaceCreationFromPythonStack() {
    stackHelper.createWorkspaceFromStackWithProject(PYTHON, WORKSPACE_NAME, PROJECT_NAME);

    stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(PROJECT_NAME);

    stackHelper.startCommandAndCheckResult(PROJECT_NAME, RUN, "run", "Hello, world!");
    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, RUN, "console-python3-simple:run", "Hello, world!");

    stackHelper.checkLanguageServerInitialization(PROJECT_NAME, PYTHON_FILE_NAME, LS_INIT_MESSAGE);
  }
}
