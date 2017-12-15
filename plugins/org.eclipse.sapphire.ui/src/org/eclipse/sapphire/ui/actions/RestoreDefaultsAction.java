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

package org.eclipse.sapphire.ui.actions;

import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdwhint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireComposite;
import org.eclipse.sapphire.ui.SapphireElementPropertyComposite;
import org.eclipse.sapphire.ui.SapphireEnumControlledPageBook;
import org.eclipse.sapphire.ui.SapphireIfElseDirective;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePartContainer;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class RestoreDefaultsAction 

    extends Action 
    
{
    public static final String ACTION_ID = "section:restore-defaults"; //$NON-NLS-1$
    
    public RestoreDefaultsAction()
    {
        setId( ACTION_ID );
        setLabel( Resources.actionLabel );
        setImageDescriptor( SapphireImageCache.ACTION_RESTORE_DEFAULTS );
    }
    
    @Override
    public boolean isVisible()
    {
        final Set<PropertyRef> properties = new HashSet<PropertyRef>();
        collectProperties( getPart(), properties );        
        
        return ! properties.isEmpty();
    }

    @Override
    public void setVisible( boolean visible )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final Object run( final Shell shell )
    {
        final ISapphirePart part = getPart();

        final Set<PropertyRef> properties = new LinkedHashSet<PropertyRef>();
        collectProperties( part, properties );

        for( Iterator<PropertyRef> itr = properties.iterator(); itr.hasNext(); )
        {
            final PropertyRef pref = itr.next();
            
            if( pref.property instanceof ValueProperty )
            {
                final Value<?> value = (Value<?>) pref.property.invokeGetterMethod( pref.modelElement );

                if( value.isDefault() )
                {
                    itr.remove();
                }
            }
            else if( pref.property instanceof ListProperty )
            {
                final ModelElementList<?> list = (ModelElementList<?>) pref.property.invokeGetterMethod( pref.modelElement );
                
                if( list.isEmpty() )
                {
                    itr.remove();
                }
            }
            else
            {
                final IModelElement child = (IModelElement) pref.property.invokeGetterMethod( pref.modelElement );
                
                if( child == null || ! ( child instanceof IRemovable ) )
                {
                    itr.remove();
                }
            }
        }
        
        if( properties.isEmpty() )
        {
            MessageDialog.openInformation( shell, Resources.dialogTitle, Resources.nothingToDoMessage );
        }
        else
        {
            final Set<PropertyRef> selectedProperties = PromptDialog.open( shell, properties );
            
            for( PropertyRef pref : selectedProperties )
            {
                if( pref.property instanceof ValueProperty )
                {
                    ( (ValueProperty) pref.property ).invokeSetterMethod( pref.modelElement, null );
                }
                else if( pref.property instanceof ListProperty )
                {
                    for( Object item : (List<?>) pref.property.invokeGetterMethod( pref.modelElement ) )
                    {
                        ( (IRemovable) item ).remove();
                    }
                }
                else if( pref.property instanceof ElementProperty )
                {
                    final IModelElement child = (IModelElement) pref.property.invokeGetterMethod( pref.modelElement );
                    ( (IRemovable) child ).remove();
                }
            }
        }
        
        return null;
    }
    
    private static void collectProperties( final ISapphirePart part,
                                           final Set<PropertyRef> result )
    {
        if( part instanceof SapphirePartContainer )
        {
            for( ISapphirePart child : ( (SapphirePartContainer) part ).getChildParts() )
            {
                collectProperties( child, result );
            }
        }
        else if( part instanceof SapphireElementPropertyComposite )
        {
            result.add( new PropertyRef( part.getModelElement(), ( (SapphireElementPropertyComposite) part ).getConditionalProperty() ) );
        }
        else if( part instanceof SapphireEnumControlledPageBook )
        {
            final SapphireComposite currentPage = ( (SapphireEnumControlledPageBook) part ).getCurrentPage();
            
            if( currentPage != null )
            {
                collectProperties( currentPage, result );
            }
        }
        else if( part instanceof SapphirePropertyEditor )
        {
            final SapphirePropertyEditor editor = (SapphirePropertyEditor) part;
            result.add( new PropertyRef( editor.getModelElement(), editor.getProperty() ) );
            
            for( SapphirePropertyEditor aux : editor.getAuxPropertyEditors() )
            {
                collectProperties( aux, result );
            }
        }
        else if( part instanceof SapphireIfElseDirective )
        {
            final SapphireIfElseDirective ifelse = (SapphireIfElseDirective) part;
            
            for( ISapphirePart child : ( ifelse.getConditionState() == true ? ifelse.getThenContent() : ifelse.getElseContent() ) )
            {
                collectProperties( child, result );
            }
        }
    }
    
    private static final class PropertyRef
    {
        public final IModelElement modelElement;
        public final ModelProperty property;
        
        public PropertyRef( final IModelElement modelElement,
                            final ModelProperty property )
        {
            this.modelElement = modelElement;
            this.property = property;
        }
    }
    
    private static final class PromptDialog
    
        extends Dialog
        
    {
        private final Set<PropertyRef> allProperties;
        private final Set<PropertyRef> selectedProperties;
        
        private static Set<PropertyRef> open( final Shell shell,
                                              final Set<PropertyRef> properties )
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
                              final Set<PropertyRef> properties )
        {
            super( shell );
            
            this.allProperties = properties;
            this.selectedProperties = new HashSet<PropertyRef>( this.allProperties );
        }
        
        @Override
        protected boolean isResizable()
        {
            return true;
        }

        @Override
        protected Control createDialogArea( final Composite parent )
        {
            getShell().setText( Resources.dialogTitle );
            
            final Composite composite = (Composite) super.createDialogArea( parent );
            
            final Label messageLabel = new Label( composite, SWT.WRAP );
            messageLabel.setText( Resources.dialogMessage );
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
                        final ModelProperty property = ( (PropertyRef) element ).property;
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
                        final PropertyRef property = (PropertyRef) event.getElement();
                        
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
    
    private static final class Resources 
    
        extends NLS
        
    {
        public static String actionLabel;
        public static String dialogTitle;
        public static String dialogMessage;
        public static String nothingToDoMessage;

        static 
        {
            initializeMessages( RestoreDefaultsAction.class.getName(), Resources.class );
        }
    }
    
}

