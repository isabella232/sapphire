/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.contacts.internal;

import org.eclipse.sapphire.modeling.xml.StandardXmlNamespaceResolver;
import org.eclipse.sapphire.modeling.xml.XmlDelimitedListBindingImpl;
import org.eclipse.sapphire.modeling.xml.XmlPath;
import org.eclipse.sapphire.samples.contacts.IContact;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ConnectionsListController

    extends XmlDelimitedListBindingImpl

{
    private static final StandardXmlNamespaceResolver NAMESPACE_RESOLVER = new StandardXmlNamespaceResolver( IContact.TYPE );
    private static final XmlPath PATH_CONNECTIONS = new XmlPath( "connections", NAMESPACE_RESOLVER );
    
    public ConnectionsListController()
    {
        super( PATH_CONNECTIONS );
    }
    
}
