/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.sqlschema;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class FKColumnAssociationValidator extends ValidationService 
{
    @Text( "Foreign key column types do not match. Column \"{0}\" is type \"{1}\". Reference Column \"{2}\" is type \"{3}\"" )
    private static LocalizableText invalidTypes;
    
    static
    {
        LocalizableText.init( FKColumnAssociationValidator.class );
    }
	
	private Listener listener;
	private Table referencedTable;

    @Override
    protected void initValidationService()
    {
		this.listener = new FilteredListener<PropertyContentEvent>()
		{
			@Override
			protected void handleTypedEvent( final PropertyContentEvent event )
			{
				refresh();
			}
		};
    	
        Table table = context( Table.class );
        table.attach(this.listener, "Columns/Type");
        
        ForeignKey fkey = context(ForeignKey.class);
        fkey.attach(listener, "ColumnAssociations/*");
                
        fkey.getReferencedTable().attach
        (
            new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    Table refTable = context(ForeignKey.class).getReferencedTable().resolve();
                    if (refTable != null && refTable != referencedTable)
                    {
                    	if (referencedTable != null)
                    	{
                    		referencedTable.detach(listener, "Columns/Type");
                    	}
                    	referencedTable = refTable;
                    	referencedTable.attach(listener, "Columns/Type");
                    	refresh();
                    }
                }
            }
        );
        Table refTable = fkey.getReferencedTable().resolve();
        if (refTable != null)
        {
        	refTable.attach(this.listener, "Columns/Type");
        }
        
    }
	
	@Override
	protected Status compute() 
	{
		ForeignKey fkey = context(ForeignKey.class);
		final Status.CompositeStatusFactory factory = Status.factoryForComposite();
		for (ForeignKey.ColumnAssociation columnAssociation : fkey.getColumnAssociations())
		{
			Column localCol = columnAssociation.getLocalColumn().resolve();
			Column referencedCol = columnAssociation.getReferencedColumn().resolve();
			if (localCol != null && referencedCol != null)
			{
				ColumnType localType = localCol.getType().content();
				ColumnType referredType = referencedCol.getType().content();
				if (localType != referredType)
				{
					String msg = invalidTypes.format(localCol.getName().content(), localCol.getType().content(), 
							referencedCol.getName().content(), referencedCol.getType().content());
					factory.merge(Status.createErrorStatus(msg));
				}
			}
		}
		return factory.create();
	}

	@Override
	public void dispose()
	{
        Table table = context( Table.class );
        if (!table.disposed())
        {
        	table.detach(this.listener, "Columns/Type");
        }
        super.dispose();
	}

}
