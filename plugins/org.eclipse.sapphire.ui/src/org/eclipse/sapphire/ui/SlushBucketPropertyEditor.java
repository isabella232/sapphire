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

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdwhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.makeTableSortable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesChangedEvent;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderImpl;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderListener;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.renderers.swt.AbstractSlushBucketPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRendererFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SlushBucketPropertyEditor

    extends AbstractSlushBucketPropertyEditorRenderer
    
{
    private ModelElementType memberType = null;
    private ValueProperty memberProperty = null;
    private PossibleValuesProviderImpl valuesProvider = null;
    private PossibleValuesProviderListener valuesProviderListener = null;
    private TableViewer sourceTableViewer = null;
    private Table sourceTable = null;
    private AddAction addAction = null;
    
    public SlushBucketPropertyEditor( final SapphireRenderingContext context,
                                      final SapphirePropertyEditor part )
    {
        super( context, part );
        
        this.memberType = getProperty().getType();
        this.memberProperty = (ValueProperty) this.memberType.getProperties().get( 0 );
        this.valuesProvider = part.getModelElement().service().getPossibleValuesProvider( this.memberProperty );
        
        setAddActionDesired( false );
    }

    public Control createSourceControl( final Composite parent )
    {
        final SapphirePropertyEditor part = getPart();

        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( glayout( 1, 0, 0 ) );
        
        // Setting the whint in the following code is a hacky workaround for the problem
        // tracked by the following JFace bug:
        //
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997
        //
        
        final Composite innerComposite = new Composite( composite, SWT.NONE );
        innerComposite.setLayoutData( gdwhint( gdfill(), 1 ) );
        
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        innerComposite.setLayout( tableColumnLayout );
        
        this.sourceTableViewer = new TableViewer( innerComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
        this.sourceTable = this.sourceTableViewer.getTable();
        this.sourceTable.setHeaderVisible( true );
        
        final TableViewerColumn viewerColumn = new TableViewerColumn( this.sourceTableViewer, SWT.NONE );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( this.memberProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        tableColumnLayout.setColumnData( column, new ColumnWeightData( 1, 100, true ) );
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                if( SlushBucketPropertyEditor.this.valuesProvider == null )
                {
                    return new Object[ 0 ];
                }
                
                final ModelElementList<IModelElement> list = getList();
                
                if( list == null )
                {
                    return new Object[ 0 ];
                }

                final Collection<String> allValues;
                
                try
                {
                    allValues = SlushBucketPropertyEditor.this.valuesProvider.getPossibleValues();
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                    return new Object[ 0 ];
                }
                
                final Set<String> valuesToRemove = new HashSet<String>();
                
                for( IModelElement member : list )
                {
                    final Value<?> value = (Value<?>) SlushBucketPropertyEditor.this.memberProperty.invokeGetterMethod( member );
                    final String str = value.getText();
                    
                    if( str != null )
                    {
                        valuesToRemove.add( str );
                    }
                }
                
                final Set<String> values = new HashSet<String>( allValues );
                values.removeAll( valuesToRemove );
                
                return values.toArray();
            }

            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }
            
            public void dispose()
            {
            }
        };
        
        this.sourceTableViewer.setContentProvider( contentProvider );
        
        final Image memberTypeImage = part.getImageCache().getImage( this.memberType );
        
        final ColumnLabelProvider labelProvider = new ColumnLabelProvider()
        {
            @Override
            public String getText( final Object element )
            {
                return (String) element;
            }
            
            @Override
            public Image getImage( final Object element )
            {
                return memberTypeImage;
            }
        };
        
        viewerColumn.setLabelProvider( labelProvider );
        
        makeTableSortable( this.sourceTableViewer );
        
        this.sourceTable.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseDoubleClick( final MouseEvent event )
                {
                    handleSourceTableDoubleClickEvent( event );
                }
            }
        );
        
        this.sourceTable.addFocusListener
        (
            new FocusAdapter()
            {
                @Override
                public void focusGained( final FocusEvent event )
                {
                    handleSourceTableFocusGainedEvent();
                }
            }
        );
        
        this.sourceTable.addKeyListener
        (
            new KeyAdapter()
            {
                @Override
                public void keyPressed( final KeyEvent event )
                {
                    if( event.character == ' ' )
                    {
                        handleSourceTableEnterKeyPressEvent();
                    }
                }
            }
        );
        
        this.valuesProviderListener = new PossibleValuesProviderListener()
        {
            @Override
            public void handlePossibleValuesChangedEvent( final PossibleValuesChangedEvent event )
            {
                SlushBucketPropertyEditor.this.sourceTableViewer.refresh();
                SlushBucketPropertyEditor.this.addAction.notifyChangeListeners();
            }
        };
        
        this.valuesProvider.addListener( this.valuesProviderListener );
        
        this.sourceTableViewer.setInput( new Object() );
        
        return composite;
    }

    @Override
    public Action createAddAction()
    {
        this.addAction = new AddAction();
        
        final ISelectionChangedListener listener = new ISelectionChangedListener()
        {
            public void selectionChanged( final SelectionChangedEvent event )
            {
                final List<String> input = new ArrayList<String>();
                
                for( Iterator<?> itr = ( (IStructuredSelection) event.getSelection() ).iterator(); itr.hasNext(); )
                {
                    input.add( (String) itr.next() );
                }
                
                SlushBucketPropertyEditor.this.addAction.setInput( input );
            }
        };
        
        this.sourceTableViewer.addSelectionChangedListener( listener );
        
        return this.addAction;
    }

    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        
        this.sourceTableViewer.refresh();
        this.addAction.notifyChangeListeners();
    }

    @Override
    protected void handleListElementChangedEvent( final ModelPropertyChangeEvent event )
    {
        super.handleListElementChangedEvent( event );
        
        this.sourceTableViewer.refresh();
        this.addAction.notifyChangeListeners();
    }
    
    @Override
    protected void handleTableFocusGainedEvent()
    {
        super.handleTableFocusGainedEvent();
        
        this.sourceTableViewer.setSelection( StructuredSelection.EMPTY );
    }
    
    @Override
    protected void handleDisposeEvent()
    {
        super.handleDisposeEvent();
        
        if( this.valuesProviderListener != null )
        {
            this.valuesProvider.removeListener( this.valuesProviderListener );
        }
    }

    private void handleSourceTableDoubleClickEvent( final MouseEvent event )
    {
        String doubleClickedItem = null;
        
        for( TableItem item : this.sourceTable.getItems() )
        {
            if( item.getBounds().contains( event.x, event.y ) )
            {
                doubleClickedItem = (String) item.getData();
                break;
            }
        }
        
        if( doubleClickedItem != null )
        {
            this.addAction.setInput( Collections.singleton( doubleClickedItem ) );
            this.addAction.execute( getUiContext().getShell() );
        }
    }
    
    private void handleSourceTableFocusGainedEvent()
    {
        setSelection( Collections.<IModelElement>emptyList() );
        
        if( this.sourceTableViewer.getSelection().isEmpty() && this.sourceTable.getItemCount() > 0 )
        {
            final String firstItem = (String) this.sourceTable.getItem( 0 ).getData();
            this.sourceTableViewer.setSelection( new StructuredSelection( firstItem ) );
        }
    }
    
    private void handleSourceTableEnterKeyPressEvent()
    {
        if( ! this.sourceTableViewer.getSelection().isEmpty() )
        {
            this.addAction.execute( getUiContext().getShell() );
        }
    }
    
    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            final ModelProperty property = propertyEditorDefinition.getProperty();
            
            if( property instanceof ListProperty && property.hasAnnotation( NoDuplicates.class ) )
            {
                final ListProperty listProperty = (ListProperty) property;
                
                if( listProperty.getAllPossibleTypes().size() == 1 )
                {
                    final ModelElementType memberType = listProperty.getType();
                    final List<ModelProperty> properties = memberType.getProperties();
                    
                    if( properties.size() == 1 )
                    {
                        final ModelProperty memberProperty = properties.get( 0 );
                        
                        if( memberProperty instanceof ValueProperty && ( (ValueProperty) memberProperty ).hasPossibleValuesProvider() )
                        {
                            return true;
                        }
                    }
                }
            }
    
            return false;
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new SlushBucketPropertyEditor( context, part );
        }
    }
    
    private final class AddAction
    
        extends Action
        
    {
        private Collection<String> input = Collections.emptyList();
        
        public void setInput( final Collection<String> input )
        {
            this.input = input;
            notifyChangeListeners();
        }
        
        @Override
        public boolean isEnabled()
        {
            final ModelElementList<IModelElement> list = getList();
            
            if( list != null )
            {
                return ! this.input.isEmpty();
            }
            
            return false;
        }

        @Override
        protected Object run( final Shell shell )
        {
            final ModelElementList<IModelElement> list = getList();
            
            if( list != null )
            {
                final List<IModelElement> items = new ArrayList<IModelElement>();
                
                for( String str : this.input )
                {
                    final IModelElement item = list.addNewElement();
                    SlushBucketPropertyEditor.this.memberProperty.invokeSetterMethod( item, str );
                    items.add( item );
                }
                
                setSelection( items );
                setFocusOnTable();
            }
            
            return null;
        }
    };

}
