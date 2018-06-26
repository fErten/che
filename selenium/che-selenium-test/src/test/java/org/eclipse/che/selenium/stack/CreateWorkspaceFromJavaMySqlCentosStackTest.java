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
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL_CENTOS;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromJavaMySqlCentosStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String WEB_JAVA_PETCLINIC = "web-java-petclinic";

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
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
  public void checkWorkspaceCreationFromJavaStack() {
    String currentWindow;

    stackHelper.createWorkspaceFromStackWithProject(
        JAVA_MYSQL_CENTOS, WORKSPACE_NAME, WEB_JAVA_PETCLINIC);

    currentWindow = stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(WEB_JAVA_PETCLINIC);

    stackHelper.startCommandFromProcessesArea("db", COMMON, "show databases", "information_schema");

    projectExplorer.waitAndSelectItem(WEB_JAVA_PETCLINIC);
    stackHelper.startCommandFromProcessesArea("dev-machine", COMMON, "build", BUILD_SUCCESS);
    stackHelper.startCommandFromProcessesArea(
        "dev-machine", BUILD, "web-java-petclinic:build", BUILD_SUCCESS);

    stackHelper.startCommandFromProcessesArea(
        "dev-machine", RUN, "web-java-petclinic:build and deploy", "Server startup in");

    stackHelper.startCommandAndCheckApp(currentWindow, "//h2[text()='Welcome']");
    stackHelper.closeProcessTabWithAskDialog("web-java-petclinic:build and deploy");

    // start 'stop tomcat' command and check that apache not running
    projectExplorer.invokeCommandWithContextMenu(
        RUN, WEB_JAVA_PETCLINIC, "web-java-petclinic:stop tomcat", "dev-machine");
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoTerminal("ps ax");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("$TOMCAT_HOME/bin/catalina.sh");

    stackHelper.startCommandFromProcessesArea(
        "dev-machine",
        DEBUG,
        "web-java-petclinic:debug",
        "Listening for transport dt_socket at address: 8000");
  }
}
