/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_DELETE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_JUMP;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL_ABOVE;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFilter;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.internal.EnhancedComposite;
import org.eclipse.sapphire.ui.internal.binding.AbstractBinding;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.TextOverlayPainter;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class CompactListPropertyEditorRenderer

    extends ListPropertyEditorRenderer
    
{
    public static final String DATA_SELECTION_PROVIDER = "selection.provider";
    
    private Runnable refreshOperation;
    
    ValueProperty memberProperty;
    private Composite innerComposite;
    private Composite textComposite;
    private List<CompactTextBinding> textBindings = new ArrayList<CompactTextBinding>();
    private SapphireFormText addText;
    private HyperlinkAdapter addTextHyperlinkAdapter;

	private Label label;
    
    public CompactListPropertyEditorRenderer( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        createContents( parent, false, false );
    }
    
    protected Control createContents( final Composite parent,
                                      final boolean suppressLabel,
                                      final boolean ignoreLeftMarginHint )
    {
        final SapphirePropertyEditor part = getPart();
        final ListProperty property = (ListProperty) part.getProperty();
        // TODO support readonly
        //final boolean isReadOnly = ( property.isReadOnly() || part.getRenderingHint( HINT_READ_ONLY, false ) );

        final boolean showLabelAbove = part.getRenderingHint( HINT_SHOW_LABEL_ABOVE, false );
        final boolean showLabelInline = part.getRenderingHint( HINT_SHOW_LABEL, ( suppressLabel ? false : ! showLabelAbove ) );
        final int leftMargin = ( ignoreLeftMarginHint ? 0 : part.getLeftMarginHint() );
        final List<ModelProperty> allMemberProperties = property.getType().getProperties();
        if( allMemberProperties.size() == 1 )
        {
            final ModelProperty prop = allMemberProperties.get( 0 );
            
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
        
        if( showLabelInline || showLabelAbove )
        {
            final String labelText = property.getLabel( false, CapitalizationType.FIRST_WORD_ONLY, true ) + ":";
            this.label = new Label( parent, SWT.NONE );
            this.label.setLayoutData( gdvindent( gdhindent( gdhspan( gdvalign( gd(), SWT.TOP ), showLabelAbove ? 2 : 1 ), leftMargin + 9 ), 4) );
            this.label.setText( labelText );
            this.context.adapt( this.label );
        }
        else
        {
        	this.label = null;
        }
        
        this.innerComposite = new EnhancedComposite( parent, SWT.NONE );
        this.innerComposite.setLayoutData( gdhindent( gdhspan( gdhfill(), showLabelInline ? 1 : 2 ), showLabelInline ? 0 : leftMargin ) );
        this.innerComposite.setLayout( glayout( 2, 0, 0, 2, 2 ) );
        this.context.adapt(this.innerComposite);

        final List<Control> relatedControls = new ArrayList<Control>();
        
        if( this.label != null ) {
            relatedControls.add( this.label );
        }
        this.innerComposite.setData( RELATED_CONTROLS, relatedControls );
        
        this.addControls( 1 );

        this.refreshOperation = new Runnable()
        {
            boolean running = false;
            
            public void run()
            {
                if( CompactListPropertyEditorRenderer.this.innerComposite.isDisposed() )
                {
                    return;
                }
                
                if( this.running == true )
                {
                    return;
                }
                
                this.running = true;
                
                try
                {
                    CompactListPropertyEditorRenderer.this.refreshControls();
                }
                finally
                {
                    this.running = false;
                }
            }
        };
        
        this.binding = new AbstractBinding( getPart(), this.context, this.innerComposite )
        {
            @Override
            protected void doUpdateModel()
            {
                // Don't do anything here. Changes to the model are pushed at cell-level.
            }

            @Override
            protected void doUpdateTarget()
            {
                // Changes are mostly synchronized at cell-level. This is only here to
                // catch the case where a full page refresh is performed perhaps because user
                // has visited the source page.
                
                CompactListPropertyEditorRenderer.this.refreshOperation.run();
            }
        };
                
        return this.innerComposite;
    }
    
    @SuppressWarnings("unchecked")
	private void addControls(int count) {
    	// first remove everything
        if (this.addText != null && this.addTextHyperlinkAdapter != null) {
    		this.addText.removeHyperlinkListener(this.addTextHyperlinkAdapter);
    	}
    	for (int i = 0; i < this.textBindings.size(); i++) {
    		this.textBindings.get(i).removeListener();
    	}
    	for (Control child : this.innerComposite.getChildren()) {
    		child.dispose();
    	}
    	this.textBindings.clear();

    	final Collection<Control> relatedControls = (Collection<Control>)this.innerComposite.getData( RELATED_CONTROLS );
    	relatedControls.clear();
        if( this.label != null ) {
            relatedControls.add( this.label );
        }
    	
        this.textComposite = new Composite( this.innerComposite, SWT.NONE );
        this.textComposite.setLayoutData( gdhspan( gdfill(), 2 ) );
        this.textComposite.setLayout( glspacing( glayout( 3, 0, 0 ), 2, 4 ) );

        // add back text controls and add link
    	for (int i = 0; i < count; i++) {
            final CompactListProxyResource resource = new CompactListProxyResource(this);
            final IModelElement proxyElement = this.memberProperty.getModelElementType().instantiate(getPart().getModelElement(), getPart().getProperty(), resource); 
            resource.init(proxyElement, this.memberProperty);
            final SapphirePropertyEditor editor = this.getPart().getChildPropertyEditor( proxyElement, this.memberProperty );
            
            CompactListTextAssistDecorator decorator = addDecorator(editor);

    		Text text = new Text(this.textComposite, SWT.BORDER);
    		text.setLayoutData( gdhindent( gdwhint( gdhfill(), 150 ), 0 ) );
    		CompactTextBinding binding = new CompactTextBinding(this, text, resource);
    		binding.setDecorator(decorator);
    		decorator.setBinding(binding);
    		this.textBindings.add(binding);
    		
    		relatedControls.add(text);

    		addToolbar(binding, editor, relatedControls);
    		
    		decorator.addEditorControl(text);
    		decorator.addEditorControl(binding.getToolbar());
    	}

    	this.addText = new SapphireFormText( this.innerComposite, SWT.NONE );
        this.addText.setLayoutData( gdhindent( gdvalign( gdhfill(), SWT.CENTER ), 10 ) );
        this.context.adapt( this.addText );
        
		relatedControls.add(this.addText);

		final StringBuilder buf = new StringBuilder();
        buf.append( "<form><p vspace=\"false\"><a href=\"action\" nowrap=\"true\">" );
        buf.append( "Add " );
        final ListProperty listProperty = (ListProperty) getPart().getProperty();
        buf.append( listProperty.getType().getLabel(false, CapitalizationType.NO_CAPS, false/*includeMnemonic*/) );
        buf.append( "</a></p></form>" );
        
        this.addText.setText( buf.toString(), true, false );
        if (this.addTextHyperlinkAdapter == null) {
        	this.addTextHyperlinkAdapter = new HyperlinkAdapter() {
            	@Override
                public void linkActivated( final HyperlinkEvent event )
                {
            		addActivated();
                	refreshDeleteActions();
                }
        	};
        }
        this.addText.addHyperlinkListener(this.addTextHyperlinkAdapter);
    }
    
    private void addToolbar(final CompactTextBinding binding, final SapphirePropertyEditor editor, final Collection<Control> relatedControls) {
        final SapphireActionGroup parentActions = new SapphireActionGroup(getPart(), getPart().getActionContext());

        final SapphireAction deleteAction = parentActions.getAction( ACTION_DELETE );
        final DeleteActionHandler deleteActionHandler = new DeleteActionHandler(binding);
        deleteActionHandler.init( deleteAction, null );
        deleteAction.addHandler( deleteActionHandler );
        
        final SapphireActionGroup actions = editor.getActions();
        actions.addAction(deleteAction);
        
        final SapphireActionHandler jumpActionHandler = actions.getAction( ACTION_JUMP ).getFirstActiveHandler();
        addJumpOverlay(jumpActionHandler, binding);

        final SapphireActionHandlerFilter assistFilter = SapphireActionSystem.createFilterByActionId( ACTION_ASSIST );
        final SapphireActionHandlerFilter jumpFilter = SapphireActionSystem.createFilterByActionId( ACTION_JUMP );
        actions.addFilter(assistFilter);
        actions.addFilter(jumpFilter);

        final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( this.context, actions );
        final SapphireToolBarActionPresentation toolBarActionsPresentation = new SapphireToolBarActionPresentation( actionPresentationManager );
        final SapphireKeyboardActionPresentation actionPresentationKeyboard = new SapphireKeyboardActionPresentation( actionPresentationManager );
        binding.setActionPresentationKeyboard(actionPresentationKeyboard);

        final ToolBar toolbar = new ToolBar( this.textComposite, SWT.FLAT | SWT.HORIZONTAL );
        toolbar.setLayoutData( gdhindent( gdvfill(), 2) );
        toolBarActionsPresentation.setToolBar( toolbar );
        toolBarActionsPresentation.render();
        this.context.adapt( toolbar );
        
        relatedControls.add(toolbar);
        
        toolbar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				deleteAction.removeHandler(deleteActionHandler);
				actionPresentationKeyboard.dispose();
			}
        });

        binding.setToolbar(toolbar);
        binding.setDeleteActionHandler(deleteActionHandler);
        
        actions.removeFilter(assistFilter);
        actions.removeFilter(jumpFilter);
    }
    
    private void addJumpOverlay(final SapphireActionHandler jumpActionHandler, final CompactTextBinding binding) {
        final TextOverlayPainter.Controller textOverlayPainterController;
        
        if( jumpActionHandler != null )
        {
            textOverlayPainterController = new TextOverlayPainter.Controller()
            {
                @Override
                public boolean isHyperlinkEnabled()
                {
                    return jumpActionHandler.isEnabled();
                }

                @Override
                public void handleHyperlinkEvent()
                {
                    jumpActionHandler.execute( CompactListPropertyEditorRenderer.this.context );
                }

                @Override
                public String getDefaultText()
                {
                	CompactListProxyResource resource = binding.getResource();
                	IModelElement element = resource.getModelElement();
                    return element == null ? null : element.read( resource.getValueProperty() ).getDefaultText();
                }
            };
        }
        else
        {
            textOverlayPainterController = new TextOverlayPainter.Controller()
            {
                @Override
                public String getDefaultText()
                {
                	CompactListProxyResource resource = binding.getResource();
                	IModelElement element = resource.getModelElement();
                    return element == null ? null : element.read( resource.getValueProperty() ).getDefaultText();
                }
            };
        }
        
        TextOverlayPainter.install( binding.getText(), textOverlayPainterController );
    }
    
    private CompactListTextAssistDecorator addDecorator(final SapphirePropertyEditor editor) {
    	final CompactListTextAssistDecorator decorator = new CompactListTextAssistDecorator(editor, this.context, this.textComposite);
    	decorator.getControl().setLayoutData( gdvindent( gdvalign( gd(), SWT.TOP ), 2 ) );
    	
        final SapphireActionGroup actions = editor.getActions();
        final SapphireAction assistAction = actions.getAction( SapphireActionSystem.ACTION_ASSIST );
        
        final SapphireActionHandler assistActionHandler = new SapphireActionHandler()
        {
            @Override
            protected Object run( final SapphireRenderingContext context )
            {
                decorator.openAssistDialog();
                return null;
            }
        };
        
        assistActionHandler.init( assistAction, null );
        assistAction.addHandler( assistActionHandler );
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    assistAction.removeHandler( assistActionHandler );
                }
            }
        );

        return decorator;
    }
    
	@SuppressWarnings("unchecked")
    private void addActivated() {
		final Collection<Control> relatedControls = (Collection<Control>)this.innerComposite.getData( RELATED_CONTROLS );

    	CompactListProxyResource resource = new CompactListProxyResource(this);
        final IModelElement proxyElement = this.memberProperty.getModelElementType().instantiate(getPart().getModelElement(), getPart().getProperty(), resource); 
        resource.init(proxyElement, this.memberProperty);
        SapphirePropertyEditor editor = this.getPart().getChildPropertyEditor( proxyElement, this.memberProperty );

        CompactListTextAssistDecorator decorator = addDecorator(editor);

		Text text = new Text(this.textComposite, SWT.BORDER);
		text.setLayoutData( gdwhint( gdhfill(), 150 ) );
		CompactTextBinding binding = new CompactTextBinding(this, text, resource);
		binding.setDecorator(decorator);
		decorator.setBinding(binding);
		this.textBindings.add(binding);
		
		relatedControls.add(text);
		
		addToolbar(binding, editor, relatedControls);

		this.context.layout();
    	text.setFocus();
    }
    
    private void refreshControls() {
    	if (!needsRefresh()) {
        	refreshDeleteActions();
    		return;
    	}
    	
        ModelElementList<IModelElement> list = this.getList();

        if (list.size() != this.textBindings.size()) {
        	this.addControls(Math.max(1, list.size()));
        	this.context.layout();
        }

        for (int i = 0; i < list.size(); i++) {
        	IModelElement elem = list.get(i);
        	this.textBindings.get(i).refreshModelElement(elem);
    	}
        if (list.size() == 0) {
    		this.textBindings.get(0).refreshModelElement(null);
        }

        refreshDeleteActions();
    }
    
    private void refreshDeleteActions() {
    	final int size = this.textBindings.size();
    	for (int i = 0; i < size ; i++) {
    		CompactTextBinding binding = this.textBindings.get(i); 
    		if (i == 0 && size == 1) {
    			binding.getDeleteActionHandler().refreshEnablement();
    		} else {
        		binding.getDeleteActionHandler().setEnabled(true);
    		}
    		binding.getDecorator().refresh();
    	}
    }
    
    private boolean needsRefresh() {
    	for (CompactTextBinding binding : this.textBindings) {
    		if (binding.isModifying()) {
    			return false;
    		}
    	}
        return true;
    }
    
	@SuppressWarnings("unchecked")
    void deleteBinding(CompactTextBinding binding) {
		final Collection<Control> relatedControls = (Collection<Control>)this.innerComposite.getData( RELATED_CONTROLS );

		IModelElement elem = binding.getModelElement();
    	Text text = binding.getText();
    	ToolBar toolbar = binding.getToolbar();
    	PropertyEditorAssistDecorator decorator = binding.getDecorator();
    	
    	binding.setModifying(true);
		
    	if (elem != null) {
    		this.getList().remove(elem);
    	}
    	
    	binding.setModifying(false);

    	if (this.textBindings.size() > 1) {
        	relatedControls.remove(toolbar);
        	relatedControls.remove(text);

        	binding.removeListener();
    		this.textBindings.remove(binding);
    		text.dispose();
    		toolbar.dispose();
    		
    		Control control = decorator.getControl();
    		decorator.removeEditorControl(control);
    		control.dispose();

        	this.context.layout();
    	} else {
    		binding.refreshModelElement(null);
    	}
    	refreshDeleteActions();
    }
    
    public void insertEmpty(final CompactTextBinding binding) {
    	for (CompactTextBinding b : this.textBindings) {
    		if (b == binding) {
    			return;
    		}
    		if (b.getModelElement() == null) {
    			final IModelElement newElement = getList().addNewElement();
    			b.setModelElement(newElement);
    		}
    	}
    }

	public static final boolean equals( Object o1, Object o2 ) {
		boolean objectsAreEqual = false;
		if (o1 == o2) {
			objectsAreEqual = true;
		} else if (o1 != null && o2 != null) {
			objectsAreEqual = o1.equals(o2);
		}

		return objectsAreEqual;
	}

    
    public final PropertyEditorAssistDecorator getDecorator()
    {
        return this.decorator;
    }
    
    public final void setDecorator( final PropertyEditorAssistDecorator decorator )
    {
        this.decorator = decorator;
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        
        CompactListPropertyEditorRenderer.this.refreshOperation.run();
    }
    
    @Override
    protected void handleListElementChangedEvent( final ModelPropertyChangeEvent event )
    {
        super.handleListElementChangedEvent( event );
        
        CompactListPropertyEditorRenderer.this.refreshOperation.run();
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
    	this.textBindings.get(0).getText().setFocus();
    }

    public final class DeleteActionHandler extends SapphireActionHandler {
    	
    	CompactTextBinding binding;

    	public DeleteActionHandler(CompactTextBinding binding) {
    		this.binding = binding;
    	}

		@Override
		protected Object run(SapphireRenderingContext context) {
			CompactListPropertyEditorRenderer.this.deleteBinding(this.binding);
			return null;
		}
    	
        public final void refreshEnablement() {
            setEnabled(this.binding.getModelElement() != null);
        }
    }
    
    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            return ( propertyEditorDefinition.getProperty() instanceof ListProperty );
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new CompactListPropertyEditorRenderer( context, part );
        }
    }
    
}
