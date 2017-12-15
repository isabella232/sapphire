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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.sapphire.modeling.IExecutableModelElement;
import org.eclipse.sapphire.ui.def.ISapphireWizardDef;
import org.eclipse.sapphire.ui.def.ISapphireWizardPageDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireWizard<M extends IExecutableModelElement>

    extends Wizard
    
{
    private final M modelElement;
    private final ISapphireWizardDef definition;
    
    public SapphireWizard( final M modelElement,
                           final String wizardDefPath )
    {
        this.modelElement = modelElement;
        this.definition = SapphireUiDefFactory.getWizardDef( wizardDefPath );
        
        setWindowTitle( this.definition.getLabel().getContent() );
        
        final ImageDescriptor imageDescriptor = this.definition.getImage().resolve();
        
        if( imageDescriptor != null )
        {
            setDefaultPageImageDescriptor( imageDescriptor );
        }
        
        setNeedsProgressMonitor( true );
    }
    
    public final M getModelElement()
    {
        return this.modelElement;
    }
    
    @Override
    public final void addPages()
    {
        for( ISapphireWizardPageDef pageDef : this.definition.getPageDefs() )
        {
            final SapphireWizardPage page = new SapphireWizardPage( this.modelElement, pageDef );
            addPage( page );
        }
    }

    @Override
    public final boolean performFinish()
    {
        final IStatus[] result = new IStatus[ 1 ];
        
        final IRunnableWithProgress runnable = new IRunnableWithProgress()
        {
            public void run( final IProgressMonitor monitor ) throws InvocationTargetException
            {
                result[ 0 ] = SapphireWizard.this.modelElement.execute( monitor );
            }
        };
        
        try
        {
            getContainer().run( true, false, runnable );
        }
        catch( InvocationTargetException e )
        {
            SapphireUiFrameworkPlugin.log( e.getTargetException() );
            return false;
        }
        catch( InterruptedException e )
        {
            return false;
        }
        
        final IStatus st = result[ 0 ];
        
        if( st.isOK() )
        {
            performPostFinish();
            
            return true;
        }
        else
        {
            SapphireStatusDialog.open( getShell(), st );
            
            return false;
        }
    }
    
    protected void performPostFinish()
    {
    	// The default implementation doesn't do anything.
    }
    
	protected final void openFileEditors( final IFile... files )
	{
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        
        if( window != null )
        {
            final IWorkbenchPage page = window.getActivePage();
            
            for( IFile file : files )
            {
            	if( file != null && file.isAccessible() )
            	{
	                try
	                {
	                    IDE.openEditor( page, file );
	                } 
	                catch( PartInitException e ) 
	                {
	                    SapphireUiFrameworkPlugin.log( e );
	                }
            	}
            }
        }
	}
    
}
