/*******************************************************************************
 * Copyright (c) 2014 Accenture Services Pvt Ltd. and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kamesh Sampath - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes    
 *    Konstantin Komissarchik - [382453] @InitialValue annotation can cause problems for XML resources with invalid schemas
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0010;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests InitialValueService and InitialValue annotation in the context of XML binding.
 * 
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0010 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore(
                loadResourceAsStream("initial.txt"));
        final XmlResourceStore xmlResourceStore = new XmlResourceStore(
                byteArrayResourceStore);

        final TestModelRoot root = TestModelRoot.TYPE.instantiate(new RootXmlResource(xmlResourceStore));
        root.initialize();
        root.resource().save();
        
        assertEqualsIgnoreNewLineDiffs(loadResource("result.txt"), new String(byteArrayResourceStore.getContents(), UTF8));
    }

}
