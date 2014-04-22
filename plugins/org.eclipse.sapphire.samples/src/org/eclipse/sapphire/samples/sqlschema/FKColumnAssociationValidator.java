/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.sqlschema;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.samples.sqlschema.ForeignKey.ColumnAssociation;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FKColumnAssociationValidator extends ValidationService 
{
    @Text( "Foreign key column types do not match" )
    private static LocalizableText error;
    
    static
    {
        LocalizableText.init( FKColumnAssociationValidator.class );
    }
    
    private ColumnAssociation association;
    private ReferenceService localColumnReferenceService;
    private Column localColumn;
    private ReferenceService referencedColumnReferenceService;
    private Column referencedColumn;
    private Listener referenceServiceListener;
    private Listener columnTypeListener;

    @Override
    protected void initValidationService()
    {
        this.referenceServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.columnTypeListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refresh();
            }
        };
        
        this.association = context( ColumnAssociation.class );
        
        this.localColumnReferenceService = this.association.getLocalColumn().service( ReferenceService.class );
        this.localColumnReferenceService.attach( this.referenceServiceListener );
        
        this.referencedColumnReferenceService = this.association.getReferencedColumn().service( ReferenceService.class );
        this.referencedColumnReferenceService.attach( this.referenceServiceListener );
    }
    
    @Override
    protected Status compute() 
    {
        this.localColumn = column( this.association.getLocalColumn(), this.localColumn );
        this.referencedColumn = column( this.association.getReferencedColumn(), this.referencedColumn );
        
        if( this.localColumn != null && this.referencedColumn != null )
        {
            final ColumnType localColumnType = this.localColumn.getType().content();
            final ColumnType referencedColumnType = this.referencedColumn.getType().content();
            
            if( localColumnType != null && referencedColumnType != null && localColumnType != referencedColumnType )
            {
                return Status.createErrorStatus( error.text() );
            }
        }
        
        return Status.createOkStatus();
    }

    private Column column( final ReferenceValue<String,Column> property, final Column cached )
    {
        final Column current = property.resolve();
        
        if( cached != current )
        {
            if( cached != null && ! cached.disposed() )
            {
                cached.getType().detach( this.columnTypeListener );
            }
            
            if( current != null )
            {
                current.getType().attach( this.columnTypeListener );
            }
        }
        
        return current;
    }

    @Override
    public void dispose()
    {
        this.association = null;
        
        this.localColumnReferenceService.detach( this.referenceServiceListener );
        this.localColumnReferenceService = null;
        
        this.referencedColumnReferenceService.detach( this.referenceServiceListener );
        this.referencedColumnReferenceService = null;
        
        this.referenceServiceListener = null;
        
        if( this.localColumn != null && ! this.localColumn.disposed() )
        {
            this.localColumn.getType().detach( this.columnTypeListener );
            this.localColumn = null;
        }
        
        if( this.referencedColumn != null && ! this.referencedColumn.disposed() )
        {
            this.referencedColumn.getType().detach( this.columnTypeListener );
            this.referencedColumn = null;
        }
        
        this.columnTypeListener = null;
    }

}
