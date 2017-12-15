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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.ListPropertyCustomBinding;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBindingModelImpl;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.RootXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.zoo.internal.StructuresModelElementListController;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBindingModelImpl

@RootXmlBinding( namespace = "http://www.eclipse.org/sapphire/samples/zoo",
                 defaultPrefix = "z",
                 elementName = "zoo" )

public interface IZooModel

    extends IModelForXml
    
{
    ModelElementType TYPE = new ModelElementType( IZooModel.class );
    
    // *** Name ***

    @XmlBinding( path = "name" )
    @Label( standard = "name" )
    @NonNullValue

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );

    // *** Address ***

    @Type( base = IAddress.class )
    @XmlBinding( path = "address" )
    
    ElementProperty PROP_ADDRESS = new ElementProperty( TYPE, "Address" );
    
    IAddress getAddress();
    IAddress getAddress( boolean createIfNecessary );

    // *** Animals ***

    @Type( base = IAnimal.class, possible = { ILion.class, IGiraffe.class } )
    
    @ListPropertyXmlBinding( path = "animals",
                             mappings = 
                             { 
                                 @ListPropertyXmlBindingMapping( element = "giraffe", type = IGiraffe.class ),
                                 @ListPropertyXmlBindingMapping( element = "lion", type = ILion.class ) 
                             } )
    
    ListProperty PROP_ANIMALS = new ListProperty( TYPE, "Animals" );
    
    ModelElementList<IEmployee> getEmployees();

    // *** Employees ***

    @Type( base = IEmployee.class )
    
    @ListPropertyXmlBinding( path = "employees",
                             mappings = 
                             { 
                                 @ListPropertyXmlBindingMapping( element = "employee", type = IEmployee.class )
                             } )
    
    ListProperty PROP_EMPLOYEES = new ListProperty( TYPE, "Employees" );
    
    ModelElementList<IAnimal> getAnimals();
    
    // *** Structures ***
    
    @Type( base = IStructure.class, possible = { IAnimalEnclosure.class, IRestroomBuilding.class } )
    @ListPropertyCustomBinding( impl = StructuresModelElementListController.class )
    
    ListProperty PROP_STRUCTURES = new ListProperty( TYPE, "Structures" );
    
    ModelElementList<IStructure> getStructures();
    
    // *** Stats ***

    @Type( base = IStats.class )
    
    ElementProperty PROP_STATS = new ElementProperty( TYPE, "Stats" );
    
    IStats getStats();

    // *** FinancialInfo ***

    @Type( base = IFinancialInfo.class )
    
    ElementProperty PROP_FINANCIAL_INFO = new ElementProperty( TYPE, "FinancialInfo" );
    
    IFinancialInfo getFinancialInfo();
}
