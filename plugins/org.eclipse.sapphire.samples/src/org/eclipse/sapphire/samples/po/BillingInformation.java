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

package org.eclipse.sapphire.samples.po;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.samples.contacts.internal.CityNamePossibleValuesService;
import org.eclipse.sapphire.samples.contacts.internal.StateCodePossibleValuesService;
import org.eclipse.sapphire.samples.contacts.internal.ZipCodePossibleValuesService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface BillingInformation extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( BillingInformation.class );
    
    // *** Name ***
    
    @Label( standard = "name" )
    @Required
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String value );
    
    // *** Organization ***
    
    @Label( standard = "organization" )
    
    ValueProperty PROP_ORGANIZATION = new ValueProperty( TYPE, "Organization" );
    
    Value<String> getOrganization();
    void setOrganization( String value );
    
    // *** Street ***

    @Label( standard = "street" )
    @Required

    ValueProperty PROP_STREET = new ValueProperty( TYPE, "Street" );

    Value<String> getStreet();
    void setStreet( String street );
    
    // *** City ***

    @Label( standard = "city" )
    @Required
    @Service( impl = CityNamePossibleValuesService.class )
    @DependsOn( { "ZipCode", "State" } )

    ValueProperty PROP_CITY = new ValueProperty( TYPE, "City" );

    Value<String> getCity();
    void setCity( String city );

    // *** State ***

    @Label( standard = "state" )
    @Required
    @Service( impl = StateCodePossibleValuesService.class )
    @DependsOn( { "ZipCode", "City" } )

    ValueProperty PROP_STATE = new ValueProperty( TYPE, "State" );

    Value<String> getState();
    void setState( String state );

    // *** ZipCode ***

    @Label( standard = "ZIP code" )
    @Required
    @Service( impl = ZipCodePossibleValuesService.class )
    @DependsOn( { "State", "City" } )

    ValueProperty PROP_ZIP_CODE = new ValueProperty( TYPE, "ZipCode" );

    Value<String> getZipCode();
    void setZipCode( String zipCode );

}
