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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.RAILS;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromRailsStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_RUBY_SIMPLE_PROJECT = "console-ruby-simple";
  private static final String WEB_RAILS_SIMPLE_PROJECT = "web-rails-simple";

  ArrayList<String> projects = new ArrayList<>();

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projects.add(CONSOLE_RUBY_SIMPLE_PROJECT);
    projects.add(WEB_RAILS_SIMPLE_PROJECT);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromPythonStack() {
    String currentWindow;

    stackHelper.createWorkspaceWithProjectsFromStack(RAILS, WORKSPACE_NAME, projects);

    currentWindow = stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(CONSOLE_RUBY_SIMPLE_PROJECT);
    stackHelper.waitProjectInitialization(WEB_RAILS_SIMPLE_PROJECT);

    stackHelper.startCommandAndCheckResult(
        CONSOLE_RUBY_SIMPLE_PROJECT, RUN, "console-ruby-simple:run", "Hello world!");

    stackHelper.startCommandAndCheckResult(
        WEB_RAILS_SIMPLE_PROJECT, BUILD, "install dependencies", "Bundle complete!");
    stackHelper.startCommandAndCheckResult(
        WEB_RAILS_SIMPLE_PROJECT,
        BUILD,
        "web-rails-simple:install dependencies",
        "Bundle complete!");

    stackHelper.startCommandAndCheckResult(WEB_RAILS_SIMPLE_PROJECT, RUN, "run", "* Listening on");
    stackHelper.startCommandAndCheckApp(currentWindow, "//h1[text()='Yay! You’re on Rails!']");
    stackHelper.closeProcessTabWithAskDialog("run");

    stackHelper.startCommandAndCheckResult(
        WEB_RAILS_SIMPLE_PROJECT, RUN, "web-rails-simple:run", "* Listening on");
    stackHelper.startCommandAndCheckApp(currentWindow, "//h1[text()='Yay! You’re on Rails!']");
  }
}
