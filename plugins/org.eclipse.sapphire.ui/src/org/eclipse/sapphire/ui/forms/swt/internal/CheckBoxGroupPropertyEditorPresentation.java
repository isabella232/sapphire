/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImageService;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.ValueImageService;
import org.eclipse.sapphire.services.ValueLabelService;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.DelayedTasksExecutor.Task;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.ListPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CheckBoxGroupPropertyEditorPresentation extends ListPropertyEditorPresentation
{
    @Text( "<empty>" )
    private static LocalizableText emptyIndicator;
    
    static
    {
        LocalizableText.init( CheckBoxGroupPropertyEditorPresentation.class );
    }

    private final Orientation orientation;
    private ValueProperty memberProperty;
    private PossibleValuesService possibleValuesService;
    private ValueLabelService valueLabelService;
    private ValueImageService valueImageService;
    private LocalizationService localizationService;
    private Listener serviceListener;
    private Composite checkBoxesComposite;

    public CheckBoxGroupPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite, final Orientation orientation )
    {
        super( part, parent, composite );
        
        this.orientation = orientation;
    }

    @Override
    
    protected void createContents( final Composite parent )
    {
        final ElementList<?> list = property();
        
        final ElementType memberType = list.definition().getType();
        final SortedSet<PropertyDef> allMemberProperties = memberType.properties();
        
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

        final Composite mainComposite = createMainComposite( parent );
        mainComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );

        final PropertyEditorAssistDecorator decorator = createDecorator( mainComposite );
        decorator.control().setLayoutData( gdvindent( gdvalign( gd(), ( this.orientation == Orientation.HORIZONTAL ? SWT.CENTER : SWT.TOP ) ), ( this.orientation == Orientation.HORIZONTAL ? 0 : 4 ) ) );
        
        this.checkBoxesComposite = new Composite( mainComposite, SWT.NONE );
        this.checkBoxesComposite.setLayoutData( gdvalign( gd(), SWT.CENTER ) );
        
        final RowLayout checkBoxesCompositeLayout = new RowLayout();
        checkBoxesCompositeLayout.type = ( this.orientation == Orientation.HORIZONTAL ? SWT.HORIZONTAL : SWT.VERTICAL );
        checkBoxesCompositeLayout.wrap = false;
        checkBoxesCompositeLayout.marginTop = 0;
        checkBoxesCompositeLayout.marginBottom = 0;
        checkBoxesCompositeLayout.marginLeft = 0;
        checkBoxesCompositeLayout.marginRight = 0;
        checkBoxesCompositeLayout.spacing = ( this.orientation == Orientation.HORIZONTAL ? 10 : 5 );
        
        this.checkBoxesComposite.setLayout( checkBoxesCompositeLayout );
        
        this.serviceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refreshCheckBoxes();
            }
        };
        
        this.localizationService = part().definition().adapt( LocalizationService.class );
        
        this.possibleValuesService = list.service( PossibleValuesService.class );
        this.possibleValuesService.attach( this.serviceListener );
        
        this.valueLabelService = this.memberProperty.service( ValueLabelService.class );
        this.valueLabelService.attach( this.serviceListener );
        
        this.valueImageService = this.memberProperty.service( ValueImageService.class );
        this.valueImageService.attach( this.serviceListener );
        
        final Listener modelListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                if( event instanceof PropertyContentEvent || event instanceof PropertyValidationEvent )
                {
                    refreshCheckBoxes();
                }
            }
        };
        
        list.attach( modelListener, this.memberProperty.name() );
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                @Override
                public void run()
                {
                    CheckBoxGroupPropertyEditorPresentation.this.possibleValuesService.detach( CheckBoxGroupPropertyEditorPresentation.this.serviceListener );
                    CheckBoxGroupPropertyEditorPresentation.this.valueLabelService.detach( CheckBoxGroupPropertyEditorPresentation.this.serviceListener );
                    CheckBoxGroupPropertyEditorPresentation.this.valueImageService.detach( CheckBoxGroupPropertyEditorPresentation.this.serviceListener );
                    
                    if( ! list.disposed() )
                    {
                        list.detach( modelListener, CheckBoxGroupPropertyEditorPresentation.this.memberProperty.name() );
                        
                        for( final Element element : list )
                        {
                            final ImageService imageService = element.service( ImageService.class );
                            
                            if( imageService != null )
                            {
                                imageService.detach( CheckBoxGroupPropertyEditorPresentation.this.serviceListener );
                            }
                        }
                    }
                }
            }
        );
        
        refreshCheckBoxes();
    }
    
    private void refreshCheckBoxes()
    {
        final ElementList<?> list = property();
        
        // Build a list of existing check boxes.
        
        final List<Button> checkboxes = new ArrayList<Button>();
        
        for( final Control control : this.checkBoxesComposite.getChildren() )
        {
            checkboxes.add( (Button) control );
        }
        
        // Build a map of value to existing elements.
        
        final Map<String,LinkedList<Element>> valueToElements = new HashMap<String,LinkedList<Element>>();
        
        for( final Element element : list )
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
        
        // Retrieve the set of possible values.
        
        Set<String> possibleValues;
        
        try
        {
            possibleValues = possibleValuesService.values();
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
            possibleValues = SetFactory.empty();
        }
        
        // Create or rebase check boxes for all possible values and all elements.
        
        int position = 0;
        
        for( final String value : possibleValues )
        {
            final LinkedList<Element> elements = valueToElements.get( value );
            
            if( elements == null )
            {
                createOrRebaseCheckBox( checkboxes, position, value, null );
                position++;
            }
            else
            {
                for( final Element element : elements )
                {
                    createOrRebaseCheckBox( checkboxes, position, value, element );
                    position++;
                }
                
                valueToElements.remove( value );
            }
        }
        
        for( final Iterator<Map.Entry<String,LinkedList<Element>>> itr = valueToElements.entrySet().iterator(); itr.hasNext(); )
        {
            final Map.Entry<String,LinkedList<Element>> entry = itr.next();
            final String value = entry.getKey();
            
            if( value != null )
            {
                for( final Element element : entry.getValue() )
                {
                    createOrRebaseCheckBox( checkboxes, position, value, element );
                    position++;
                }
                
                itr.remove();
            }
        }

        for( final Map.Entry<String,LinkedList<Element>> entry : valueToElements.entrySet() )
        {
            final String value = entry.getKey();
            
            for( final Element element : entry.getValue() )
            {
                createOrRebaseCheckBox( checkboxes, position, value, element );
                position++;
            }
        }
        
        // Dispose of extra check boxes.
        
        for( int i = position, n = checkboxes.size(); i < n; i++ )
        {
            checkboxes.get( i ).dispose();
        }
        
        // Re-layout everything since the number of check boxes may have changed and their labels may have changed.
        
        layout();
    }
    
    private void createOrRebaseCheckBox( final List<Button> checkboxes, final int position, final String value, final Element element )
    {
        final Button checkbox;
        
        if( position < checkboxes.size() )
        {
            checkbox = checkboxes.get( position );
        }
        else
        {
            checkbox = createCheckBox();
        }
        
        rebaseCheckBox( checkbox, value, element );
    }
    
    private Button createCheckBox()
    {
        final Button checkbox = new Button( this.checkBoxesComposite, SWT.CHECK );
        
        checkbox.addSelectionListener
        (
            new SelectionAdapter()
            {
                @Override
                public void widgetSelected( final SelectionEvent event )
                {
                    final Task task = new Task()
                    {
                        @Override
                        public void run()
                        {
                            if( checkbox.isDisposed() )
                            {
                                return;
                            }
                            
                            final ElementList<?> list = property();
                            final boolean selection = checkbox.getSelection();
                            Element e = (Element) checkbox.getData( "Element" );
                            
                            if( e == null && selection == true )
                            {
                                final Disposable s = list.suspend();
                                
                                try
                                {
                                    e = list.insert();
                                    writeMemberProperty( e, (String) checkbox.getData( "Value" ) );
                                }
                                finally
                                {
                                    s.dispose();
                                }
                            }
                            else if( e != null && selection == false )
                            {
                                list.remove( e );
                            }
                        }
                    };
                    
                    DelayedTasksExecutor.schedule( task );
                }
            }
        );
        
        addControl( checkbox );
        
        this.decorator.addEditorControl( checkbox );
        
        return checkbox;
    }

    private void rebaseCheckBox( final Button checkbox, final String value, final Element element )
    {
        checkbox.setData( "Value", value );
        checkbox.setData( "Element", element );
        checkbox.setSelection( element != null );
        
        String text = null;
        
        if( value == null )
        {
            text = emptyIndicator.text();
        }
        else
        {
            try
            {
                text = this.valueLabelService.provide( value );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
            
            if( text == null )
            {
                text = value;
            }
            else if( ! text.equals( value ) )
            {
                text = this.localizationService.transform( text, CapitalizationType.FIRST_WORD_ONLY, false );
            }
        }
        
        checkbox.setText( text );
        
        ImageService elementImageService = (ImageService) checkbox.getData( "ImageService" );
        
        if( elementImageService != null )
        {
            elementImageService.detach( this.serviceListener );
        }
        
        if( element != null )
        {
            elementImageService = element.service( ImageService.class );
            checkbox.setData( "ImageService", elementImageService );
            
            if( elementImageService != null )
            {
                elementImageService.attach( this.serviceListener );
            }
        }
        
        ImageData image = null;
        
        if( elementImageService != null )
        {
            image = elementImageService.image();
        }
        else
        {
            try
            {
                image = this.valueImageService.provide( value );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        if( element == null )
        {
            checkbox.setImage( resources().image( image ) );
        }
        else
        {
            checkbox.setImage( resources().image( image, element.property( this.memberProperty ).validation().severity() ) );
        }
    }
    
    private String readMemberProperty( final Element element )
    {
        return element.property( this.memberProperty ).text();
    }
    
    private void writeMemberProperty( final Element element, final String text )
    {
        element.property( this.memberProperty ).write( text, true );
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        for( final Control control : this.checkBoxesComposite.getChildren() )
        {
            control.setFocus();
            break;
        }
    }
    
    public static final class HorizontalFactory extends PossibleValuesListPresentationFactory
    {
        @Override
        protected boolean check( final PropertyEditorPart part )
        {
            final String style = part.definition().getStyle().content();
            
            if( style != null && ( style.equals( "Sapphire.PropertyEditor.CheckBoxGroup" ) || style.equals( "Sapphire.PropertyEditor.CheckBoxGroup.Horizontal" ) ) )
            {
                return super.check( part );
            }
            
            return false;
        }

        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            if( check( part ) )
            {
                return new CheckBoxGroupPropertyEditorPresentation( part, parent, composite, Orientation.HORIZONTAL );
            }
            
            return null;
        }
    }

    public static final class VerticalFactory extends PossibleValuesListPresentationFactory
    {
        @Override
        protected boolean check( final PropertyEditorPart part )
        {
            final String style = part.definition().getStyle().content();
            
            if( style != null && style.equals( "Sapphire.PropertyEditor.CheckBoxGroup.Vertical" ) )
            {
                return super.check( part );
            }
            
            return false;
        }

        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            if( check( part ) )
            {
                return new CheckBoxGroupPropertyEditorPresentation( part, parent, composite, Orientation.VERTICAL );
            }
            
            return null;
        }
    }
    
}
