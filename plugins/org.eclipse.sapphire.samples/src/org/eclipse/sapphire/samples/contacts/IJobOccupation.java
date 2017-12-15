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

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Validator;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.contacts.internal.ManagerNameValidator;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@Label( standard = "job" )

public interface IJobOccupation

    extends IOccupation
    
{
    ModelElementType TYPE = new ModelElementType( IJobOccupation.class );
    
    // *** Employer ***
    
    @Label( standard = "employer" )
    @NonNullValue
    @XmlBinding( path = "employer" )
    
    ValueProperty PROP_EMPLOYER = new ValueProperty( TYPE, "Employer" );
    
    Value<String> getEmployer();
    void setEmployer( String value );
    
    // *** Title ***
    
    @Label( standard = "title" )
    @NonNullValue
    @XmlBinding( path = "title" )
    
    ValueProperty PROP_TITLE = new ValueProperty( TYPE, "Title" );
    
    Value<String> getTitle();
    void setTitle( String value );
    
    // *** Manager ***
    
    @Label( standard = "manager" )
    @Validator( impl = ManagerNameValidator.class )
    @XmlBinding( path = "manager" )

    @PossibleValues
    ( 
        property = "/Contacts/Name", 
        caseSensitive = false, 
        invalidValueMessage = "Could not find contact name \"{0}\" in the database." 
    )

    ValueProperty PROP_MANAGER = new ValueProperty( TYPE, "Manager" );

    Value<String> getManager();
    void setManager( String value );

}
