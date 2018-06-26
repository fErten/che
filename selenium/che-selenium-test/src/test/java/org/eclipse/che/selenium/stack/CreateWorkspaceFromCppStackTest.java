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
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CPP;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromCppStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_CPP_SIMPLE = "console-cpp-simple";
  private static final String C_SIMPLE_CONSOLE = "c-simple-console";

  private ArrayList<String> projects = new ArrayList<>();

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projects.add(CONSOLE_CPP_SIMPLE);
    projects.add(C_SIMPLE_CONSOLE);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromCppStack() {
    stackHelper.createWorkspaceFromStackWithProjects(CPP, WORKSPACE_NAME, projects);

    stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(CONSOLE_CPP_SIMPLE);
    stackHelper.waitProjectInitialization(C_SIMPLE_CONSOLE);

    stackHelper.startCommandAndCheckResult(CONSOLE_CPP_SIMPLE, RUN, "run", "Hello World!");
    stackHelper.startCommandAndCheckResult(
        CONSOLE_CPP_SIMPLE, RUN, "console-cpp-simple:build and run", "Hello World!");

    stackHelper.startCommandAndCheckResult(
        C_SIMPLE_CONSOLE, RUN, "c-simple-console:build and run", "Hello World");
  }
}
