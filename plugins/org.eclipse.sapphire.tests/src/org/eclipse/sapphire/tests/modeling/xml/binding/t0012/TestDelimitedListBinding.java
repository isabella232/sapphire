/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0012;

import org.eclipse.sapphire.modeling.xml.StandardXmlNamespaceResolver;
import org.eclipse.sapphire.modeling.xml.XmlDelimitedListBindingImpl;
import org.eclipse.sapphire.modeling.xml.XmlPath;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestDelimitedListBinding extends XmlDelimitedListBindingImpl
{
    private static final StandardXmlNamespaceResolver NAMESPACE_RESOLVER = new StandardXmlNamespaceResolver( TestElement.TYPE );
    private static final XmlPath PATH_LIST = new XmlPath( "List", NAMESPACE_RESOLVER );
    
    public TestDelimitedListBinding()
    {
        super( PATH_LIST );
    }
    
}
