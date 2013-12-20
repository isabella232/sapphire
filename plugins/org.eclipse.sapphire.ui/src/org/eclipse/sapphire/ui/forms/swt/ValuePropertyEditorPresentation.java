/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ValuePropertyEditorPresentation extends PropertyEditorPresentation
{
    private final ModifyPropertyValueTask modifyPropertyTask;
    protected boolean updatingModel;
    protected boolean updatingEditor;
    
    public ValuePropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
        
        this.modifyPropertyTask = new ModifyPropertyValueTask();
    }
    
    @Override
    public Value<?> property()
    {
        return (Value<?>) super.property();
    }
    
    protected final void setPropertyValue( final String value )
    {
        setPropertyValue( value, true );
    }
    
    protected final void setPropertyValue( final String value, final boolean async )
    {
        if( async )
        {
            this.modifyPropertyTask.setValue( value );
            DelayedTasksExecutor.schedule( this.modifyPropertyTask );
        }
        else
        {
            boolean rollback = false;
            
            this.updatingModel = true;
            
            try
            {
                try
                {
                    property().write( value );
                }
                catch( Exception e )
                {
                    final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                    
                    if( editFailedException != null )
                    {
                        rollback = true;
                    }
                    else
                    {
                        Sapphire.service( LoggingService.class ).log( e );
                    }
                }
            }
            finally
            {
                this.updatingModel = false;
            }
        
            if( rollback )
            {
                handlePropertyChangedEvent();
            }
        }
    }
    
    private final class ModifyPropertyValueTask extends DelayedTasksExecutor.Task
    {
        private String value = null;
        
        public synchronized String getValue()
        {
            return this.value;
        }
        
        public synchronized void setValue( final String value )
        {
            this.value = value;
        }
        
        public int getPriority()
        {
            return 100;
        }
        
        public void run()
        {
            setPropertyValue( getValue(), false );
        }
    };
    
}
