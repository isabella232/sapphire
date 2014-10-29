/******************************************************************************
 * Copyright (c) 2014 Oracle
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
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface Table extends Element 
{
    ElementType TYPE = new ElementType( Table.class );
    
    // *** Name ***
    
    @XmlBinding( path = "name" )
    @Label( standard = "name" )
    @Required
    @Unique
    @Collation( ignoreCaseDifferences = "true" )

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );

    // *** Columns ***
    
    @Type( base = Column.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "column", type = Column.class ) )
    @CountConstraint( min = 1 )
    
    ListProperty PROP_COLUMNS = new ListProperty( TYPE, "Columns" );
    
    ElementList<Column> getColumns();    

    // *** PrimaryKey ***
    
    @Type( base = PrimaryKey.class )
    
    ElementProperty PROP_PRIMARY_KEY = new ElementProperty( TYPE, "PrimaryKey" );
    
    ElementHandle<PrimaryKey> getPrimaryKey();

    // *** ForeignKeys ***
    
    @Type( base = ForeignKey.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "foreign-key", type = ForeignKey.class ) )

    ListProperty PROP_FOREIGN_KEYS = new ListProperty( TYPE, "ForeignKeys" );
    
    ElementList<ForeignKey> getForeignKeys();
    
}
