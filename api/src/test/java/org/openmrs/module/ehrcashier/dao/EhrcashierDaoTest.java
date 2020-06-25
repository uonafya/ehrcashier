/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ehrcashier.api.dao;

import org.junit.Test;
import org.junit.Ignore;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ehrcashier.Item;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * It is an integration test (extends BaseModuleContextSensitiveTest), which verifies DAO methods
 * against the in-memory H2 database. The database is initially loaded with data from
 * standardTestDataset.xml in openmrs-api. All test methods are executed in transactions, which are
 * rolled back by the end of each test method.
 */
public class EhrcashierDaoTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	EhrcashierDao dao;
	
	@Autowired
	UserService userService;
	
	@Test
	@Ignore("Unignore if you want to make the Item class persistable, see also Item and liquibase.xml")
	public void saveItem_shouldSaveAllPropertiesInDb() {
		//Given
		Item item = new Item();
		item.setDescription("some description");
		item.setOwner(userService.getUser(1));
		
		//When
		dao.saveItem(item);
		
		//Let's clean up the cache to be sure getItemByUuid fetches from DB and not from cache
		Context.flushSession();
		Context.clearSession();
		
		//Then
		Item savedItem = dao.getItemByUuid(item.getUuid());
		
		assertThat(savedItem, hasProperty("uuid", is(item.getUuid())));
		assertThat(savedItem, hasProperty("owner", is(item.getOwner())));
		assertThat(savedItem, hasProperty("description", is(item.getDescription())));
	}
}
