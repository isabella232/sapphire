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

package org.eclipse.sapphire.ui.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sapphire.modeling.ByteArrayModelStore;
import org.eclipse.sapphire.modeling.IEclipseFileModelStore;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementDisposedEvent;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.ModelStoreForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.DelayedTasksExecutor.Task;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.provisional.extensions.ISourceEditingTextTools;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.provisional.IDOMSourceEditingTextTools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "restriction" )

public class ModelStoreForXmlEditor

    extends ModelStoreForXml
    implements IEclipseFileModelStore
    
{
    private StructuredTextEditor sourceEditor;
    private IModelElement rootModelElement;
    private final Map<Node,IModelElement> nodeToModelElementMap;
    private final Scrubber scrubber;
    private final ModelElementListener modelElementDisposeListener;
    
    public ModelStoreForXmlEditor( final SapphireEditor sapphireEditor,
                                   final StructuredTextEditor sourceEditor )
    {
        super( (ByteArrayModelStore) null );
        
        this.sourceEditor = sourceEditor;
        this.rootModelElement = null;
        this.nodeToModelElementMap = new HashMap<Node,IModelElement>();
        this.scrubber = new Scrubber();
        this.scrubber.start();
        
        this.modelElementDisposeListener = new ModelElementListener()
        {
            @Override
            public void handleElementDisposedEvent( final ModelElementDisposedEvent event )
            {
                handleElementDisposed( event.getModelElement() );
            }
        };
    }
    
    public StructuredTextEditor getXmlEditor()
    {
        return this.sourceEditor;
    }

    @Override
    public IFile getEclipseFile()
    {
    	IEditorInput input = this.sourceEditor.getEditorInput(); 
    	if( input instanceof FileEditorInput )
    		return ( (FileEditorInput) input ).getFile();
    	return null;
    }

    @Override
    public File getFile()
    {
    	IEditorInput input = this.sourceEditor.getEditorInput(); 
    	if( input instanceof FileEditorInput)
    		return getEclipseFile().getLocation().toFile();
    	//Handle files that are not part of the current workspace.
    	URI uri = ( (FileStoreEditorInput) input ).getURI();
    	return new File( uri );
    }

    @Override
    public boolean isXmlDeclarationNeeded()
    {
        return true;
    }

    @Override
    public void open() throws IOException
    {
        final ISourceEditingTextTools sourceEditingTextTools = (ISourceEditingTextTools) this.sourceEditor.getAdapter( ISourceEditingTextTools.class );
        final IDOMSourceEditingTextTools domSourceEditingTextTools = (IDOMSourceEditingTextTools) sourceEditingTextTools;
        this.document = domSourceEditingTextTools.getDOMDocument();
        
        final INodeAdapter adapter = new INodeAdapter()
        {
            public boolean isAdapterForType( final Object type )
            {
                return false;
            }

            public void notifyChanged( final INodeNotifier notifier,
                                       final int eventType,
                                       final Object changedFeature,
                                       final Object oldValue,
                                       final Object newValue,
                                       final int pos )
            {
                /* System.err.println( "notifyChanged" );
                System.err.println( "  notifier = " + notifier.getClass().getName() );
                System.err.println( "  eventType = " + eventType );
                System.err.println( "  changedFeature = " + ( changedFeature == null ? "null" : changedFeature.getClass().getName() ) );
                System.err.println( "  oldValue = " + ( oldValue == null ? "null" : oldValue.getClass().getName() ) );
                System.err.println( "  newValue = " + ( newValue == null ? "null" : newValue.getClass().getName() ) );
                System.err.println( "  pos = " + pos ); */
                
                if( eventType == INodeNotifier.ADD && newValue instanceof IDOMNode )
                {
                    addAdapter( (IDOMNode) newValue, this );
                }
                
                handleXmlNodeChange( (Node) notifier );
            }
        };

        addAdapter( (IDOMNode) this.document, adapter );
    }

    @Override
    public void save() throws IOException
    {
        if( validateEdit() )
        {
            this.sourceEditor.doSave( new NullProgressMonitor() );
        }
    }

    @Override
    public boolean validateEdit()
    {
        return this.sourceEditor.validateEditorInputState();
    }
    
    @Override
    public void registerRootModelElement( final IModelElement rootModelElement )
    {
        this.rootModelElement = rootModelElement;
    }

    @Override
    public void registerModelElement( final Node xmlNode,
                                      final IModelElement modelElement )
    {
        synchronized( this.nodeToModelElementMap )
        {
            this.nodeToModelElementMap.put( xmlNode, modelElement );
            modelElement.addListener( this.modelElementDisposeListener );
        }
    }
    
    private void handleElementDisposed( final IModelElement element )
    {
        if( element instanceof IModelElementForXml )
        {
            final XmlElement xmlElement = ( (IModelElementForXml) element ).getXmlElement();
            
            if( xmlElement != null )
            {
                synchronized( this.nodeToModelElementMap )
                {
                    final Node xmlNode = xmlElement.getDomNode();
                    final IModelElement registeredModelElement = this.nodeToModelElementMap.get( xmlNode );
                    
                    if( registeredModelElement == element )
                    {
                        this.nodeToModelElementMap.remove( xmlNode );
                    }
                }
            }
        }
    }

    protected IModelElement getModelElement( final Node xmlNode )
    {
        synchronized( this.nodeToModelElementMap )
        {
            Node node = xmlNode;
            IModelElement modelElement = this.nodeToModelElementMap.get( node );
            
            while( modelElement == null && node != null && ! ( node instanceof Document ) )
            {
                node = node.getParentNode();
                modelElement = this.nodeToModelElementMap.get( node );
            }
            
            if( modelElement == null )
            {
                modelElement = this.rootModelElement;
            }
            
            return modelElement;
        }
    }
    
    protected void handleXmlNodeChange( final Node xmlNode )
    {
        handleXmlNodeChange( xmlNode, getModelElement( xmlNode ) );
    }
    
    protected void handleXmlNodeChange( final Node xmlNode,
                                        final IModelElement nearestMatchModelElement )
    {
        DelayedTasksExecutor.schedule( new RefreshElementTask( nearestMatchModelElement ) );
    }
    
    private static void addAdapter( final IDOMNode node,
                                    final INodeAdapter adapter )
    {
        node.addAdapter( adapter );
        
        final NodeList children = node.getChildNodes();
        
        for( int i = 0, n = children.getLength(); i < n; i++ )
        {
            addAdapter( (IDOMNode) children.item( i ), adapter );
        }
    }
    
    protected static final class RefreshElementTask
    
        extends DelayedTasksExecutor.Task
        
    {
        private final IModelElement element;
        
        public RefreshElementTask( final IModelElement element )
        {
            this.element = element;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj != null && obj instanceof RefreshElementTask )
            {
                return ( this.element == ( (RefreshElementTask) obj ).element );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.element.hashCode();
        }
        
        @Override
        public boolean subsumes( final Task task )
        {
            boolean result = super.subsumes( task );
            
            if( result == false )
            {
                if( task instanceof RefreshPropertyTask && 
                    this.element == ( (RefreshPropertyTask) task ).element )
                {
                    return true;
                }
            }
            
            return result;
        }

        public void run()
        {
            this.element.refresh( false, true );
        }
    }

    protected static final class RefreshPropertyTask
    
        extends DelayedTasksExecutor.Task
        
    {
        private final IModelElement element;
        private final ModelProperty property;
        
        public RefreshPropertyTask( final IModelElement element,
                                    final ModelProperty property )
        {
            this.element = element;
            this.property = property;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj != null && obj instanceof RefreshPropertyTask )
            {
                final RefreshPropertyTask task = (RefreshPropertyTask) obj;
                return this.element == task.element && this.property == task.property;
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.element.hashCode() + this.property.hashCode();
        }
        
        public void run()
        {
            this.element.refresh( this.property, false, true );
        }
    }

    private final class Scrubber
    
        extends Thread
        
    {
        private boolean stopRequested = false;
        
        public void run()
        {
            final Map<Node,IModelElement> nodeToModelElementMap = ModelStoreForXmlEditor.this.nodeToModelElementMap;
            
            while( true )
            {
                synchronized( this )
                {
                    try
                    {
                        sleep( 10000 );
                    }
                    catch( InterruptedException e ) {}
                    
                    if( this.stopRequested == true )
                    {
                        return;
                    }
                }
                
                synchronized( nodeToModelElementMap )
                {
                    for( Iterator<Map.Entry<Node,IModelElement>> itr = nodeToModelElementMap.entrySet().iterator();
                         itr.hasNext(); )
                    {
                        final Map.Entry<Node,IModelElement> entry = itr.next();
                        
                        if( entry.getKey().getParentNode() == null )
                        {
                            /* final String nodeClassName = entry.getKey().getClass().getSimpleName();
                            final int nodeHashCode = entry.getKey().hashCode();
                            final String modelElementClassName = entry.getValue().getClass().getSimpleName();
                            final int modelElementHashCode = entry.getValue().hashCode();
                            
                            System.err.println( "SCRUBBER REMOVED: " + nodeClassName + ":" + nodeHashCode + " -> " +
                                                modelElementClassName + ":" + modelElementHashCode ); */
                            
                            itr.remove();
                        }
                    }
                }
            }
        }
        
        @SuppressWarnings( "unused" )
        
        public synchronized void dispose()
        {
            this.stopRequested = true;
        }
    }
    
}
