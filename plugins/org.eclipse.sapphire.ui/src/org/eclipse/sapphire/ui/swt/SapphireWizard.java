/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt;

import static org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil.toImageDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.sapphire.modeling.IExecutableModelElement;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.SapphirePart.ImageChangedEvent;
import org.eclipse.sapphire.ui.SapphirePartEvent;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphireWizardPagePart;
import org.eclipse.sapphire.ui.SapphireWizardPart;
import org.eclipse.sapphire.ui.def.ISapphireWizardDef;
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

public class SapphireWizard<M extends IExecutableModelElement> extends Wizard
{
    private final M element;
    private final SapphireWizardPart part;
    private final SapphirePartListener listener;
    
    public SapphireWizard( final M modelElement,
                           final String wizardDefPath )
    {
        this.element = modelElement;
        
        final ISapphireWizardDef definition = SapphireUiDefFactory.getWizardDef( wizardDefPath );
        
        this.part = new SapphireWizardPart();
        this.part.init( null, this.element, definition, Collections.<String,String>emptyMap() );
        
        setWindowTitle( this.part.getLabel() );
        
        this.listener = new SapphirePartListener()
        {
            @Override
            public void handleEvent( final SapphirePartEvent event )
            {
                if( event instanceof ImageChangedEvent )
                {
                    refreshImage();
                }
            }
        };
        
        this.part.addListener( this.listener );
        
        refreshImage();
        
        setNeedsProgressMonitor( true );
    }
    
    public final M getModelElement()
    {
        return this.element;
    }
    
    @Override
    public void addPages()
    {
        for( SapphireWizardPagePart pagePart : this.part.getPages() )
        {
            addPage( new SapphireWizardPage( pagePart ) );
        }
    }

    @Override
    public final boolean performFinish()
    {
        DelayedTasksExecutor.sweep();
        
        final Status[] result = new Status[ 1 ];
        
        final IRunnableWithProgress runnable = new IRunnableWithProgress()
        {
            public void run( final IProgressMonitor monitor ) throws InvocationTargetException
            {
                result[ 0 ] = performFinish( ProgressMonitorBridge.create( monitor ) );
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
        
        final Status st = result[ 0 ];
        
        if( st.ok() )
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
    
    protected Status performFinish( final ProgressMonitor monitor )
    {
        return this.element.execute( monitor );
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
    
    private final void refreshImage()
    {
        setDefaultPageImageDescriptor( toImageDescriptor( this.part.getImage() ) );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        this.part.removeListener( this.listener );
    }
    
}
