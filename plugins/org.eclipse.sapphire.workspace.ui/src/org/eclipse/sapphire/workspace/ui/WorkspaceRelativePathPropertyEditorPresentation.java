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

package org.eclipse.sapphire.workspace.ui;

import static org.eclipse.sapphire.ui.forms.PropertyEditorPart.DATA_BINDING;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.RelativePathBrowseActionHandler.ContainersOnlyViewerFilter;
import org.eclipse.sapphire.ui.forms.swt.RelativePathBrowseActionHandler.ExtensionBasedViewerFilter;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.TextFieldPropertyEditorPresentation;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownComposite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WorkspaceRelativePathPropertyEditorPresentation extends TextFieldPropertyEditorPresentation
{
    public WorkspaceRelativePathPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = part();
        final Value<?> value = (Value<?>) part.property();
        final Element element = value.element();
        
        final Text textField = (Text) super.createContents( parent, true );

        final Composite drillDownParent = createMainComposite
        ( 
            parent,
            new CreateMainCompositeDelegate( part )
            {
                @Override
                public boolean getShowLabel()
                {
                    return false;
                }
            }
        );
        
        drillDownParent.setLayout( glayout( 1, 9, 0, 0, 0 ) );

        final DrillDownComposite drillDown = new DrillDownComposite( drillDownParent, SWT.BORDER );
        drillDown.setLayoutData( gdfill() );

        final TreeViewer treeViewer = new TreeViewer( drillDown, SWT.NONE );
        final Tree tree = treeViewer.getTree();
        drillDown.setChildTree( treeViewer );
        
        treeViewer.setContentProvider( new WorkbenchContentProvider() );
        treeViewer.setLabelProvider( WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider() );
        treeViewer.setSorter( new ViewerSorter() );
        
        final ValidFileSystemResourceType validFileSystemResourceTypeAnnotation
            = value.definition().getAnnotation( ValidFileSystemResourceType.class );
    
        if( validFileSystemResourceTypeAnnotation != null )
        {
            if( validFileSystemResourceTypeAnnotation.value() == FileSystemResourceType.FOLDER )
            {
                treeViewer.addFilter( new ContainersOnlyViewerFilter() );
            }
            
            final FileExtensionsService fileExtensionsService = value.service( FileExtensionsService.class );
            
            if( fileExtensionsService != null )
            {
                final ExtensionBasedViewerFilter filter = new ExtensionBasedViewerFilter( fileExtensionsService.extensions() );
                
                treeViewer.addFilter( filter );
                
                final Listener listener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        filter.change( fileExtensionsService.extensions() );
                        treeViewer.refresh();
                    }
                };
                
                fileExtensionsService.attach( listener );
                
                tree.addDisposeListener
                (
                    new DisposeListener()
                    {
                        public void widgetDisposed( final DisposeEvent event )
                        {
                            fileExtensionsService.detach( listener );
                        }
                    }
                );
            }
        }
        
        treeViewer.addDoubleClickListener
        (
            new IDoubleClickListener() 
            {
                public void doubleClick( final DoubleClickEvent event ) 
                {
                    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    
                    if( selection != null )
                    {
                        final Object item = selection.getFirstElement();
                        
                        if( treeViewer.getExpandedState( item ) )
                        {
                            treeViewer.collapseToLevel( item, 1 );
                        }
                        else
                        {
                            treeViewer.expandToLevel( item, 1 );
                        }
                    }
                }
            }
        );
        
        final IContainer root;
        
        if( element instanceof CreateWorkspaceFileOp )
        {
            root = ( (CreateWorkspaceFileOp) element ).getRoot().resolve();
        }
        else
        {
            root = ResourcesPlugin.getWorkspace().getRoot();
        }
        
        treeViewer.setInput( root );
        
        this.decorator.addEditorControl( drillDown );
        this.decorator.addEditorControl( tree );

        final String val = value.text();
        
        if( val != null )
        {
            IPath path = new Path( val );
            IResource resource = root.findMember( val );
            
            while( resource == null )
            {
                path = path.removeLastSegments( 1 );
                resource = root.findMember( path );
            }
            
            if( resource instanceof IFile && validFileSystemResourceTypeAnnotation.value() == FileSystemResourceType.FOLDER )
            {
                resource = resource.getParent();
            }
            
            treeViewer.setSelection( new StructuredSelection( resource ) );
        }

        treeViewer.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( final SelectionChangedEvent event )
                {
                    final IResource resource;
                    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    
                    if( selection == null || selection.isEmpty() )
                    {
                        resource = (IResource) treeViewer.getInput();
                    }
                    else
                    {
                        resource = (IResource) selection.getFirstElement();
                    }
                    
                    String path = resource.getFullPath().makeRelativeTo( root.getFullPath() ).toString();
                    
                    if( path.startsWith( "/" ) && path.length() > 1 )
                    {
                        path = path.substring( 1 );
                    }
                    
                    textField.setText( path );
                }
            }
        );
    
        textField.setData( DATA_BINDING, this.binding );
        
        addControl( tree );
    }
    
    @Override
    protected boolean canScaleVertically()
    {
        return true;
    }

    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            return new WorkspaceRelativePathPropertyEditorPresentation( part, parent, composite );
        }
    }
    
}
