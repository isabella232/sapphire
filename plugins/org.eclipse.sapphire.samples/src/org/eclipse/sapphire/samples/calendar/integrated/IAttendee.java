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

package org.eclipse.sapphire.samples.calendar.integrated;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.samples.calendar.AttendeeType;
import org.eclipse.sapphire.samples.calendar.integrated.internal.AttendeeImageProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Image( provider = AttendeeImageProvider.class )
@GenerateImpl

public interface IAttendee

    extends IModelElement

{
    ModelElementType TYPE = new ModelElementType( IAttendee.class );
    
    // *** Name ***
    
    @Label( standard = "name" )
    @NonNullValue

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );
    
    // *** Type ***
    
    @Type( base = AttendeeType.class )
    @Label( standard = "type" )
    @DefaultValue( text = "REQUIRED" )

    ValueProperty PROP_TYPE = new ValueProperty( TYPE, "Type" );

    Value<AttendeeType> getType();
    void setType( String type );
    void setType( AttendeeType type );
    
    // *** InContactsDatabase ***
    
    @Type( base = Boolean.class )
    @Label( standard = "in contacts database" )
    @DefaultValue( text = "false" )
    @ReadOnly
    
    ValueProperty PROP_IN_CONTACTS_DATABASE = new ValueProperty( TYPE, "InContactsDatabase" );
    
    Value<Boolean> isInContactsDatabase();
    
    // *** EMail ***
    
    @Label( standard = "E-Mail" )

    ValueProperty PROP_E_MAIL = new ValueProperty( TYPE, "EMail" );

    Value<String> getEMail();
    void setEMail( String email );
    
}
