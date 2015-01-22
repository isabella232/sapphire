/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - improved handling of primary and foreign keys
 ******************************************************************************/

package org.eclipse.sapphire.samples.sqlschema;

import org.eclipse.sapphire.Collation;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.sqlschema.internal.PartOfForeignKeyDerivedValueService;
import org.eclipse.sapphire.samples.sqlschema.internal.PartOfPrimaryKeyDerivedValueService;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface Column extends Element 
{
    ElementType TYPE = new ElementType( Column.class );
    
    // *** Name ***
    
    @Label( standard = "name" )
    @Required
    @Unique
    @Collation( ignoreCaseDifferences = "true" )
    @XmlBinding( path = "name" )

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );
    
    // *** Type ***
    
    @Type( base = ColumnType.class )
    @Label( standard = "type" )
    @Required
    @XmlBinding( path = "type" )
    
    ValueProperty PROP_TYPE = new ValueProperty( TYPE, "Type" );
    
    Value<ColumnType> getType();
    void setType( String value );
    void setType( ColumnType value );
    
    // *** Size ***
    
	@Type( base = Integer.class )
	@Label( standard = "size", full = "column size" )
	@XmlBinding( path = "size" )
	@NumericRange( min = "1" )
	@Enablement( expr = "${ Type == 'STRING' }" )
	@Required
	
	ValueProperty PROP_SIZE = new ValueProperty( TYPE, "Size" );
	
	Value<Integer> getSize();
	void setSize( String value );
	void setSize( Integer value );
	
    // *** PartOfPrimaryKey ***
    
    @Type( base = Boolean.class )
    @Derived
    @Service( impl = PartOfPrimaryKeyDerivedValueService.class )    

    ValueProperty PROP_PART_OF_PRIMARY_KEY = new ValueProperty( TYPE, "PartOfPrimaryKey" );
    
    Value<Boolean> isPartOfPrimaryKey();
	
    // *** PartOfForeignKey ***
    
    @Type( base = Boolean.class )
    @Derived
    @Service( impl = PartOfForeignKeyDerivedValueService.class )    

    ValueProperty PROP_PART_OF_FOREIGN_KEY = new ValueProperty( TYPE, "PartOfForeignKey" );
    
    Value<Boolean> isPartOfForeignKey();

}
