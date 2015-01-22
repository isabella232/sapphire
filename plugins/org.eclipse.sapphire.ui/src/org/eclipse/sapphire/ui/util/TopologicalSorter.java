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

package org.eclipse.sapphire.ui.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;

/**
 * Topological sort implementation. Useful for sorting entities in dependency order or for
 * organizing entities that carry before and after hints. 
 * 
 * <p>The algorithm is capable of identifying and breaking cycles. It is possible to
 * register a listener that will be notified of cycles. A common use of this is to log
 * the information about the cycles. It also possible to throw an exception when a cycle
 * is detected.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class TopologicalSorter<T>
{
    private static final String BEFORE_PREFIX = "before:";
    private static final String AFTER_PREFIX = "after:";

    @Text( "Cycle detected: {0}" )
    private static LocalizableText cycleDetectedMessage; 
    
    static
    {
        LocalizableText.init( TopologicalSorter.class );
    }
    
    private Map<String,Entity> entities = new LinkedHashMap<String,Entity>();
    private List<CycleListener> cycleListeners = new ArrayList<CycleListener>();
    
    /**
     * Creates a new entity around the specified data object. The entity has API for 
     * defining before and after constraints.
     * 
     * @param data the data object
     * @return the created entity
     */
    
    public Entity entity( final T data )
    {
        return entity( getClass().getName(), data );
    }

    /**
     * Creates a new entity around the specified data object. The entity has API for 
     * defining before and after constraints.
     * 
     * @param id the id to assign to the entity
     * @param data the data object
     * @return the created entity
     */
    
    public Entity entity( final String id,
                          final T data )
    {
        String uniqueId = id;
        int counter = 0;
        
        while( this.entities.containsKey( uniqueId ) )
        {
            counter++;
            uniqueId = id + String.valueOf( counter );
        }
        
        final Entity entity = new Entity( data );
        this.entities.put( uniqueId, entity );
        
        return entity;
    }
    
    /**
     * Adds a cycle listener. The listeners will be notified of cycles detected during the sort.
     * The listener can be used, for instance, to log the cycles or to thrown an exception.
     *  
     * @param listener the listener to remove
     */
    
    public void addCycleListener( final CycleListener listener )
    {
        this.cycleListeners.add( listener );
    }
    
    /**
     * Removes a cycle listener that was previously registered.
     * 
     * @param listener the listener to remove
     */
    
    public void removeCycleListener( final CycleListener listener )
    {
        this.cycleListeners.remove( listener );
    }
    
    /**
     * Performs the sort after entities have been created and their constraints have been
     * defined. Note that there are typically many solutions that work, but this algorithm
     * returns stable results based on the order that entities and constraints have been
     * added.
     * 
     * @return the sorted list
     */
    
    public List<T> sort()
    {
        // Resolve id-based constraints.
        
        for( Entity entity : this.entities.values() )
        {
            for( Iterator<String> itr = entity.constraints.iterator(); itr.hasNext(); )
            {
                final String constraint = itr.next();
                
                String targetEntityId = null;
                boolean after = false;
                
                if( constraint.startsWith( BEFORE_PREFIX ) && constraint.length() > BEFORE_PREFIX.length() )
                {
                    targetEntityId = constraint.substring( BEFORE_PREFIX.length() );
                }
                else if( constraint.startsWith( AFTER_PREFIX ) && constraint.length() > AFTER_PREFIX.length() )
                {
                    targetEntityId = constraint.substring( AFTER_PREFIX.length() );
                    after = true;
                }
                
                if( targetEntityId != null )
                {
                    final Entity targetEntity = this.entities.get( targetEntityId );
                    
                    if( targetEntity != null )
                    {
                        if( after )
                        {
                            entity.after( targetEntity );
                        }
                        else
                        {
                            entity.before( targetEntity );
                        }
                        
                        itr.remove();
                    }
                }
            }
        }
        
        // Detect and deal with cycles. The first phase traverses all sub-graphs that
        // have at least one leaf. The second phase mops up cycles in graphs with no
        // leaves.
        
        clearVisited();
        
        for( Entity entity : findLeaves() )
        {
            final List<Entity> path = new ArrayList<Entity>();
            dealWithCycles( entity, path );
        }
        
        for( Entity entity : this.entities.values() )
        {
            if( ! entity.visited )
            {
                final List<Entity> path = new ArrayList<Entity>();
                dealWithCycles( entity, path );
            }
        }
        
        // If we got this far, the graph has no cycles. There has either not been
        // any in the first place or they have been broken. Time to sort.
        
        clearVisited();
        
        final List<T> result = new ArrayList<T>();
        
        for( Entity entity : findLeaves() )
        {
            visit( entity, result );
        }
        
        return result;
    }

    @SuppressWarnings( "unchecked" )
    
    private void visit( final Entity entity,
                        final List<T> result )
    {
        if( ! entity.visited )
        {
            entity.visited = true;
            
            for( Entity x : entity.outgoing )
            {
                visit( x, result );
            }
            
            result.add( (T) entity.data() );
        }
    }
    
    private List<Entity> findLeaves()
    {
        final List<Entity> leaves = new ArrayList<Entity>();
        
        for( Entity entity : this.entities.values() )
        {
            if( entity.incoming.isEmpty() )
            {
                leaves.add( entity );
            }
        }
        
        return leaves;
    }
    
    private void dealWithCycles( final Entity entity,
                                 final List<Entity> path )
    {
        entity.visited = true;
        
        int index = path.indexOf( entity );
        
        if( index == -1 )
        {
            path.add( entity );
            
            if( entity.outgoing.size() == 1 )
            {
                dealWithCycles( entity.outgoing.iterator().next(), path );
            }
            else
            {
                for( Entity x : entity.outgoing )
                {
                    final List<Entity> pathCopy = new ArrayList<Entity>( path );
                    dealWithCycles( x, pathCopy );
                }
            }
        }
        else
        {
            if( ! this.cycleListeners.isEmpty() )
            {
                final List<Entity> cycle = path.subList( index, path.size() );
                
                for( CycleListener listener : this.cycleListeners )
                {
                    listener.handleCycle( cycle );
                }
            }
            
            final Entity last = path.get( path.size() - 1 );
            last.outgoing.remove( entity );
            entity.incoming.remove( last );
        }
    }

    private void clearVisited()
    {
        for( Entity entity : this.entities.values() )
        {
            entity.visited = false;
        }
    }
    
    private static String convertToString( final List<Entity> entities )
    {
        final StringBuilder buf = new StringBuilder();
        
        for( int i = 0, n = entities.size(); i < n; i++ )
        {
            if( i > 0 )
            {
                buf.append( ", " );
            }
            
            buf.append( entities.get( i ) );
        }
        
        return buf.toString();
    }
    
    /**
     * Represents a single object to be sorted to the sorter. Allows the before and after
     * constraints to be specified. 
     * 
     * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
     */

    public static class Entity
    {
        private final Object data;
        private final Set<Entity> outgoing = new LinkedHashSet<Entity>();
        private final Set<Entity> incoming = new LinkedHashSet<Entity>();
        private final Set<String> constraints = new HashSet<String>();
        private boolean visited;
        
        private Entity( final Object data )
        {
            this.data = data;
        }
        
        /**
         * Returns the data enclosed by this entity.
         * 
         * @return the data enclosed by this entity
         */
        
        public Object data()
        {
            return this.data;
        }
        
        /**
         * Specifies a requirement that this entity should appear before the specified entity.
         * 
         * @param entity the target entity
         */
        
        public void before( final Entity entity )
        {
            this.incoming.add( entity );
            entity.outgoing.add( this );
        }
        
        /**
         * Specifies a requirement that this entity should appear after the specified entity.
         * 
         * @param entity the target entity
         */
        
        public void after( final Entity entity )
        {
            entity.incoming.add( this );
            this.outgoing.add( entity );
        }
        
        /**
         * Specifies a before or after constraint using string syntax. The format is
         * [before|after]:{id}. The entity with the specified id does not need to be present
         * at the time that the constraint is added. Resolution of id-based constraints
         * happens right before the sort.
         * 
         * @param contraint the constraint text using [before|after]:{id} format 
         */
        
        public void constraint( final String contraint )
        {
            this.constraints.add( contraint );
        }
        
        public String toString()
        {
            return this.data.toString();
        }
    }
    
    /**
     * The base class for listeners that want to be notified of cycles detected during the sort.
     * The listener can be used, for instance, to log the cycles or to thrown an exception. 
     */
    
    public static abstract class CycleListener
    {
        /**
         * The method that will be called when a cycle is detected.
         * 
         * @param cycle the entities involved in the cycle
         */
        
        public abstract void handleCycle( final List<Entity> cycle );
    }
    
    /**
     * Cycle listener that outputs cycles to the provided PrintStream.
     * 
     * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
     */
    
    public static final class PrintStreamCycleListener extends CycleListener
    {
        private final PrintStream stream;
        
        public PrintStreamCycleListener( final PrintStream stream )
        {
            this.stream = stream;
        }
        
        @Override
        public void handleCycle( final List<Entity> cycle )
        {
            this.stream.println( cycleDetectedMessage.format( convertToString( cycle ) ) );
        }
    }
    
    /**
     * Cycle listener that throws CycleException on first cycle. This will abort the sort. The 
     * thrown exception will propagate out to the caller of the sort method.
     * 
     * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
     */
    
    public static final class ExceptionCycleListener extends CycleListener
    {
        @Override
        public void handleCycle( final List<Entity> cycle )
        {
            throw new CycleException( cycle );
        }
    }
    
    /**
     * An exception that can be thrown to signal presence of a cycle. Can be used by itself
     * or with ExceptionCycleListener.
     * 
     * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
     */
    
    public static final class CycleException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        
        private final List<Entity> cycle;
        
        public CycleException( final List<Entity> cycle )
        {
            this.cycle = Collections.unmodifiableList( new ArrayList<Entity>( cycle ) );
        }
        
        @Override
        public String getMessage()
        {
            return cycleDetectedMessage.format( convertToString( this.cycle ) );
        }
        
        public List<Entity> getCycle()
        {
            return this.cycle;
        }
    }

}
