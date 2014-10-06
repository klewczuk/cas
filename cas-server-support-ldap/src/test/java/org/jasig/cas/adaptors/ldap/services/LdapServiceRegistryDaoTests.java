/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.ldap.services;

import org.jasig.cas.authentication.AbstractLdapTests;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link LdapServiceRegistryDao} class.
 * <p>
 * The casRegisteredService schema MUST be installed on the target OpenLDAP server prior to running this test.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 */
public class LdapServiceRegistryDaoTests extends AbstractLdapTests {
    private LdapServiceRegistryDao dao;

    public LdapServiceRegistryDaoTests() {
        super("/ldap-context.xml", "/ldap-regservice-test.xml");
        this.dao = this.context.getBean("serviceRegistryDao", LdapServiceRegistryDao.class);
        this.dao.init();
    }

    @Before
    public void setUp() throws Exception {

        for (final RegisteredService service : this.dao.load()) {
            this.dao.delete(service);
        }
    }


    @Test
    public void testServices() throws Exception {

        assertEquals(0, this.dao.load().size());

        AbstractRegisteredService rs = new RegisteredServiceImpl();
        rs.setName("Service Name1");
        rs.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        rs.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        rs.setDescription("Service description");
        rs.setServiceId("https://?.edu/**");
        rs.setTheme("the theme name");
        rs.setEvaluationOrder(123);
        rs.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        rs.setRequiredHandlers(new HashSet<String>(Arrays.asList("handler8", "handle92")));

        this.dao.save(rs);

        rs = new RegexRegisteredService();
        rs.setName("Service Name Regex");
        rs.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        rs.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        rs.setDescription("Service description");
        rs.setServiceId("^http?://.+");
        rs.setTheme("the theme name");
        rs.setEvaluationOrder(123);
        rs.setDescription("Here is another description");

        rs.setRequiredHandlers(new HashSet<String>(Arrays.asList("handler1", "handler2")));
        this.dao.save(rs);

        final List<RegisteredService> services = this.dao.load();
        assertEquals(2, services.size());

        AbstractRegisteredService rs2 = (AbstractRegisteredService) this.dao.findServiceById(services.get(0).getId());
        assertNotNull(rs2);
        rs2.setEvaluationOrder(9999);

        rs2.setName("Another Test Service");

        rs2 = (AbstractRegisteredService) this.dao.save(rs2);
        assertNotNull(rs2);

        for (final RegisteredService registeredService : services) {
            this.dao.delete(registeredService);
        }
        assertEquals(0, this.dao.load().size());
    }

}
