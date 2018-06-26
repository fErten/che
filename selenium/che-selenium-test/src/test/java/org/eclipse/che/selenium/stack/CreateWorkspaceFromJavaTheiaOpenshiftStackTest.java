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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_THEIA_OPENSHIFT;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
@Test(groups = {TestGroup.OPENSHIFT})
public class CreateWorkspaceFromJavaTheiaOpenshiftStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
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
  public void createWorkspaceFromJavaTheiaOpenshiftStack() {
    stackHelper.createWorkspaceFromStackWithoutProject(JAVA_THEIA_OPENSHIFT, WORKSPACE_NAME);

    seleniumWebDriverHelper.waitAndSwitchToFrame(
        By.id("ide-application-iframe"), PREPARING_WS_TIMEOUT_SEC);

    seleniumWebDriverHelper.waitVisibility(By.id("theia-app-shell"), PREPARING_WS_TIMEOUT_SEC);
  }
}
