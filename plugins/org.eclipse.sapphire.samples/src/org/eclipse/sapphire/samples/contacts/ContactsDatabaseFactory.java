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

package org.eclipse.sapphire.samples.contacts;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.modeling.xml.ModelStoreForXml;
import org.eclipse.sapphire.samples.contacts.internal.ContactsDatabase;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContactsDatabaseFactory
{
    public static IContactsDatabase load( final File file )
    {
        return load( new ModelStoreForXml( file ) );
    }
    
    public static IContactsDatabase load( final IFile file )
    {
        return load( new ModelStoreForXml( file ) );
    }
    
    public static IContactsDatabase load( final ModelStoreForXml modelStore )
    {
        return new ContactsDatabase( modelStore );
    }
    
}
