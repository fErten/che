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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PHP;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromPHPStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "web-php-simple";
  private static final String PHP_FILE_NAME = "index.php";
  private static final String LS_INIT_MESSAGE =
      format(
          "Finished language servers initialization, file path '/%s/%s'",
          PROJECT_NAME, PHP_FILE_NAME);

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private CheTerminal terminal;
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
  public void checkWorkspaceCreationFromPHPStack() {
    String currentWindow;

    stackHelper.createWorkspaceWithProjectFromStack(PHP, WORKSPACE_NAME, PROJECT_NAME);

    currentWindow = stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(PROJECT_NAME);

    // open 'index.php' file and check Python language server initialization
    stackHelper.checkLanguageServerInitialization(PROJECT_NAME, PHP_FILE_NAME, LS_INIT_MESSAGE);

    stackHelper.startCommandAndCheckResult(PROJECT_NAME, RUN, "run php script", "Hello World!");

    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, RUN, "start apache", "Starting Apache httpd web server apache2");
    stackHelper.startCommandAndCheckApp(currentWindow, "//*[text()='Hello World!']");

    stackHelper.startCommandAndCheckResult(PROJECT_NAME, RUN, "restart apache", "...done");
    stackHelper.startCommandAndCheckApp(currentWindow, "//*[text()='Hello World!']");

    // start 'stop apache' command and check that apache not running
    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, RUN, "stop apache", "Stopping Apache httpd web server apache2");
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoTerminal("ps ax | grep 'service apache2 start'");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("service apache2 start");
  }
}
