/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

import java.awt.*;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
/**
 *
 * @author Marko Lokka
 */
public class RectangleSelection2 {

    private final String ERROR = RectangleSelection2.class.getSimpleName();

    private double dx;
    private double dy;

    Point2D mouseSceneCoords;
    private Pane pane;
    private Rectangle rect;
    private Scene scene;

    private String startNode;
    private String lastNode;
    private Stage stage;
    private SelectionModel selectionModel;
    private boolean drag = false;

    public RectangleSelection2(Scene scene, Pane pane, SelectionModel selectionModel) {

        this.scene = scene;
        this.pane = pane;
        this.selectionModel = selectionModel;

        this.stage = (Stage) scene.getWindow();

        rect = new Rectangle(0, 0, 0, 0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.RED.deriveColor(0, 0.6, 1, 0.6));

        pane.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
    }

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            drag = true;
            sprintf("drag started: " + drag);
            double offsetX = (event.getX() - dx);
            double offsetY = (event.getY() - dy);

            rect.setWidth(offsetX);

            if (offsetX < 0) {
                rect.setX(event.getX());
                rect.setWidth(dx - rect.getX());
            }

            if (offsetY > 0) {
                sprintf("offsetY > 0: " + offsetY);
                rect.setHeight(offsetY);
            } else {
                sprintf("offsetY else : " + offsetY);
                rect.setY(event.getY());
                rect.setHeight(dy - rect.getY());

            }

            event.consume();

        }
    };

    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            Point mouse = java.awt.MouseInfo.getPointerInfo().getLocation();
            Point2D local = pane.screenToLocal(mouse.x, mouse.y);

            dx = local.getX();
            dy = local.getY();

            sprintf("Mouse pressed dx: " + dx + " dy: " + dy + " getSceneX.x: " + event.getSceneX() + " getSceneY.y: " + event.getSceneY());

            rect.setX(dx);
            rect.setY(dy);
            rect.setWidth(0);
            rect.setHeight(0);

            pane.getChildren().add(rect);

            event.consume();
        }
    };

    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            sprintf("event target is: " + event.getTarget());

            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);

            pane.getChildren().remove(rect);
            drag = false;
            sprintf("Drag false: " + drag);
            event.consume();

        }
    };

}
