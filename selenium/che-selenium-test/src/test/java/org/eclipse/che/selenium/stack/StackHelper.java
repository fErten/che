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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.By;

/** @author Skoryk Serhii */
public class StackHelper {

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private AskDialog askDialog;
  @Inject private CodenvyEditor editor;
  @Inject private Workspaces workspaces;
  @Inject private ToastLoader toastLoader;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  // Start command from project context menu and check expected message in Terminal
  public void startCommandAndCheckResult(
      String projectName,
      ContextMenuCommandGoals goal,
      String commandName,
      String expectedMessageInTerminal) {
    projectExplorer.waitAndSelectItem(projectName);
    // TODO check issue about item selection while projects have long name
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.invokeCommandWithContextMenu(goal, projectName, commandName);

    consoles.waitTabNameProcessIsPresent(commandName);
    consoles.waitProcessInProcessConsoleTree(commandName);
    consoles.waitExpectedTextIntoConsole(expectedMessageInTerminal, PREPARING_WS_TIMEOUT_SEC);
  }

  public void startCommandFromProcessesArea(
      String machineName,
      ContextMenuCommandGoals goal,
      String commandName,
      String expectedMessageInTerminal) {
    consoles.startCommandFromProcessesArea(machineName, goal, commandName);
    consoles.waitTabNameProcessIsPresent(commandName);
    consoles.waitProcessInProcessConsoleTree(commandName);
    consoles.waitExpectedTextIntoConsole(expectedMessageInTerminal, PREPARING_WS_TIMEOUT_SEC);
  }

  // Open web page by url and check visibility of web element on opened page
  public void startCommandAndCheckApp(String currentWindow, String webElementXpath) {
    consoles.waitPreviewUrlIsPresent();
    consoles.clickOnPreviewUrl();
    seleniumWebDriverHelper.switchToNextWindow(currentWindow);

    seleniumWebDriverHelper.waitVisibility(By.xpath(webElementXpath));

    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
  }

  // Open file and check LS initialization message in "dev-machine" process
  public void checkLanguageServerInitialization(
      String projectName, String fileName, String textInTerminal) {
    consoles.selectProcessByTabName("dev-machine");
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.openItemByPath(projectName);
    projectExplorer.openItemByPath(projectName + "/" + fileName);
    editor.waitTabIsPresent(fileName);

    consoles.waitExpectedTextIntoConsole(textInTerminal, ELEMENT_TIMEOUT_SEC);
  }

  // Switch from Dashboard to IDE and check that workspace is ready to use
  public String switchToIdeAndWaitWorkspaceIsReadyToUse() {
    String currentWindow = seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();

    return currentWindow;
  }

  // Wait for project has PROJECT_FOLDER status
  public void waitProjectInitialization(String projectName) {
    projectExplorer.waitItem(projectName);
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    projectExplorer.waitDefinedTypeOfFolder(projectName, PROJECT_FOLDER);
  }

  public void createWorkspaceWithProjectFromStack(
      NewWorkspace.Stack stack, String workspaceName, String projectName) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();

    newWorkspace.waitToolbar();
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(workspaceName);
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

  public void createWorkspaceWithoutProjectFromStack(
      NewWorkspace.Stack stack, String workspaceName) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();

    newWorkspace.waitToolbar();
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(workspaceName);

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

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

  public void closeProcessTabWithAskDialog(String tabName) {
    String message =
        format(
            "The process %s will be terminated after closing console. Do you want to continue?",
            tabName);
    consoles.waitProcessInProcessConsoleTree(tabName);
    consoles.closeProcessByTabName(tabName);
    askDialog.acceptDialogWithText(message);
    consoles.waitProcessIsNotPresentInProcessConsoleTree(tabName);
  }
}
