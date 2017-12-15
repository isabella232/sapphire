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

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_ASSIST_DECORATOR;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_BINDING;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_EXPAND_VERTICALLY;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL_ABOVE;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhindent;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileExtensions;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.SapphireCommands;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.RelativePathValueBrowseHandler.ContainersOnlyViewerFilter;
import org.eclipse.sapphire.ui.assist.RelativePathValueBrowseHandler.ExtensionBasedViewerFilter;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownComposite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EclipseWorkspacePathPropertyEditorRenderer

    extends DefaultValuePropertyEditorRenderer
    
{
    public EclipseWorkspacePathPropertyEditorRenderer( final SapphireRenderingContext context,
                                                       final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final SapphirePropertyEditor part = getPart();
        final ModelProperty property = part.getProperty();
        
        final boolean showLabelAbove = part.getRenderingHint( HINT_SHOW_LABEL_ABOVE, false );
        final int baseIndent = part.getLeftMarginHint() + 9;
        final boolean expandVertically = part.getRenderingHint( HINT_EXPAND_VERTICALLY, false );
        final int heightHint = part.getRenderingHint( ISapphirePartDef.HINT_HEIGHT, 10 ) * 15;
        
        final Text textField = (Text) super.createContents( parent, true );

        final DrillDownComposite drillDown = new DrillDownComposite( parent, SWT.BORDER );
        
        GridData gd = ( expandVertically ? gdfill() : gdhhint( gdhfill(), heightHint ) );
        gd = gdhindent( hspan( gd, showLabelAbove ? 2 : 1 ), baseIndent );
        drillDown.setLayoutData( gd );

        final TreeViewer treeViewer = new TreeViewer( drillDown, SWT.NONE );
        final Tree tree = treeViewer.getTree();
        drillDown.setChildTree( treeViewer );
        
        treeViewer.setContentProvider( new WorkbenchContentProvider() );
        treeViewer.setLabelProvider( WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider() );
        treeViewer.setSorter( new ViewerSorter() );
        
        final ValidFileSystemResourceType validFileSystemResourceTypeAnnotation
            = property.getAnnotation( ValidFileSystemResourceType.class );
    
        if( validFileSystemResourceTypeAnnotation != null )
        {
            if( validFileSystemResourceTypeAnnotation.value() == FileSystemResourceType.FOLDER )
            {
                treeViewer.addFilter( new ContainersOnlyViewerFilter() );
            }
            
            final ValidFileExtensions validFileExtensionsAnnotation = property.getAnnotation( ValidFileExtensions.class );
            
            if( validFileExtensionsAnnotation != null )
            {
                final List<String> extensions = Arrays.asList( validFileExtensionsAnnotation.value() );
                treeViewer.addFilter( new ExtensionBasedViewerFilter( extensions ) );
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
        
        treeViewer.setInput( ResourcesPlugin.getWorkspace() );
        
        final PropertyEditorAssistDecorator decorator 
            = (PropertyEditorAssistDecorator) textField.getData( DATA_ASSIST_DECORATOR );

        decorator.addEditorControl( drillDown );
        decorator.addEditorControl( tree );

        treeViewer.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( final SelectionChangedEvent event )
                {
                    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    String path = MiscUtil.EMPTY_STRING;
                    
                    if( selection != null && ! selection.isEmpty() )
                    {
                        final IResource resource = (IResource) selection.getFirstElement();
                        path = resource.getFullPath().toPortableString();
                        
                        if( path.startsWith( "/" ) && path.length() > 1 )
                        {
                            path = path.substring( 1 );
                        }
                    }
                    
                    textField.setText( path );
                }
            }
        );
    
        textField.setData( DATA_BINDING, this.binding );
        
        SapphireCommands.configurePropertyEditorContext( tree );
        addControl( tree );
        
        final IModelElement modelElement = getModelElement();
        
        if( modelElement != null )
        {
            final Value<?> value = (Value<?>) getProperty().invokeGetterMethod( modelElement );
            final String val = value.getText();
            
            if( val != null )
            {
                IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( val );
                
                if( resource instanceof IFile && validFileSystemResourceTypeAnnotation.value() == FileSystemResourceType.FOLDER )
                {
                    resource = resource.getParent();
                }
                
                treeViewer.setSelection( new StructuredSelection( resource ) );
            }
        }
    }
    
    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            return true;
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new EclipseWorkspacePathPropertyEditorRenderer( context, part );
        }
    }
    
}
