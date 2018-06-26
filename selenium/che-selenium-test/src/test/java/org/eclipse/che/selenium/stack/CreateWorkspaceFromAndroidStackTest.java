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
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ANDROID;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromAndroidStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String MOBILE_ANDROID_HELLO_WORLD = "mobile-android-hello-world";
  private static final String MOBILE_ANDROID_SIMPLE = "mobile-android-simple";

  private ArrayList<String> projects = new ArrayList<>();

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projects.add(MOBILE_ANDROID_HELLO_WORLD);
    projects.add(MOBILE_ANDROID_SIMPLE);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromAndroidStack() {
    stackHelper.createWorkspaceFromStackWithProjects(ANDROID, WORKSPACE_NAME, projects);

    stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(MOBILE_ANDROID_HELLO_WORLD);
    stackHelper.waitProjectInitialization(MOBILE_ANDROID_SIMPLE);

    stackHelper.startCommandAndCheckResult(
        MOBILE_ANDROID_HELLO_WORLD, BUILD, "build", BUILD_SUCCESS);
    stackHelper.startCommandAndCheckResult(
        MOBILE_ANDROID_HELLO_WORLD, RUN, "mobile-android-hello-world:run", BUILD_SUCCESS);

    stackHelper.startCommandAndCheckResult(
        MOBILE_ANDROID_SIMPLE, RUN, "mobile-android-simple:build", BUILD_SUCCESS);
  }
}
