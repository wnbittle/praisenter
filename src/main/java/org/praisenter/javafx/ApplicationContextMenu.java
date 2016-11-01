/*
 * Copyright (c) 2015-2016 William Bittle  http://www.praisenter.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of Praisenter nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.praisenter.javafx;

import java.util.Deque;
import java.util.LinkedList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;

/**
 * Represents a custom ContextMenu where {@link ApplicationAction}s can be easily bound.
 * @author William Bittle
 * @version 3.0.0
 * @since 3.0.0
 */
public final class ApplicationContextMenu extends ContextMenu implements EventHandler<KeyEvent> {
	/** The node this context menu is bound to */
	private final Node node;
	
	/** True if the window that contains the node is focused */
	private final BooleanProperty windowFocused = new SimpleBooleanProperty();
	
	/**
	 * Minimal constructor.
	 * @param node the node this context menu is for
	 */
	public ApplicationContextMenu(Node node) {
		this.node = node;
		
//		// listen for state changed events to update the context menu state
//		node.addEventHandler(ApplicationPaneEvent.STATE_CHANGED, e -> {
//			if (e.getEventType() == ApplicationPaneEvent.STATE_CHANGED) {
//				updateMenuState(e.getApplicationPane());
//			}
//		});
//
//		// listen for when the scene changes so we can re-bind to the
//		// parent window and focus owner
//		node.sceneProperty().addListener((obs, ov, nv) -> {
//			windowFocused.unbind();
//			if (nv != null) {
//				windowFocused.bind(nv.getWindow().focusedProperty());
//			}
//		});
//		
//		// we need to re-evaluate the menu state when the focus between menus changes
//		// primarily for re-evaluating the content in the clipboard.
//		// this allows us to disable an item if the user goes to a different app and copies
//		// something that isn't the expected type
//		this.windowFocused.addListener((obs, ov, nv) -> {
//			if (node instanceof ApplicationPane) {
//				ApplicationPane pane = (ApplicationPane)node;
//				// recursively go through the menu updating disabled and visibility states
//				updateMenuState(pane);
//			}
//		});
		
		this.setOnShowing(e -> {
			if (this.node instanceof ApplicationPane) {
				updateMenuState((ApplicationPane)this.node);
			}
		});
		
		// NOTE: catch any accelerator keys so we can consume the events
		// so that they don't cause the events to be processed twice, once
		// in the context menu and once in the main menu
		// JAVABUG 10/24/16 Duplicated accelerators https://bugs.openjdk.java.net/browse/JDK-8088068
		node.addEventFilter(KeyEvent.KEY_TYPED, this);
		node.addEventFilter(KeyEvent.KEY_PRESSED, this);
		node.addEventFilter(KeyEvent.KEY_RELEASED, this);
	}
	
	/* (non-Javadoc)
	 * @see javafx.event.EventHandler#handle(javafx.event.Event)
	 */
	@Override
	public void handle(KeyEvent event) {
		Deque<MenuItem> menus = new LinkedList<MenuItem>();
		// seed with the menu bar's menus
		menus.addAll(getItems());
		while (menus.size() > 0) {
			MenuItem menu = menus.pop();
			if (menu.getAccelerator() != null && menu.getAccelerator().match(event)) {
				// fire the menu action always
				menu.fire();
				// check for common events that are available on editable controls
				if (ApplicationAction.COPY.getAccelerator().match(event) ||
					ApplicationAction.CUT.getAccelerator().match(event) ||
					ApplicationAction.PASTE.getAccelerator().match(event) ||
					ApplicationAction.DELETE.getAccelerator().match(event) ||
					ApplicationAction.SELECT_ALL.getAccelerator().match(event)) {
					// allow the event to continue
				} else {
					// otherwise consume it so it doesn't get processed by another handler
					// with the same accelerator/key stroke binding
					event.consume();
				}
				break;
			}
		}
	}
	
	/**
	 * Helper method for making {@link ApplicationAction} MenuItems.
	 * @param action the action
	 * @return MenuItem
	 */
	public MenuItem createMenuItem(ApplicationAction action) {
		MenuItem item = action.toMenuItem();
		item.setOnAction(e -> {
			this.node.fireEvent(new ApplicationEvent(item, item, ApplicationEvent.ALL, action));
		});
		return item;
	}
	
	/**
	 * Updates the state of the context menu based on the given application pane's state.
	 * @param pane the pane
	 */
	private void updateMenuState(ApplicationPane pane) {
		Deque<MenuItem> menus = new LinkedList<MenuItem>();
		// seed with the menu bar's menus
		menus.addAll(this.getItems());
		while (menus.size() > 0) {
			MenuItem menu = menus.pop();
			
			// process this item
			Object data = menu.getUserData();
			if (data != null && data instanceof ApplicationAction) {
				ApplicationAction action = (ApplicationAction)data;
				// an action is disabled or hidden as long as both the root
				// and the currently focused application pane don't handle it
				boolean disabled = !pane.isApplicationActionEnabled(action);
				boolean visible = pane.isApplicationActionVisible(action);
				menu.setDisable(disabled);
				menu.setVisible(visible);
			}
			
			// add children
			if (menu instanceof Menu) {
				menus.addAll(((Menu)menu).getItems());
			}
		}
	}
}