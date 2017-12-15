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

package org.eclipse.sapphire.ui.swt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.FormComponentDef;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphirePropertyPage extends PropertyPage
{
    private IModelElement element;
    private boolean elementInstantiatedLocally;
    private DefinitionLoader.Reference<FormComponentDef> definition;
    
    public SapphirePropertyPage( final ModelElementType type,
                                 final DefinitionLoader.Reference<FormComponentDef> definition )
    {
        init( type, definition );
    }

    public SapphirePropertyPage( final IModelElement element,
                                 final DefinitionLoader.Reference<FormComponentDef> definition )
    {
        init( element, definition );
    }

    protected SapphirePropertyPage()
    {
    }
    
    protected void init( final ModelElementType type,
                         final DefinitionLoader.Reference<FormComponentDef> definition )
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( definition == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.elementInstantiatedLocally = true;
        
        init( type.instantiate(), definition );
    }
    
    protected void init( final IModelElement element,
                         final DefinitionLoader.Reference<FormComponentDef> definition )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( definition == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.element = element;
        this.definition = definition;
    }
    
    public final IModelElement element()
    {
        return this.element;
    }
    
    public final FormComponentDef definition()
    {
        return this.definition.resolve();
    }

    @Override
    public void createControl( final Composite parent )
    {
        super.createControl( parent );
        getDefaultsButton().setVisible( false );
        parent.layout( true, true );
    }

    protected Control createContents( final Composite parent ) 
    {
        final SapphireForm form = new SapphireForm( parent, this.element, this.definition );
        
        final Runnable messageUpdateOperation = new Runnable()
        {
            public void run()
            {
                final Status st = form.part().validation();
                
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
        
        form.part().attach
        (
            new FilteredListener<PartValidationEvent>()
            {
                @Override
                protected void handleTypedEvent( PartValidationEvent event )
                {
                    messageUpdateOperation.run();
                }
            }
        );
        
        return form;
    }
    
    @Override
    public boolean performOk() 
    {
        try
        {
            this.element.resource().save();
            
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
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.element != null )
        {
            if( this.elementInstantiatedLocally )
            {
                this.element.dispose();
            }

            this.element = null;
        }
        
        if( this.definition != null )
        {
            this.definition.dispose();
            this.definition = null;
        }
    }

    private static final class Resources extends NLS
    {
        public static String errorDialogTitle;
        
        static
        {
            initializeMessages( SapphirePropertyPage.class.getName(), Resources.class );
        }
    }
    
}
