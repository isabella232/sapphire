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

package org.eclipse.sapphire.ui.swt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphirePropertyPage 

    extends PropertyPage
    
{
    private final String compositeDefPath;
    private IModelElement modelElement = null;
    
    public SapphirePropertyPage( final String compositeDefPath )
    {
        this.compositeDefPath = compositeDefPath;
    }

    protected abstract IModelElement createModelElement();
    
    @Override
    public void createControl( final Composite parent )
    {
        super.createControl( parent );
        getDefaultsButton().setVisible( false );
        parent.layout( true, true );
    }

    protected Control createContents( final Composite parent ) 
    {
        this.modelElement = createModelElement();
        
        final SapphireControl control 
            = new SapphireControl( parent, this.modelElement, this.compositeDefPath );
        
        final Runnable messageUpdateOperation = new Runnable()
        {
            public void run()
            {
                final Status st = control.getPart().getValidationState();
                
                if( st.severity() == Status.Severity.ERROR )
                {
                    setMessage( st.message(), ERROR );
                    setValid( false );
                }
                else if( st.severity() == Status.Severity.WARNING )
                {
                    setMessage( st.message(), WARNING );
                    setValid( true );
                }
                else
                {
                    setMessage( null );
                    setValid( true );
                }
            }
        };
        
        messageUpdateOperation.run();
        
        final SapphirePartListener messageUpdateListener = new SapphirePartListener()
        {
            @Override
            public void handleValidateStateChange( final Status oldValidateState,
                                                   final Status newValidationState )
            {
                messageUpdateOperation.run();
            }
        };
        
        control.getPart().addListener( messageUpdateListener );
        
        return control;
    }
    
    @Override
    public boolean performOk() 
    {
        try
        {
            this.modelElement.resource().save();
            
            return true;
        }
        catch( ResourceStoreException e )
        {
            MessageDialog.openError( getShell(), Resources.errorDialogTitle, e.getMessage() );
            
            return false;
        }
    }
    
    @Override
    protected void performApply() 
    {
        performOk();
    }

    private static final class Resources
    
        extends NLS
        
    {
        public static String errorDialogTitle;
        
        static
        {
            initializeMessages( SapphirePropertyPage.class.getName(), Resources.class );
        }
    }
    
}
