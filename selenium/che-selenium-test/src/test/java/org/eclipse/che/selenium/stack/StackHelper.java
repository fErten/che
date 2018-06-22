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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.By;

public class StackHelper {

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CodenvyEditor editor;
  @Inject private Workspaces workspaces;
  @Inject private ToastLoader toastLoader;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  public void createWorkspaceWithProjectsFromStack(
      NewWorkspace.Stack stack, String workspaceName, ArrayList<String> projectNames) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(workspaceName);
    projectSourcePage.clickOnAddOrImportProjectButton();

    projectNames.forEach(
        project -> {
          projectSourcePage.selectSample(project);
        });

    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

  public void startCommandAndCheckResult(
      String projectName,
      ContextMenuCommandGoals goal,
      String commandName,
      String expectedMessageInTerminal) {
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.invokeCommandWithContextMenu(goal, projectName, commandName);

    consoles.waitTabNameProcessIsPresent(commandName);
    consoles.waitProcessInProcessConsoleTree(commandName);
    consoles.waitExpectedTextIntoConsole(expectedMessageInTerminal, WIDGET_TIMEOUT_SEC);
  }

  public void startCommandAndCheckApp(String currentWindow, String xpath) {
    consoles.waitPreviewUrlIsPresent();
    consoles.clickOnPreviewUrl();
    seleniumWebDriverHelper.switchToNextWindow(currentWindow);

    seleniumWebDriverHelper.waitVisibility(By.xpath(xpath));

    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
  }

  public void checkLanguageServerInitialization(
      String projectName, String fileName, String textInTerminal) {
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.openItemByPath(projectName);
    projectExplorer.openItemByPath(projectName + "/" + fileName);
    editor.waitTabIsPresent(fileName);

    // check a language server initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(textInTerminal, ELEMENT_TIMEOUT_SEC);
  }

  public String switchToIdeAndWaitWorkspaceIsReadyToUse() {
    String currentWindow = seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();

    return currentWindow;
  }

  public void waitProjectInitialization(String projectName) {
    projectExplorer.waitItem(projectName);
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    projectExplorer.waitDefinedTypeOfFolder(projectName, PROJECT_FOLDER);
  }

  /**
   * Create workspace on Dashboard from @stack with @name with @projectName project
   *
   * @param stack
   * @param name
   * @param projectName
   */
  public void createWorkspaceWithProjectFromStack(
      NewWorkspace.Stack stack, String name, String projectName) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(name);
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }
}
