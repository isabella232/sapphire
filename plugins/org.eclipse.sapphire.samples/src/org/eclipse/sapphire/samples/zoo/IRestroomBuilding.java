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

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.BooleanPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "restroom building" )
@Image( small = "org.eclipse.sapphire.samples/images/bio-hazard.png" )
@GenerateXmlBinding

public interface IRestroomBuilding

    extends IStructure
    
{
    ModelElementType TYPE = new ModelElementType( IRestroomBuilding.class );
    
    // *** FacilityForMenPresent ***

    @Type( base = Boolean.class )
    @Label( standard = "facility for men present" )
    @DefaultValue( "false" )
    @XmlBinding( path = "restroom-building/facility-for-men-present" )

    ValueProperty PROP_FACILITY_FOR_MEN_PRESENT = new ValueProperty( TYPE, "FacilityForMenPresent" );

    Value<Boolean> isFacilityForMenPresent();
    void setFacilityForMenPresent( String facilityForMenPresent );
    void setFacilityForMenPresent( Boolean facilityForMenPresent );
    
    // *** FacilityForWomenPresent ***

    @Type( base = Boolean.class )
    @Label( standard = "facility for women present" )
    @DefaultValue( "false" )

    @BooleanPropertyXmlBinding( path = "restroom-building/facility-for-women-present", 
                                treatExistenceAsValue = true )
                                
    ValueProperty PROP_FACILITY_FOR_WOMEN_PRESENT = new ValueProperty( TYPE, "FacilityForWomenPresent" );

    Value<Boolean> isFacilityForWomenPresent();
    void setFacilityForWomenPresent( String facilityForWomenPresent );
    void setFacilityForWomenPresent( Boolean facilityForWomenPresent );
    
    // *** FacilityForFamiliesPresent ***

    @Type( base = Boolean.class )
    @Label( standard = "facility for families present" )
    @DefaultValue( "true" )

    @BooleanPropertyXmlBinding( path = "restroom-building/facility-for-families-absent", 
                                treatExistenceAsValue = true, valueWhenPresent = false )

    ValueProperty PROP_FACILITY_FOR_FAMILIES_PRESENT = new ValueProperty( TYPE, "FacilityForFamiliesPresent" );

    Value<Boolean> isFacilityForFamiliesPresent();
    void setFacilityForFamiliesPresent( String facilityForFamiliesPresent );
    void setFacilityForFamiliesPresent( Boolean facilityForFamiliesPresent );

}
