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

package org.eclipse.sapphire.samples.zoo;

import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.ValuePropertyCustomBinding;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.zoo.internal.EmployeeFullNamePropertyBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "employee" )
@Image( small = "org.eclipse.sapphire.samples/images/person.png" )
@GenerateXmlBinding

public interface IEmployee

    extends IModelElementForXml, IRemovable

{
    ModelElementType TYPE = new ModelElementType( IEmployee.class );

    // *** FirstName ***
    
    @XmlBinding( path = "first-name" )
    @Label( standard = "first name" )
    @NonNullValue

    ValueProperty PROP_FIRST_NAME = new ValueProperty( TYPE, "FirstName" );

    Value<String> getFirstName();
    void setFirstName( String firstName );
    
    // *** LastName ***

    @XmlBinding( path = "last-name" )
    @Label( standard = "last name" )
    @NonNullValue

    ValueProperty PROP_LAST_NAME = new ValueProperty( TYPE, "LastName" );

    Value<String> getLastName();
    void setLastName( String lastName );
    
    // ** FullName ***

    @Label( standard = "full name" )
    @NonNullValue

    // FullName is a derived property created as a composite of the FirstName and LastName
    // properties. 
    
    // The @ReadOnly annotation indicates that the setter method should not be expected.
    
    @ReadOnly

    // The @DependsOn annotation indicates that this property changes whenever one
    // of the specified properties changes.

    @DependsOn( { "FirstName", "LastName" } )
    
    @ValuePropertyCustomBinding( impl = EmployeeFullNamePropertyBinding.class )
    
    ValueProperty PROP_FULL_NAME = new ValueProperty( TYPE, "FullName" );

    Value<String> getFullName();
}
