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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.forms.ContainerPart;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.WithPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class RestoreDefaultsActionHandler extends SapphireActionHandler 
{
    public static final String ID = "Sapphire.Restore.Defaults";
    
    @Text( "Restore Defaults" )
    private static LocalizableText dialogTitle;
    
    @Text( "Confirm properties that should be reset to their default state. Properties that are not checked will not be modified." )
    private static LocalizableText dialogMessage;
    
    @Text( "All properties in this section are presently in their default state." )
    private static LocalizableText nothingToDoMessage;

    static 
    {
        LocalizableText.init( RestoreDefaultsActionHandler.class );
    }
    
    public RestoreDefaultsActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected Object run( final Presentation context )
    {
        final FormComponentPresentation p = (FormComponentPresentation) context;
        final SapphirePart part = (SapphirePart) getPart();

        final Set<Property> properties = new LinkedHashSet<Property>();
        collectProperties( part, properties );

        for( Iterator<Property> itr = properties.iterator(); itr.hasNext(); )
        {
            if( itr.next().empty() )
            {
                itr.remove();
            }
        }
        
        if( properties.isEmpty() )
        {
            MessageDialog.openInformation( p.shell(), dialogTitle.text(), nothingToDoMessage.text() );
        }
        else
        {
            final Set<Property> selectedProperties = PromptDialog.open( p.shell(), properties );
            
            for( Property property : selectedProperties )
            {
                property.clear();
            }
        }
        
        return null;
    }
    
    public static void collectProperties( final SapphirePart part,
                                          final Set<Property> result )
    {
        if( part.visible() )
        {
            if( part instanceof ContainerPart )
            {
                for( FormComponentPart child : ( (ContainerPart<?>) part ).children().visible() )
                {
                    collectProperties( child, result );
                }
            }
            else if( part instanceof WithPart )
            {
                final WithPart w = ( (WithPart) part );
                result.add( w.property() );
            }
            else if( part instanceof PropertyEditorPart )
            {
                final PropertyEditorPart editor = (PropertyEditorPart) part;
                
                if( ! editor.isReadOnly() )
                {
                    result.add( editor.property() );
                }
                
                for( SapphirePart related : editor.getRelatedContent() )
                {
                    collectProperties( related, result );
                }
            }
        }
    }
    
    private static final class PromptDialog extends Dialog
    {
        private final Set<Property> allProperties;
        private final Set<Property> selectedProperties;
        
        private static Set<Property> open( final Shell shell,
                                                   final Set<Property> properties )
        {
            final PromptDialog dialog = new PromptDialog( shell, properties );
            
            if( dialog.open() == Window.OK )
            {
                return dialog.selectedProperties;
            }
            else
            {
                return Collections.emptySet();
            }
        }
        
        private PromptDialog( final Shell shell,
                              final Set<Property> properties )
        {
            super( shell );
            
            this.allProperties = properties;
            this.selectedProperties = new HashSet<Property>( this.allProperties );
        }
        
        @Override
        protected boolean isResizable()
        {
            return true;
        }

        @Override
        protected Control createDialogArea( final Composite parent )
        {
            getShell().setText( dialogTitle.text() );
            
            final Composite composite = (Composite) super.createDialogArea( parent );
            
            final Label messageLabel = new Label( composite, SWT.WRAP );
            messageLabel.setText( dialogMessage.text() );
            messageLabel.setLayoutData( gdwhint( gdhfill(), 300 ) );
            
            final CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList( composite, SWT.BORDER );
            final Table table = tableViewer.getTable();
            
            table.setLayoutData( gdhhint( gdwhint( gdfill(), 300 ), 300 ) );
            
            tableViewer.setContentProvider
            (
                new IStructuredContentProvider()
                {
                     public Object[] getElements( final Object inputElement )
                     {
                         return PromptDialog.this.allProperties.toArray();
                     }

                    public void inputChanged( final Viewer viewer,
                                              final Object oldInput,
                                              final Object newInput )
                    {
                    }
                     
                    public void dispose()
                    {
                    }
                }
            );
            
            tableViewer.setLabelProvider
            (
                new ITableLabelProvider()
                {
                    public String getColumnText( final Object element,
                                                 final int columnIndex )
                    {
                        final PropertyDef property = ( (Property) element ).definition();
                        return property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false );
                    }

                    public Image getColumnImage( final Object element,
                                                 final int columnIndex )
                    {
                        return null;
                    }

                    public boolean isLabelProperty( final Object element,
                                                    final String property )
                    {
                        return false;
                    }

                    public void addListener( final ILabelProviderListener listener )
                    {
                    }

                    public void removeListener( final ILabelProviderListener listener )
                    {
                    }
                    
                    public void dispose()
                    {
                    }
                }
            );
            
            tableViewer.setCheckStateProvider
            (
                new ICheckStateProvider()
                {
                    public boolean isChecked( final Object element )
                    {
                        return PromptDialog.this.selectedProperties.contains( element );
                    }

                    public boolean isGrayed( final Object element )
                    {
                        return false;
                    }
                }
            );
            
            tableViewer.addCheckStateListener
            (
                new ICheckStateListener()
                {
                    public void checkStateChanged( final CheckStateChangedEvent event )
                    {
                        final Property property = (Property) event.getElement();
                        
                        if( event.getChecked() == true )
                        {
                            PromptDialog.this.selectedProperties.add( property );
                        }
                        else
                        {
                            PromptDialog.this.selectedProperties.remove( property );
                        }
                    }
                }
            );
            
            tableViewer.setInput( this );
            
            return composite;
        }
    }
    
}

