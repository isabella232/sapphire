/******************************************************************************
 * Copyright (c) 2012 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.samples.contacts;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlSchema;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@GenerateImpl
@XmlNamespace( uri="http://www.eclipse.org/sapphire/samples/contacts", prefix="c" )
@XmlSchema( namespace="http://www.eclipse.org/sapphire/samples/contacts", location="http://www.eclipse.org/sapphire/samples/contacts/1.0" )
@XmlBinding( path = "c:contacts" )

public interface IContactsDatabase extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( IContactsDatabase.class );
    
    // *** Contacts ***

    @Type( base = IContact.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "contact", type = IContact.class ) )
    
    ListProperty PROP_CONTACTS = new ListProperty( TYPE, "Contacts" );
    
    ModelElementList<IContact> getContacts();
}
