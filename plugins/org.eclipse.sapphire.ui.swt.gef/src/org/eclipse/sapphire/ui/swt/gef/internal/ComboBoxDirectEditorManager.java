/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.internal;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.draw2d.Label;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.swt.gef.parts.NodeDirectEditManager;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ComboBoxDirectEditorManager extends NodeDirectEditManager
{
	private ComboBoxCellEditor comboCellEditor;
	private ICellEditorListener cellEditorListener;
	private boolean committing = false;
	private String initialValue;
	
	public ComboBoxDirectEditorManager(GraphicalEditPart source, TextPart textPart, CellEditorLocator locator, Label label)
	{
		super(source, textPart, locator, label);
		initCellEditorListener();
	}
	
	@Override
	protected CellEditor createCellEditorOn(Composite composite) 
	{        
		this.comboCellEditor = new DiagramComboBoxCellEditor(getTextPart(), composite, this.property);
		return comboCellEditor;
	}
	
	@Override
	protected void initCellEditor() 
	{
		// update text
		String initValue = this.property.text();
		if (initValue == null)
		{
			initValue = this.textPart.getContent();
		}
		this.initialValue = initValue;
		PossibleValuesService possibleValuesService = this.property.service(PossibleValuesService.class);
		Set<String> possibleValues = possibleValuesService.values();
		Iterator<String> it = possibleValues.iterator();
		int index = -1;
		while (it.hasNext())
		{
			index++;
			if (it.next().equals(initValue))
			{					
				break;
			}
		}
		getCellEditor().setValue(index);
		initCellEditorPresentation();
		this.comboCellEditor.addListener(cellEditorListener);
	}	
	
	@Override
	protected void unhookListeners() 
	{
		this.comboCellEditor.removeListener(cellEditorListener);
		super.unhookListeners();
	}
	
	@Override
	protected void commit()
	{
	}
	
	private void initCellEditorListener() 
	{
		cellEditorListener = new ICellEditorListener() 
		{
			public void editorValueChanged(boolean oldValidState,
					boolean newValidState) 
			{
				// Ignore.
			}

			public void cancelEditor() 
			{
			}

			public void applyEditorValue() {
				ComboBoxDirectEditorManager.this.applyEditorValue();
			}
		};
	}
	
	private void applyEditorValue()
	{
		if (committing)
			return;
		committing = true;
		try
		{
			eraseFeedback();
			String newValue = (String)this.comboCellEditor.getValue();
			if (newValue != null && !newValue.equals(this.initialValue))
			{
				CommandStack stack = getEditPart().getViewer().getEditDomain()
						.getCommandStack();
				stack.execute(getEditPart().getCommand(getDirectEditRequest()));
			}
		}
		finally
		{
			bringDown();
			committing = false;
		}
	}
		
}
