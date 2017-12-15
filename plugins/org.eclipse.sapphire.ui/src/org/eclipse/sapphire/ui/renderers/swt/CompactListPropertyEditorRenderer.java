/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [343677] Element property validation is not surfaced by with directive
 *    Konstantin Komissarchik - [338857] List property editor doesn't disable add action button when the list property is disabled
 *    Konstantin Komissarchik - [392773] IllegalArgumentException while selecting outline node in gallery
 ******************************************************************************/

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_DELETE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_JUMP;
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
import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.modeling.BindingImpl;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.XmlValueBindingImpl;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFilter;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.internal.binding.AbstractBinding;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.TextOverlayPainter;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CompactListPropertyEditorRenderer extends ListPropertyEditorRenderer
{
    public static final String DATA_SELECTION_PROVIDER = "selection.provider";
    
    private Runnable refreshOperation;
    
    ValueProperty memberProperty;
    private Composite mainComposite;
    private Composite textComposite;
    private List<TextBinding> textBindings = new ArrayList<TextBinding>();
    private SapphireFormText addText;
    private HyperlinkAdapter addTextHyperlinkAdapter;
    
    public CompactListPropertyEditorRenderer( final SapphireRenderingContext context,
                                              final PropertyEditorPart part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = getPart();
        final ListProperty property = (ListProperty) part.getProperty();
        // TODO support readonly
        //final boolean isReadOnly = ( property.isReadOnly() || part.getRenderingHint( HINT_READ_ONLY, false ) );

        final List<ModelProperty> allMemberProperties = property.getType().properties();
        
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
        
        this.mainComposite = createMainComposite( parent );
        this.mainComposite.setLayout( glayout( 2, 0, 0, 2, 2 ) );

        addControls( 1 );

        this.refreshOperation = new Runnable()
        {
            boolean running = false;
            
            public void run()
            {
                if( CompactListPropertyEditorRenderer.this.mainComposite.isDisposed() )
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
        
        this.binding = new AbstractBinding( getPart(), this.context, this.mainComposite )
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
    }
    
    private void addControls(int count) {
        // first remove everything
        if (this.addText != null && this.addTextHyperlinkAdapter != null) {
            this.addText.removeHyperlinkListener(this.addTextHyperlinkAdapter);
        }
        for (int i = 0; i < this.textBindings.size(); i++) {
            this.textBindings.get(i).removeListener();
        }
        for (Control child : this.mainComposite.getChildren()) {
            child.dispose();
        }
        this.textBindings.clear();

        this.textComposite = new Composite( this.mainComposite, SWT.NONE );
        this.textComposite.setLayoutData( gdhspan( gdfill(), 2 ) );
        this.textComposite.setLayout( glspacing( glayout( 3, 0, 0 ), 2, 4 ) );
        
        addControl( this.textComposite );

        // add back text controls and add link
        for (int i = 0; i < count; i++) {
            final ProxyResource resource = new ProxyResource();
            final IModelElement proxyElement = this.memberProperty.getModelElementType().instantiate(getList(), getPart().getProperty(), resource); 
            resource.init(proxyElement, this.memberProperty);
            final PropertyEditorPart editor = this.getPart().getChildPropertyEditor( proxyElement, this.memberProperty );
            
            PropertyEditorAssistDecorator decorator = addDecorator(editor);

            Text text = new Text(this.textComposite, SWT.BORDER);
            text.setLayoutData( gdhindent( gdwhint( gdhfill(), 150 ), 0 ) );
            TextBinding binding = new TextBinding(text, resource);
            binding.setDecorator(decorator);
            this.textBindings.add(binding);

            addControl( text );

            addToolbar(binding, editor);
            
            decorator.addEditorControl(text);
            decorator.addEditorControl(binding.getToolbar());
        }

        this.addText = new SapphireFormText( this.mainComposite, SWT.NONE );
        this.addText.setLayoutData( gdhindent( gdvalign( gdhfill(), SWT.CENTER ), 10 ) );
        this.context.adapt( this.addText );
        
        addControl(this.addText);

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
    
    private void addToolbar(final TextBinding binding, final PropertyEditorPart editor) {
        final SapphireActionGroup parentActions = new SapphireActionGroup(getPart(), getPart().getActionContext());

        final SapphireAction deleteAction = parentActions.getAction( ACTION_DELETE );
        deleteAction.setGroup(null);
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
        
        addControl(toolbar);
        
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
    
    private void addJumpOverlay(final SapphireActionHandler jumpActionHandler, final TextBinding binding) {
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
                    ProxyResource resource = binding.getResource();
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
                    ProxyResource resource = binding.getResource();
                    IModelElement element = resource.getModelElement();
                    return element == null ? null : element.read( resource.getValueProperty() ).getDefaultText();
                }
            };
        }
        
        TextOverlayPainter.install( binding.getText(), textOverlayPainterController );
    }
    
    private PropertyEditorAssistDecorator addDecorator(final PropertyEditorPart editor) {
        final PropertyEditorAssistDecorator decorator = new PropertyEditorAssistDecorator(editor, this.context, this.textComposite);
        decorator.control().setLayoutData( gdvindent( gdvalign( gd(), SWT.TOP ), 2 ) );

        return decorator;
    }
    
    private void addActivated() {
        ProxyResource resource = new ProxyResource();
        final IModelElement proxyElement = this.memberProperty.getModelElementType().instantiate(getList(), getPart().getProperty(), resource); 
        resource.init(proxyElement, this.memberProperty);
        PropertyEditorPart editor = this.getPart().getChildPropertyEditor( proxyElement, this.memberProperty );

        PropertyEditorAssistDecorator decorator = addDecorator(editor);

        Text text = new Text(this.textComposite, SWT.BORDER);
        text.setLayoutData( gdwhint( gdhfill(), 150 ) );
        TextBinding binding = new TextBinding(text, resource);
        binding.setDecorator(decorator);
        this.textBindings.add(binding);
        
        addControl(text);
        
        addToolbar(binding, editor);
        
        decorator.addEditorControl(text);
        decorator.addEditorControl(binding.getToolbar());

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
            TextBinding binding = this.textBindings.get(i); 
            if (i == 0 && size == 1) {
                binding.getDeleteActionHandler().refreshEnablement();
            } else {
                binding.getDeleteActionHandler().setEnabled(true);
            }
        }
    }
    
    private boolean needsRefresh() {
        for (TextBinding binding : this.textBindings) {
            if (binding.isModifying()) {
                return false;
            }
        }
        return true;
    }
    
    void deleteBinding(TextBinding binding) {
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
            binding.removeListener();
            this.textBindings.remove(binding);
            text.dispose();
            toolbar.dispose();
            
            Control control = decorator.control();
            decorator.removeEditorControl(control);
            control.dispose();

            this.context.layout();
        } else {
            binding.refreshModelElement(null);
        }
        refreshDeleteActions();
    }
    
    public void insertEmpty(final TextBinding binding) {
        for (TextBinding b : this.textBindings) {
            if (b == binding) {
                return;
            }
            if (b.getModelElement() == null) {
                final IModelElement newElement = getList().insert();
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
    protected void handleListElementChangedEvent( final Event event )
    {
        super.handleListElementChangedEvent( event );
        this.refreshOperation.run();
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.textBindings.get(0).getText().setFocus();
    }

    private final class DeleteActionHandler extends SapphireActionHandler {
        
        TextBinding binding;

        public DeleteActionHandler(TextBinding binding) {
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
    
    private final class ProxyResource extends XmlResource 
    {
        private IModelElement proxyElement;
        private IModelElement actualElement;
        private ValueProperty actualProperty;
        
        private String value;

        public ProxyResource() {
            super((XmlResource)getPart().getModelElement().resource());
        }

        public void init(IModelElement proxyElement, ValueProperty actualProperty) {
            this.proxyElement = proxyElement;
            this.actualProperty = actualProperty;
        }
        

        @Override
        protected BindingImpl createBinding(ModelProperty property) {
            if (property instanceof ValueProperty) {
                return new ProxyBinding();
            }
            return null;
        }
        
        public void setModelElement(IModelElement element) {
            this.actualElement = element;
            this.value = element != null ? element.read(this.actualProperty).getText() : null;
            this.proxyElement.refresh();
        }

        public IModelElement getModelElement() {
            return this.actualElement;
        }
        
        public ValueProperty getValueProperty() {
            return this.actualProperty;
        }
        
        private IModelElement getActualElement(boolean create) {
            if (create && this.actualElement == null) {
                final IModelElement element = getList().insert();
                setModelElement(element);
            }
            return this.actualElement;
        }

        private final class ProxyBinding extends XmlValueBindingImpl {

            @Override
            public String read() {
                return ProxyResource.this.value;
            }

            @Override
            public void write(String value) {
                ProxyResource.this.value = value;
                
                final IModelElement element = getActualElement(true/*create*/);
                final ValueProperty property = getValueProperty();
                element.write(property, value);
            }
        }

        @Override
        public XmlElement getXmlElement(boolean createIfNecessary) {
            final IModelElement element = getActualElement(true);
            if (element != null) {
                return ((XmlResource)element.resource()).getXmlElement();
            }
            return null;
        }
    }
    
    private final class TextBinding implements ModifyListener 
    {
        private Text text;
        private ProxyResource resource;
        private ToolBar toolbar;
        private CompactListPropertyEditorRenderer.DeleteActionHandler deleteActionHandler;
        private PropertyEditorAssistDecorator decorator;
        private SapphireKeyboardActionPresentation actionPresentationKeyboard;
        private boolean modifying = false;
        
        public TextBinding(Text text, ProxyResource resource) {
            this.text = text;
            this.text.addModifyListener(this);

            this.resource = resource;
        }
        
        public void removeListener() {
            this.text.removeModifyListener(this);
            this.actionPresentationKeyboard.dispose();
        }
        
        public Text getText() {
            return this.text;
        }
        
        public ProxyResource getResource() {
            return this.resource;
        }
        
        public void refreshModelElement(IModelElement element) {
            setModelElement(element);
            String value = element != null ? element.read(this.resource.getValueProperty()).getText() : null;
            value = value == null ? "" : value;
            if (!CompactListPropertyEditorRenderer.equals(value, this.text.getText())) {
                this.text.setText(value);
            }
        }
        
        public void setModelElement(IModelElement element) {
            this.resource.setModelElement(element);
        }
        
        public IModelElement getModelElement() {
            return this.resource.getModelElement();
        }
        
        public boolean isModifying() {
            return this.modifying;
        }
        
        public void setModifying(boolean modifying) {
            this.modifying = modifying;
        }

        public ToolBar getToolbar() {
            return this.toolbar;
        }

        public void setToolbar(ToolBar toolbar) {
            this.toolbar = toolbar;
        }

        public void setActionPresentationKeyboard(SapphireKeyboardActionPresentation actionPresentationKeyboard) {
            this.actionPresentationKeyboard = actionPresentationKeyboard;
            this.actionPresentationKeyboard.attach(this.text);
            this.actionPresentationKeyboard.render();
        }

        public CompactListPropertyEditorRenderer.DeleteActionHandler getDeleteActionHandler() {
            return this.deleteActionHandler;
        }

        public void setDeleteActionHandler(
                CompactListPropertyEditorRenderer.DeleteActionHandler deleteActionHandler) {
            this.deleteActionHandler = deleteActionHandler;
        }

        public PropertyEditorAssistDecorator getDecorator() {
            return this.decorator;
        }

        public void setDecorator(PropertyEditorAssistDecorator decorator) {
            this.decorator = decorator;
        }
        
        public void modifyText(ModifyEvent e) {
            if( ! this.text.isDisposed() && ( this.text.getStyle() & SWT.READ_ONLY ) == 0 ) 
            {
                IModelElement element = this.resource.getModelElement();
                final String value = this.text.getText();
                if (value.length() == 0 && e.getSource().equals(this.text) && element == null) {
                    // do nothing..
                } else {
                    this.modifying = true;
                    
                    boolean createNew = false;
                    if (element == null) {
                        // new element may not be the last one - insert empty strings 
                        insertEmpty(this);
                        
                        final IModelElement newElement = getList().insert();
                        setModelElement(newElement);
                        createNew = true;
                    }
                    this.resource.element().write( this.resource.getValueProperty(), value );
                    if (createNew) {
                        this.text.setSelection(value.length(), value.length());
                    }

                    this.modifying = false;
                }
            }
        }
    }
    
    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final PropertyEditorPart propertyEditorDefinition )
        {
            return ( propertyEditorDefinition.getProperty() instanceof ListProperty );
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final PropertyEditorPart part )
        {
            return new CompactListPropertyEditorRenderer( context, part );
        }
    }
    
}
