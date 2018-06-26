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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CEYLON_WITH_JAVA_JAVASCRIPT;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromCeylonWithJavaStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "ceylon-hello-world";
  private static final String MODULE_COMPILED_MESSAGE =
      "Note: Created module che.ceylon.samples.helloWorld";
  private static final String MODULE_STARTED_MESSAGE =
      "Hello World from Ceylon on the following backend : ";

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CommandsPalette commandsPalette;
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
  public void checkWorkspaceCreationFromCeylonStack() {
    stackHelper.createWorkspaceWithProjectFromStack(
        CEYLON_WITH_JAVA_JAVASCRIPT, WORKSPACE_NAME, PROJECT_NAME);

    stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(PROJECT_NAME);

    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, BUILD, "compile for JVM", MODULE_COMPILED_MESSAGE);
    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, RUN, "Run on JVM", MODULE_STARTED_MESSAGE + "jvm !");

    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, BUILD, "compile for JS", MODULE_COMPILED_MESSAGE);
    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, RUN, "Run on NodeJS", MODULE_STARTED_MESSAGE + "js !");

    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, BUILD, "compile for Dart", MODULE_COMPILED_MESSAGE);
    stackHelper.startCommandAndCheckResult(
        PROJECT_NAME, RUN, "Run on Dart", MODULE_STARTED_MESSAGE + "dartvm !");

    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.isItemVisible(PROJECT_NAME + "/modules");
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick("clean module");
    projectExplorer.waitItemIsNotPresentVisibleArea(PROJECT_NAME + "/modules");
  }
}
