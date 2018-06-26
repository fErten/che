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
import java.util.ArrayList;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromPHPStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String WEB_PHP_SIMPLE = "web-php-simple";
  private static final String WEB_PHP_GAE_SIMPLE = "web-php-gae-simple";
  private static final String PHP_FILE_NAME = "index.php";
  private static final String LS_INIT_MESSAGE =
      format(
          "Finished language servers initialization, file path '/%s/%s'",
          WEB_PHP_SIMPLE, PHP_FILE_NAME);

  private ArrayList<String> projects = new ArrayList<>();
  String currentWindow;

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Consoles consoles;
  @Inject private CheTerminal terminal;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projects.add(WEB_PHP_SIMPLE);
    projects.add(WEB_PHP_GAE_SIMPLE);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromPHPStack() {
    stackHelper.createWorkspaceFromStackWithProjects(PHP, WORKSPACE_NAME, projects);

    currentWindow = stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(WEB_PHP_SIMPLE);
    stackHelper.waitProjectInitialization(WEB_PHP_GAE_SIMPLE);
  }

  @Test(priority = 1)
  public void checkWebPhpSimpleCommands() {
    // open 'index.php' file and check Python language server initialization
    stackHelper.checkLanguageServerInitialization(WEB_PHP_SIMPLE, PHP_FILE_NAME, LS_INIT_MESSAGE);

    stackHelper.startCommandAndCheckResult(WEB_PHP_SIMPLE, RUN, "run php script", "Hello World!");

    stackHelper.startCommandAndCheckResult(
        WEB_PHP_SIMPLE, RUN, "start apache", "Starting Apache httpd web server apache2");
    stackHelper.startCommandAndCheckApp(currentWindow, "//*[text()='Hello World!']");

    stackHelper.startCommandAndCheckResult(WEB_PHP_SIMPLE, RUN, "restart apache", "...done");
    stackHelper.startCommandAndCheckApp(currentWindow, "//*[text()='Hello World!']");

    // start 'stop apache' command and check that apache not running
    stackHelper.startCommandAndCheckResult(
        WEB_PHP_SIMPLE, RUN, "stop apache", "Stopping Apache httpd web server apache2");
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoTerminal("ps ax");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("/usr/sbin/apache2 -k start");
  }

  @Test(priority = 1)
  public void checkWebPhpGaeSimpleCommands() {
    stackHelper.startCommandAndCheckResult(
        WEB_PHP_GAE_SIMPLE, RUN, "web-php-gae-simple:run", "Starting admin server");
    stackHelper.startCommandAndCheckApp(currentWindow, "//input[@value='Sign Guestbook']");
    stackHelper.closeProcessTabWithAskDialog("web-php-gae-simple:run");
  }
}
