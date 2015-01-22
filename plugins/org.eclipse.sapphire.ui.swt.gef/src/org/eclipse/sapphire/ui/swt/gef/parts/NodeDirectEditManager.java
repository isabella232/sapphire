/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.diagram.editor.FunctionUtil;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.figures.TextFigure;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModelUtil;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class NodeDirectEditManager extends DirectEditManager {

	private IActionBars actionBars;
	private CellEditorActionHandler actionHandler;
	private IAction copy, cut, paste, undo, redo, find, selectAll, delete;
	private double cachedZoom = -1.0;
	private Font scaledFont;
	private ZoomListener zoomListener = new ZoomListener() {
		public void zoomChanged(double newZoom) {
			updateScaledFont(newZoom);
		}
	};
	private Label label;
	protected SapphireDiagramEditor diagramEditor;
	protected TextPart textPart;
	protected Value<?> property;
	protected IFigure textFigure;
	protected IFigure parentFigure;
	
	private String initValue;
	private boolean committed = false;

	public NodeDirectEditManager(GraphicalEditPart source, TextPart textPart, CellEditorLocator locator, Label label) {
		super(source, null, locator);
		this.textPart = textPart;
		this.label = label; 

		ShapeEditPart shapeEditPart = (ShapeEditPart)source;		
		DiagramNodeEditPart nodeEditPart = shapeEditPart.getNodeEditPart();		
		ShapePresentation shapePresentation = ShapeModelUtil.getChildShapePresentation(
				nodeEditPart.getCastedModel().getShapePresentation(), textPart);
		this.textFigure = shapePresentation.getFigure();
		this.parentFigure = shapePresentation.getParentFigure();
		
		this.diagramEditor = ((ShapeEditPart)source).getConfigurationManager().getDiagramEditor();
		this.property = FunctionUtil.getFunctionProperty(this.textPart.getLocalModelElement(), 
				this.textPart.getContentFunction());
		
	}

	/**
	 * @see org.eclipse.gef.tools.DirectEditManager#bringDown()
	 */
	protected void bringDown() {
		if (!committed && isDirty()) {
			if (getCellEditor() instanceof TextCellEditor) {
				Text text = (Text) getCellEditor().getControl();
				text.setText(initValue);
				internalCommit();
			}
		}
		this.diagramEditor.setDirectEditingActive(false);
		ZoomManager zoomMgr = (ZoomManager) this.diagramEditor.getGraphicalViewer()
				.getProperty(ZoomManager.class.toString());
		if (zoomMgr != null)
			zoomMgr.removeZoomListener(zoomListener);
		if (actionHandler != null) { 
			actionHandler.dispose();
			actionHandler = null;
		}
		if (actionBars != null) {
			restoreSavedActions(actionBars);
			actionBars.updateActionBars();
			actionBars = null;
		}

		super.bringDown();
		// dispose any scaled fonts that might have been created
		disposeScaledFont();
		label.setVisible(true);
	}

	@Override
	public void show() 
	{
		diagramEditor.setDirectEditingActive(true);
		super.show();
	}
	
	@Override
	protected CellEditor createCellEditorOn(Composite composite) 
	{
		return new DiagramTextCellEditor(this.textPart, composite, SWT.CENTER);
	}

	private void disposeScaledFont() {
		if (scaledFont != null) {
			scaledFont.dispose();
			scaledFont = null;
		}
	}

	@Override
	protected void initCellEditor() {
		// update text
		initValue = this.property.text();
		if (initValue == null)
		{
			initValue = this.textPart.getContent();
		}
		getCellEditor().setValue(initValue);		
		initCellEditorPresentation();
	}

	@Override
	protected DirectEditRequest createDirectEditRequest() 
	{
		DirectEditRequest req = super.createDirectEditRequest();
		Map<String, TextPart> extendedData = new HashMap<String, TextPart>();
		extendedData.put(DiagramNodeEditPart.DIRECT_EDIT_REQUEST_PARAM, this.textPart);
		req.setExtendedData(extendedData);
		return req;
	}

	protected void initCellEditorPresentation()
	{
		// Shenxue: set text to "" doesn't work since it messes the size calculation in parent's
		// layout manager. It'd shrink the label figure size to 0. Use figure's visibility instead.
		//label.setText("");
		label.setVisible(false);
		// update font
		ZoomManager zoomMgr = (ZoomManager) getEditPart().getViewer()
				.getProperty(ZoomManager.class.toString());
		if (zoomMgr != null) {
			// this will force the font to be set
			cachedZoom = -1.0;
			updateScaledFont(zoomMgr.getZoom());
			zoomMgr.addZoomListener(zoomListener);
		} else
			getCellEditor().getControl().setFont(label.getFont());

		// Hook the cell editor's copy/paste actions to the actionBars so that
		// they can
		// be invoked via keyboard shortcuts.
		actionBars = this.diagramEditor.getEditorSite().getActionBars();
		saveCurrentActions(actionBars);
		actionHandler = new CellEditorActionHandler(actionBars);
		actionHandler.addCellEditor(getCellEditor());
		actionBars.updateActionBars();		
	}
	
	@Override
	protected void handleValueChanged() 
	{
		internalCommit();

		// TextPart's content might be a function with default values for empty String property.
		// But we don't want the cell size to jump. So resetting TextFigure's content again to avoid
		// cell size jump.
		String cellText = (String)getCellEditor().getValue();
		if (cellText == null || cellText.isEmpty())
		{
			((TextFigure)textFigure).setText(" ");
		}
		
		// In Kepler when the cell editor is brought up and the focus is set on the text, the text
		// triggers a focus lost event which causes direct edit manager's bringDown() method to be called.
		// The overridden bringDown() method would set the label to be visible again. Resetting the label's
		// visibility here is a workaround.
		label.setVisible(false);
		super.handleValueChanged();
	}

	@Override
	protected void commit() 
	{
		this.committed = true;
		super.commit();
		((TextFigure)textFigure).setText(textPart.getContent());
	}
	
	public TextPart getTextPart()
	{
		return this.textPart;
	}
	
	private void internalCommit()
	{
		CommandStack stack = getEditPart().getViewer().getEditDomain()
				.getCommandStack();
		stack.execute(getEditPart().getCommand(getDirectEditRequest()));						
	}
	
	private void restoreSavedActions(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copy);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), paste);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), delete);
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
				selectAll);
		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), cut);
		actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), find);
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undo);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redo);
	}

	private void saveCurrentActions(IActionBars actionBars) {
		copy = actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
		paste = actionBars.getGlobalActionHandler(ActionFactory.PASTE.getId());
		delete = actionBars
				.getGlobalActionHandler(ActionFactory.DELETE.getId());
		selectAll = actionBars.getGlobalActionHandler(ActionFactory.SELECT_ALL
				.getId());
		cut = actionBars.getGlobalActionHandler(ActionFactory.CUT.getId());
		find = actionBars.getGlobalActionHandler(ActionFactory.FIND.getId());
		undo = actionBars.getGlobalActionHandler(ActionFactory.UNDO.getId());
		redo = actionBars.getGlobalActionHandler(ActionFactory.REDO.getId());
	}

	private void updateScaledFont(double zoom) {
		if (cachedZoom == zoom || getCellEditor() == null)
			return;
		Font font = this.label.getFont();

		disposeScaledFont();
		cachedZoom = zoom;
		if (zoom == 1.0) {
			getCellEditor().getControl().setFont(font);
		}
		else {
			FontData fd = font.getFontData()[0];
			fd.setHeight((int) (fd.getHeight() * zoom));
			this.scaledFont = new Font(null, fd);
			getCellEditor().getControl().setFont(this.scaledFont);
		}
	}

}
