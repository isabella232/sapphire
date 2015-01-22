/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.sqlschema;

import org.eclipse.sapphire.Collation;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementReference;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface PrimaryKey extends Element 
{
	ElementType TYPE = new ElementType( PrimaryKey.class );
	
    // *** Column ***
    
    interface Column extends Element
    {
        ElementType TYPE = new ElementType( Column.class );
        
        // *** Name ***
        
        @Reference( target = org.eclipse.sapphire.samples.sqlschema.Column.class )
        @ElementReference( list = "../../Columns", key = "Name" )
        @Required
        @MustExist
        @Unique
        @Collation( ignoreCaseDifferences = "true" )
        
        ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
        
        ReferenceValue<String,org.eclipse.sapphire.samples.sqlschema.Column> getName();
        void setName( String value );
        void setName( org.eclipse.sapphire.samples.sqlschema.Column value );
    }
    
    @Type( base = Column.class )
    @CountConstraint( min = 1 )
    @PossibleValues( property = "../Columns/Name" )
    
    ListProperty PROP_COLUMNS = new ListProperty( TYPE, "Columns" );
    
    ElementList<Column> getColumns();

}
