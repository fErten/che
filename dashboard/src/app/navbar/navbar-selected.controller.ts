/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc directive
 * @name navbar.selected.controller:NavBarSelected
 * @description This class is controller of NavBarSelected
 * @author Florent Benoit
 */
export class NavBarSelectedCtrl {

  static $inject = ['$mdSidenav'];

  $mdSidenav: ng.material.ISidenavService;

  /**
   * Default constructor that is using resource
   */
  constructor ($mdSidenav: ng.material.ISidenavService) {
    this.$mdSidenav = $mdSidenav;
  }

  /**
   * Close left navbar
   */
  close() {
    this.$mdSidenav('left').close();
  }


}
