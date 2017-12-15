/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.sqlschema.internal;

import org.eclipse.sapphire.DerivedValueService;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.samples.sqlschema.Column;
import org.eclipse.sapphire.samples.sqlschema.PrimaryKey;
import org.eclipse.sapphire.samples.sqlschema.Table;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PartOfPrimaryKeyDerivedValueService extends DerivedValueService 
{
    private Listener listener;
    
    protected void initDerivedValueService()
    {
        this.listener =  new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refresh();
            }
        };
        
        context( Table.class ).attach( this.listener, "PrimaryKey/Columns/Name" );
    }

    @Override
    protected String compute() 
    {
        final Column column = context( Column.class );
        final Table table = column.nearest( Table.class );
        
        final PrimaryKey pk = table.getPrimaryKey().content();
        
        if( pk != null)
        {
            for( final PrimaryKey.Column pkcol : pk.getColumns() )
            {
                final String name = pkcol.getName().content();
                
                if( name != null && name.equals( column.getName().content() ) )
                {
                    return Boolean.TRUE.toString();
                }        
            }
        }
        
        return Boolean.FALSE.toString();
    }

    @Override
    public void dispose()
    {
        context( Table.class ).detach( this.listener, "PrimaryKey/Columns/Name" );
        super.dispose();
    }
    
}
