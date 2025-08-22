
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;



public class RectangleSelection {

    private double dx;
    private double dy;

    Point2D mouseSceneCoords;
    private TilePane tilePane;
    private Pane pane;
//    private AnchorPane anchorPane;

//    private Parent pane;
//    private static Map<String, Path> id_path_map;
    private Rectangle rect = new Rectangle(0, 0, 0, 0);

    private Scene scene;

    private String startNode;
    private String lastNode;
    private final String ERROR = RectangleSelection.class.getSimpleName();
//    private AnchorPane pane;
    private Stage stage;
    private SelectionModel selectionModel;
    private SimpleBooleanProperty drag = new SimpleBooleanProperty(false);

    public RectangleSelection(Scene scene, Pane pane, TilePane aTilePane, SelectionModel selectionModel) {

//        this.id_path_map = id_path_map;
//        this.pane = grid.getParent();
        this.tilePane = aTilePane;
        this.scene = scene;

        this.stage = (Stage) scene.getWindow();
        this.selectionModel = selectionModel;
        this.pane = pane;
//        pane = (AnchorPane) scene.lookup("df_anchorPane");
//        pane.getChildren().clear();
//        this.scene = (Scene) this.grid.getScene();
//        if (scene == null) {
//            Messages.errorSmth(ERROR, "" , ex, Misc_GUI.getLineNumber(), true);
//        }
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.RED.deriveColor(0, 0.6, 1, 0.6));

        tilePane.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
//        grid.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
//            grid.addEventHandler(MouseEvent.MOUSE_MOVED, onMouseMovedEventHandler);
        tilePane.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
//        scene.addEventHandler(KeyEvent.KEY_PRESSED, onKeyPressed);
    }

    EventHandler<KeyEvent> onKeyPressed = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {

            switch (event.getCode()) {
                case UP:
                    sprintf("up pressed node: ");
                    break;
                case RIGHT:
                    sprintf("right pressed");
                    break;
                case DOWN:
                    sprintf("down pressed");
                    break;
                case LEFT:
                    sprintf("left pressed");
                    break;
                case ESCAPE:
                    selectionModel.clearAll(tilePane);
                default:
                    break;
            }
        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            drag.set(true);
            sprintf("drag started: " + drag);

            pane.getChildren().add(rect);
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
        private Pane getTillRoot(Node target) {
            if (target instanceof GridPane) {
                return (Pane) target;
            } else {
                getTillRoot(target.getParent());
            }
            return null;
        }

        @Override
        public void handle(MouseEvent event) {
            drag.set(false);
            Point mouse = java.awt.MouseInfo.getPointerInfo().getLocation();
            Point2D local = tilePane.screenToLocal(mouse.x, mouse.y);

            dx = local.getX();
            dy = local.getY();

            sprintf("Mouse pressed dx: " + dx + " dy: " + dy + " getSceneX.x: " + event.getSceneX() + " getSceneY.y: " + event.getSceneY());

            rect.setX(dx);
            rect.setY(dy);
            rect.setWidth(0);
            rect.setHeight(0);

            sprintf("current evet target: " + event.getTarget());
            if (drag.get()) {
                pane.getChildren().add(rect);
            }
//                if (!(event.getTarget() instanceof Pane)) {
//                    Pane pi = getTillRoot((Node) event.getTarget());
//                    if (pi != null) {
//                        sprintf("Pi pane: " + pi);
//                        pi.getChildren().add(rect);
//                    }
//                } else {
//                   
//                }
//            }

            event.consume();
        }
    };

    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        private boolean hasImageViewNode(Node node) {
//            sprintf("hasImageViewNode: " + node);

            if (node instanceof VBox) {
                for (Node node2 : ((VBox) node).getChildren()) {
                    if (node2 instanceof ImageView) {
                        sprintf("ImageView: " + node2);

                        return true;
                    }
                }
            }
            return false;
        }

        private int parseId(String toParse, String nodeId) throws NumberFormatException {
            sprintf("parseId: " + toParse + " nodeId: " + nodeId);
            return Integer.parseInt(nodeId.replace(toParse, ""));
        }

        @Override
        public void handle(MouseEvent event) {
            sprintf("event target is: " + event.getTarget());

            if (!(event.getTarget() instanceof VBox)) {
                sprintf("Event was not vbox: " + event.getSource());
                event.consume();
            }
            if (!event.isShiftDown() && !event.isControlDown()) {
                selectionModel.clearAll(tilePane);
                selectionModel.addWithToggle((Node) event.getTarget());
                event.consume();
                return;
            } else if (event.isShiftDown() && !event.isControlDown()) {

                selectionModel.addWithToggle((Node) event.getTarget());
                List<Integer> integer_list = new ArrayList<>();
                warningText("onMouseReleasedEventHandler is not ready!");
                for (Node n : selectionModel.getSelectionList()) {
                    sprintf("selectionModel: " + n);
                    if (n.getId() != null) {
                        int number = -1;
                        try {
                            number = Integer.parseInt(n.getId().replace("imageFrame", ""));
                        } catch (Exception e) {
                            Main.setProcessCancelled(true);
                            sprintf("number parse error: " + e.getMessage());
                            return;
                        }
                        integer_list.add(number);
                    }
                }
                Collections.sort(integer_list);
                int min = Collections.min(integer_list);
                int max = Collections.max(integer_list);
                sprintf("min: " + min + " max: " + max);
                for (Integer number : integer_list) {
                    sprintf("Number is: " + number);
                }
                boolean passed = false;

                for (Node node : tilePane.getChildren()) {

                    int current = parseNodeId(node, "imageFrame: ");
                    if (current >= 0) {
                        if (current == min) {
                            passed = true;
                        }
                        if (passed) {
                            if (current == max) {
                                selectionModel.addWithToggle(node);
                                break;
                            } else {
                                selectionModel.addWithToggle(node);
                            }
                        }
                    }
                }

            }

//            for (Node node : grid.getChildren()) {
//                sprintf("grid node: " + node);
//                if (node instanceof VBox) {
//                    if (node.getId().contains("imageFrame: ")) {
//                        if (node.getBoundsInParent().intersects(rect.getBoundsInParent())) {
//                            if (event.isControlDown()) {
//                                if (selectionModel.contains(node)) {
//                                    selectionModel.remove(node);
//                                } else {
//                                    selectionModel.add(node);
//                                }
//                            } else {
////                            sprintf("node: " + getImageNameByID(node.getId()));
//                                selectionModel.add(node);
//                            }
//
//                        }
//                        if (event.isShiftDown() && selectionModel.getSelectionList().size() >= 1) {
//                            sprintf("event.isShiftDown() && selectionModel.getSelectionList().size() >= 2) {");
//                            int start = 0;
//                            int end = 0;
//                            boolean first = false;
//                            ArrayList<Integer> integer_List = new ArrayList<>();
//
//                            if (selectionModel.getSelectionList().isEmpty()) {
//                                sprintf("selectionModel.getSelectionList().isEmpty()");
//                                event.consume();
//                                return;
//                            }
//                            for (Node s1 : selectionModel.getSelectionList()) {
//                                if (s1.getId() != null) {
//                                    int id;
//                                    try {
//                                        id = Integer.parseInt(s1.getId().replace("imageFrame: ", ""));
//                                        integer_List.add(id);
//                                        sprintf("integer idddd parse: " + id);
//                                    } catch (Exception ex) {
//                                        sprintf("exception null with integer add s1= " + s1.getId());
//                                    }
////                                    sprintf("not null: " + s1.getId());
//                                }
//                            }
//                            if (integer_List.isEmpty()) {
//                                sprintf("integer list was empty!");
//                                event.consume();
//                                return;
//                            }
//                            start = Collections.min(integer_List);
//                            end = Collections.max(integer_List);
//                            sprintf("start node is: " + start + " end node is:  " + end);
//                            selectionModel.clearAll();
//                            for (Integer intti : integer_List) {
//                                sprintf("Intti is: " + intti);
//                            }
//                            if (end != start) {
//                                sprintf("end != start");
//                                int current = 0;
//                                for (Node node2 : grid.getChildren()) {
//                                    if (node2.getId() != null) {
////                                        if (hasImageViewNode(node2)) {
//                                        sprintf("parseif would be: " + node2.getId());
//                                        current = parseId("imageFrame: ", node2.getId());
//                                        sprintf("current: " + current);
//                                        if (current >= start && current <= end) {
//                                            sprintf("adding to sel: " + node2.getId());
//                                            selectionModel.add(node2);
//                                        }
////                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//                selectionModel.log();
            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);

            pane.getChildren().remove(rect);
            drag.set(false);
            sprintf("Drag false: " + drag);
            event.consume();

        }

        private int parseNodeId(Node node, String nameToParse) {
            try {
                return Integer.parseInt(node.getId().replace(nameToParse, ""));
            } catch (Exception e) {
                return -1;
            }
        }
    };

//    public static Path getImageNameByID(String id) {
//        for (Map.Entry<String, Path> entry : id_path_map.entrySet()) {
//            if (entry.getKey().equals(id)) {
//                return entry.getValue();
//            }
//        }
//        return null;
//    }
}
