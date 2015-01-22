/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [404482] CheckBoxListPropertyEditorRenderer needs a listener
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.makeTableSortable;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.suppressDashedTableEntryBorder;
import static org.eclipse.sapphire.util.CollectionsUtil.equalsBasedOnEntryIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImageService;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.ValueImageService;
import org.eclipse.sapphire.services.ValueLabelService;
import org.eclipse.sapphire.ui.ListSelectionService;
import org.eclipse.sapphire.ui.ListSelectionService.ListSelectionChangedEvent;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.internal.ColumnSortComparator;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class CheckBoxListPropertyEditorPresentation extends ListPropertyEditorPresentation
{
    @Text( "<empty>" )
    private static LocalizableText emptyIndicator;
    
    static
    {
        LocalizableText.init( CheckBoxListPropertyEditorPresentation.class );
    }

    private Table table;
    private CheckboxTableViewer tableViewer;
    private ElementType memberType;
    private ValueProperty memberProperty;
    private PossibleValuesService possibleValuesService;
    private Listener possibleValuesServiceListener;

    public CheckBoxListPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    
    protected void createContents( final Composite parent )
    {
        // Initialize
        
        final PropertyEditorPart part = part();
        final Property property = part.property();
        
        this.memberType = property.definition().getType();
        
        final SortedSet<PropertyDef> allMemberProperties = this.memberType.properties();
        
        if( allMemberProperties.size() == 1 )
        {
            final PropertyDef prop = allMemberProperties.first();
            
            if( prop instanceof ValueProperty )
            {
                this.memberProperty = (ValueProperty) prop;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        else
        {
            throw new IllegalStateException();
        }
        
        this.possibleValuesService = property.service( PossibleValuesService.class );
        
        this.possibleValuesServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
            	CheckBoxListPropertyEditorPresentation.this.tableViewer.refresh();
            }
        };
        
        this.possibleValuesService.attach( this.possibleValuesServiceListener );

        // Create Controls
        
        final Composite mainComposite = createMainComposite( parent );
        mainComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
        
        final PropertyEditorAssistDecorator decorator = createDecorator( mainComposite );
        decorator.control().setLayoutData( gdvalign( gd(), SWT.TOP ) );
        
        // Setting the whint in the following code is a hacky workaround for the problem
        // tracked by the following JFace bug:
        //
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997
        //
        
        final Composite tableComposite = new Composite( mainComposite, SWT.NONE );
        tableComposite.setLayoutData( gdwhint( gdfill(), 1 ) );
        
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableComposite.setLayout( tableColumnLayout );
        
        this.tableViewer = CheckboxTableViewer.newCheckList( tableComposite, SWT.BORDER | SWT.FULL_SELECTION );
        this.table = this.tableViewer.getTable();
        
        final TableViewerColumn viewerColumn = new TableViewerColumn( this.tableViewer, SWT.NONE );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( this.memberProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        tableColumnLayout.setColumnData( column, new ColumnWeightData( 1, 100, true ) );
        
        decorator.addEditorControl( mainComposite );
        
        suppressDashedTableEntryBorder( this.table );
        
        // Bind to Model
        
        final ColumnSortComparator comparator = new ColumnSortComparator()
        {
            @Override
            protected String convertToString( final Object obj )
            {
                return ( (Entry) obj ).value;
            }
        };
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            private List<Entry> entries = new ArrayList<Entry>();
            
            public Object[] getElements( final Object input )
            {
                if( this.entries != null )
                {
                    for( Entry entry : this.entries )
                    {
                        entry.dispose();
                    }
                    
                    this.entries = null;
                }
                
                final Map<String,LinkedList<Element>> valueToElements = new HashMap<String,LinkedList<Element>>();
                
                for( final Element element : property() )
                {
                    final String value = readMemberProperty( element );
                    LinkedList<Element> elements = valueToElements.get( value );
                    
                    if( elements == null )
                    {
                        elements = new LinkedList<Element>();
                        valueToElements.put( value, elements );
                    }
                    
                    elements.add( element );
                }
                
                this.entries = new ArrayList<Entry>();
                
                Set<String> possibleValues;
                
                try
                {
                    possibleValues = CheckBoxListPropertyEditorPresentation.this.possibleValuesService.values();
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    possibleValues = SetFactory.empty();
                }

                for( String value : possibleValues )
                {
                    final Entry entry;
                    final LinkedList<Element> elements = valueToElements.get( value );
                    
                    if( elements == null )
                    {
                        entry = new Entry( value, null );
                    }
                    else
                    {
                        final Element element = elements.remove();
                        
                        if( elements.isEmpty() )
                        {
                            valueToElements.remove( value );
                        }
                        
                        entry = new Entry( value, element );
                    }
                    
                    this.entries.add( entry );
                }
                
                for( Map.Entry<String,LinkedList<Element>> entry : valueToElements.entrySet() )
                {
                    final String value = entry.getKey();
                    
                    for( Element element : entry.getValue() )
                    {
                        this.entries.add( new Entry( value, element ) );
                    }
                }
                
                return this.entries.toArray();
            }
            
            public void dispose()
            {
                for( Entry entry : this.entries )
                {
                    entry.dispose();
                }
                
                this.entries = null;
            }

            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }
        };
        
        this.tableViewer.setContentProvider( contentProvider );
        
        final ColumnLabelProvider labelProvider = new ColumnLabelProvider()
        {
            @Override
            public String getText( final Object element )
            {
                return ( (Entry) element ).label();
            }

            @Override
            public Image getImage( final Object element )
            {
                return ( (Entry) element ).image();
            }
            
            @Override
            public Color getForeground( final Object element )
            {
                return ( (Entry) element ).foreground();
            }
        };
        
        viewerColumn.setLabelProvider( labelProvider );
        
        final ICheckStateProvider checkStateProvider = new ICheckStateProvider()
        {
            public boolean isChecked( final Object element )
            {
                return ( (Entry) element ).selected();
            }
            
            public boolean isGrayed( final Object element )
            {
                return false;
            }
        };
        
        this.tableViewer.setCheckStateProvider( checkStateProvider );
        
        if( part.getRenderingHint( PropertyEditorDef.HINT_SHOW_HEADER, true ) == true )
        {
            this.table.setHeaderVisible( true );

            makeTableSortable
            (
                this.tableViewer,
                Collections.<TableColumn,Comparator<Object>>singletonMap( column, comparator ),
                CheckBoxListPropertyEditorPresentation.this.possibleValuesService.ordered() ? null : column
            );
        }
        
        final ListSelectionService selectionService = part.service( ListSelectionService.class );
        
        this.tableViewer.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( final SelectionChangedEvent event )
                {
                    selectionService.select( getSelectedElements() );
                }
            }
        );
        
        final Listener selectionServiceListener = new FilteredListener<ListSelectionChangedEvent>()
        {
            @Override
            protected void handleTypedEvent( final ListSelectionChangedEvent event )
            {
                setSelectedElements( event.after() );
            }
        };

        selectionService.attach( selectionServiceListener );

        addOnDisposeOperation
        ( 
            new Runnable()
            {
                public void run()
                {
                    selectionService.detach( selectionServiceListener );
                    
                    if( CheckBoxListPropertyEditorPresentation.this.possibleValuesService != null )
                    {
                    	CheckBoxListPropertyEditorPresentation.this.possibleValuesService.detach( CheckBoxListPropertyEditorPresentation.this.possibleValuesServiceListener );
                    }
                }
            }
        );
        
        this.tableViewer.addCheckStateListener
        (
            new ICheckStateListener()
            {
                public void checkStateChanged( final CheckStateChangedEvent event )
                {
                    final Entry entry = (Entry) event.getElement();
                    
                    entry.flip();
                    selectionService.select( getSelectedElements() );
                }
            }
        );
        
        this.table.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseDoubleClick( final MouseEvent event )
                {
                    Entry entry = null;
                    
                    for( TableItem item : CheckBoxListPropertyEditorPresentation.this.table.getItems() )
                    {
                        if( item.getBounds().contains( event.x, event.y ) )
                        {
                            entry = (Entry) item.getData();
                            break;
                        }
                    }
                    
                    if( entry != null )
                    {
                        entry.flip();
                        selectionService.select( getSelectedElements() );
                    }
                }
            }
        );
        
        // Finish Up
        
        this.tableViewer.setInput( new Object() );
        addControl( this.table );
    }

    @Override
    protected boolean canScaleVertically()
    {
        return true;
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.table.setFocus();
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        refreshTable();
    }
    
    @Override
    protected void handleChildPropertyEvent( final PropertyContentEvent event )
    {
        super.handleChildPropertyEvent( event );
        refreshTable();
    }
    
    public final Element getSelectedElement()
    {
        final IStructuredSelection sel = (IStructuredSelection) CheckBoxListPropertyEditorPresentation.this.tableViewer.getSelection();
        
        if( sel == null )
        {
            return null;
        }
        else
        {
            return ( (Entry) sel.getFirstElement() ).element;
        }
    }
    
    public final List<Element> getSelectedElements()
    {
        final IStructuredSelection sel = (IStructuredSelection) CheckBoxListPropertyEditorPresentation.this.tableViewer.getSelection();
        final ListFactory<Element> elements = ListFactory.start();
        
        if( sel != null )
        {
            for( Iterator<?> itr = sel.iterator(); itr.hasNext(); )
            {
                final Element element = ( (Entry) itr.next() ).element;
                
                if( element != null )
                {
                    elements.add( element );
                }
            }
        }
        
        return elements.result();
    }
    
    public final void setSelectedElements( final List<Element> elements )
    {
        if( ! equalsBasedOnEntryIdentity( getSelectedElements(), elements ) )
        {
            final ListFactory<Entry> entries = ListFactory.start();
            
            for( Element element : elements )
            {
                for( TableItem item : this.table.getItems() )
                {
                    final Entry entry = (Entry) item.getData();
                    
                    if( entry.element == element )
                    {
                        entries.add( entry );
                    }
                }
            }
            
            this.tableViewer.setSelection( new StructuredSelection( entries.result() ) );
        }
    }
    
    private void refreshTable()
    {
        final int oldItemCount = this.table.getItemCount();
        final Entry oldSelection = (Entry) ( (IStructuredSelection) this.tableViewer.getSelection() ).getFirstElement();
        
        this.tableViewer.refresh();
        
        final int newItemCount = this.table.getItemCount();
        
        if( oldSelection != null )
        {
            final String oldSelectionValue = oldSelection.value;
            Entry newSelection = null;
            
            for( int i = 0; i < newItemCount && newSelection == null; i++ )
            {
                final Entry entry = (Entry) this.table.getItem( i ).getData();
                
                if( oldSelectionValue.equals( entry.value ) )
                {
                    newSelection = entry;
                }
            }
            
            if( newSelection != null )
            {
                this.tableViewer.setSelection( new StructuredSelection( newSelection ) );
            }
        }
        
        if( oldItemCount != newItemCount )
        {
            this.table.getParent().layout( true, true );
        }
    }

    private ElementType getMemberType()
    {
        return this.memberType;
    }
    
    private ValueProperty getMemberProperty()
    {
        return this.memberProperty;
    }
    
    private String readMemberProperty( final Element element )
    {
        final String text = element.property( this.memberProperty ).text();
        return ( text == null ? "" : text );
    }
    
    public final class Entry
    {
        private final LocalizationService localizationService;
        private String value;
        private Element element;
        private Value<?> property;
        private ValueLabelService valueLabelService;
        private ValueImageService valueImageService;
        private ImageService elementImageService;
        private Listener elementImageServiceListener;
        private Listener propertyValidationListener;
        
        public Entry( final String value, final Element element )
        {
            this.localizationService = part().definition().adapt( LocalizationService.class );
            this.value = value;
            
            this.valueLabelService = CheckBoxListPropertyEditorPresentation.this.memberProperty.service( ValueLabelService.class );
            this.valueImageService = CheckBoxListPropertyEditorPresentation.this.memberProperty.service( ValueImageService.class );
            
            rebase( element );
        }
        
        private void rebase( final Element element )
        {
            if( this.element != null )
            {
                if( this.elementImageService != null )
                {
                    this.elementImageService.detach( this.elementImageServiceListener );
                }
                
                if( this.elementImageService != null || this.valueImageService != null )
                {
                    this.property.detach( this.propertyValidationListener );
                }
            }
            
            this.element = element;
            
            if( this.element != null )
            {
                this.property = this.element.property( getMemberProperty() );
                this.elementImageService = this.element.service( ImageService.class );
                
                if( this.elementImageService != null )
                {
                    if( this.elementImageServiceListener == null )
                    {
                        this.elementImageServiceListener = new Listener()
                        {
                            @Override
                            public void handle( final Event event )
                            {
                                CheckBoxListPropertyEditorPresentation.this.tableViewer.update( Entry.this, null );
                            }
                        };
                    }
                    
                    this.elementImageService.attach( this.elementImageServiceListener );
                }
                
                if( this.elementImageService != null || this.valueImageService != null )
                {
                    if( this.propertyValidationListener == null )
                    {
                        this.propertyValidationListener = new FilteredListener<PropertyValidationEvent>()
                        {
                            @Override
                            protected void handleTypedEvent( final PropertyValidationEvent event )
                            {
                                CheckBoxListPropertyEditorPresentation.this.tableViewer.update( Entry.this, null );
                            }
                        };
                    }
                    
                    this.property.attach( this.propertyValidationListener );
                }
            }
            else
            {
                this.property = null;
            }
        }
        
        public String label()
        {
            String label = null;
        
            if( this.value.length() == 0 )
            {
                label = emptyIndicator.text();
            }
            else
            {
                try
                {
                    label = this.valueLabelService.provide( this.value );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
                
                if( label == null )
                {
                    label = this.value;
                }
                else if( ! label.equals( this.value ) )
                {
                    label = this.localizationService.transform( label, CapitalizationType.FIRST_WORD_ONLY, false );
                }
            }
            
            return label;
        }

        public Image image()
        {
            ImageData image = null;
            
            if( this.elementImageService != null )
            {
                image = this.elementImageService.image();
            }
            else
            {
                try
                {
                    image = this.valueImageService.provide( this.value );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
                
                if( image == null )
                {
                    image = getMemberType().image();
                }
            }
            
            if( this.property == null )
            {
                return resources().image( image );
            }
            else
            {
                return resources().image( image, this.property.validation().severity() );
            }
        }
        
        public Color foreground()
        {
            Color color = null;
            
            if( this.value.length() == 0 )
            {
                color = Display.getCurrent().getSystemColor( SWT.COLOR_DARK_GRAY );
            }
            
            return color;
        }
        
        public boolean selected()
        {
            return ( this.element != null );
        }
        
        public void flip()
        {
            final ElementList<?> list = property();
            
            if( this.element == null )
            {
                final Disposable suspension = list.suspend();
                
                try
                {
                    rebase( list.insert() );
                    this.property.write( this.value, true );
                }
                finally
                {
                    suspension.dispose();
                }
            }
            else
            {
                // Must null the element field before trying to remove the element as remove will 
                // trigger property change event and it is possible for the resulting refresh to 
                // set the element field to a new value before returning.
                
                final Element el = this.element;
                rebase( null );
                list.remove( el );
            }
        }
        
        public void dispose()
        {
            rebase( null );
        }
    }
    
    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            return new CheckBoxListPropertyEditorPresentation( part, parent, composite );
        }
    }
    
    public static final class EnumFactory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            final Property property = part.property();
            
            if( property.definition() instanceof ListProperty &&
                property.service( PossibleTypesService.class ).types().size() == 1 )
            {
                final SortedSet<PropertyDef> properties = property.definition().getType().properties();
                
                if( properties.size() == 1 )
                {
                    final PropertyDef memberProperty = properties.first();
                    
                    if( memberProperty instanceof ValueProperty &&
                        memberProperty.hasAnnotation( Unique.class ) &&
                        Enum.class.isAssignableFrom( memberProperty.getTypeClass() ) )
                    {
                        return new CheckBoxListPropertyEditorPresentation( part, parent, composite );
                    }
                }
            }
    
            return null;
        }
    }
    
}
