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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlNamespace;
import org.eclipse.sapphire.samples.contacts.internal.CityNameValuesProvider;
import org.eclipse.sapphire.samples.contacts.internal.StateCodeValuesProvider;
import org.eclipse.sapphire.samples.contacts.internal.ZipCodeValuesProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlNamespace( uri = "http://www.eclipse.org/sapphire/samples/address", prefix = "a" )

public interface IAddress

    extends IModelElement

{
    ModelElementType TYPE = new ModelElementType( IAddress.class );
    
    // *** Street ***

    @XmlBinding( path = "a:street" )
    @Label( standard = "street" )
    @NonNullValue

    ValueProperty PROP_STREET = new ValueProperty( TYPE, "Street" );

    Value<String> getStreet();
    void setStreet( String street );
    
    // *** City ***

    @XmlBinding( path = "a:city" )
    @Label( standard = "city" )
    @NonNullValue
    @PossibleValues( service = CityNameValuesProvider.class )
    @DependsOn( { "ZipCode", "State" } )

    ValueProperty PROP_CITY = new ValueProperty( TYPE, "City" );

    Value<String> getCity();
    void setCity( String city );

    // *** State ***

    @XmlBinding( path = "a:state" )
    @Label( standard = "state" )
    @NonNullValue
    @PossibleValues( service = StateCodeValuesProvider.class )
    @DependsOn( { "ZipCode", "City" } )

    ValueProperty PROP_STATE = new ValueProperty( TYPE, "State" );

    Value<String> getState();
    void setState( String state );

    // *** ZipCode ***

    @XmlBinding( path = "a:zip" )
    @Label( standard = "ZIP code" )
    @NonNullValue
    @PossibleValues( service = ZipCodeValuesProvider.class )
    @DependsOn( { "State", "City" } )

    ValueProperty PROP_ZIP_CODE = new ValueProperty( TYPE, "ZipCode" );

    Value<String> getZipCode();
    void setZipCode( String zipCode );

}
