/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.contacts;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContactsDatabaseFactory
{
    public static ContactsDatabase load( final File file )
    
        throws ResourceStoreException
        
    {
        return load( new XmlResourceStore( file ) );
    }
    
    public static ContactsDatabase load( final IFile file )
    
        throws ResourceStoreException
        
    {
        return load( new XmlResourceStore( new WorkspaceFileResourceStore( file ) ) );
    }
    
    public static ContactsDatabase load( final XmlResourceStore resourceStore )
    {
        return ContactsDatabase.TYPE.instantiate( new RootXmlResource( resourceStore ) );
    }
    
}
