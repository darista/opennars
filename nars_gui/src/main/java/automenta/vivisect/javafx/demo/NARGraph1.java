package automenta.vivisect.javafx.demo;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import automenta.vivisect.dimensionalize.IterativeLayout;
import automenta.vivisect.javafx.Spacegraph;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import nars.Global;
import nars.NAR;
import nars.NARStream;
import nars.concept.Concept;
import nars.guifx.NARfx;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.util.FastMath;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class NARGraph1 extends Spacegraph {

    public interface VisModel {

        Color getEdgeColor(double termPrio, double taskMean);

        Paint getVertexColor(double priority, float conf);

        double getVertexScale(Concept c);
    }


    private NAR nar;
    private Timeline time;

    static final Random rng = new XORShiftRandom();


    final AtomicBoolean nodeDirty = new AtomicBoolean(true),
            edgeDirty = new AtomicBoolean(true);

    public static Polygon newPoly(int sides, double d) {

        Polygon polygon = new Polygon();
        polygon.setStrokeWidth(0);
        polygon.setStroke(null);
        double da = (2 * Math.PI) / sides, a = 0;
        double r = d / 2;
        for (int i = 0; i < sides; i++) {
            polygon.getPoints().addAll(
                    r * Math.cos(a),
                    r * Math.sin(a)
            );
            a += da;
        }
        return polygon;
    }


    final static Font nodeFont = NARfx.mono(1);

    public class TermNode extends Group {



        private final Term term;
        private final Text titleBar;
        private final Polygon base;
        Concept c = null;

        private double priorityDisplayed = -1;

        /**
         * granularity for discretizing displayed scales to reduces # of updates
         */
        final static double priorityDisplayedResolution = 100;


        double minSize = 16;
        double maxSize = 64;

        /**
         * cached from last set
         */
        private double scaled;
        private double tx;
        private double ty;

        private boolean hover = false;
        private Color stroke;

        public TermNode(Term t) {
            super();

            this.titleBar = new Text(t.toStringCompact());
            base = newPoly(6, 2.0);




            this.term = t;
            this.c = nar.concept(t);

            randomPosition(30, 30);

            titleBar.setFill(Color.WHITE);

            titleBar.setPickOnBounds(false);
            titleBar.setMouseTransparent(true);
            titleBar.setFont(nodeFont);
            titleBar.setTextAlignment(TextAlignment.CENTER);
            titleBar.setSmooth(false);


            base.setOnMouseClicked(e -> {
                //System.out.println("click " + e.getClickCount());
                if (e.getClickCount() == 2) {
                    if (c != null)
                        NARfx.window(nar, c);
                }
            });

            EventHandler<MouseEvent> mouseActivity = e -> {
                if (!hover) {
                    base.setStroke(Color.ORANGE);
                    base.setStrokeWidth(0.05);
                    base.setStrokeType(StrokeType.INSIDE);
                    hover = true;
                }
            };
            //base.setOnMouseMoved(mouseActivity);
            base.setOnMouseEntered(mouseActivity);
            base.setOnMouseExited(e -> {
                if (hover) {
                    base.setStroke(null);
                    base.setStrokeWidth(0);
                    hover = false;
                }
            });

            setPickOnBounds(false);


            getChildren().setAll(base, titleBar);//, titleBar);



            update();

            base.setLayoutX(-0.5f);
            base.setLayoutY(-0.5f);

            /*titleBar.setScaleX(0.25f);
            titleBar.setScaleY(0.25f);*/
            titleBar.setLayoutX(-getLayoutBounds().getWidth()/(2)+0.25);
            //titleBar.setY(-getLayoutBounds().getHeight()/2);
//            System.out.println(titleBar);
//            System.out.println(titleBar.getLayoutBounds());
//            System.out.println(titleBar.getLocalToParentTransform());
//            System.out.println(titleBar.getLocalToSceneTransform());
//            System.out.println(titleBar.getBoundsInLocal());


            //setCache(true);
            //setCacheHint(CacheHint.SPEED);
            titleBar.setCacheHint(CacheHint.SCALE_AND_ROTATE);
            titleBar.setCache(true);

            /*double s = 1.0 / titleBar.getBoundsInLocal().getWidth();

            titleBar.setScaleX(s);
            titleBar.setScaleY(s);*/

            //getChildren().add(new Rectangle(1,1))

        }


        public void randomPosition(double bx, double by) {

            move(rng.nextDouble() * bx, rng.nextDouble() * by);
        }

        public void update() {

            double vertexScaling = visModel.getVertexScale(c);

            if ((int) (priorityDisplayedResolution * priorityDisplayed) !=
                    (int) (priorityDisplayedResolution * vertexScaling)) {

                double scale = minSize + (maxSize - minSize) * vertexScaling;
                this.scaled = scale;

                setScaleX(scale);
                setScaleY(scale);

                float conf = c != null ? c.getBeliefs().getConfidenceMax(0, 1) : 0;
                base.setFill(visModel.getVertexColor(vertexScaling, conf));

                //setOpacity(0.75f + 0.25f * vertexScaling);

                this.priorityDisplayed = vertexScaling;
                //System.out.println(scale + " " + vertexScaling + " " + (int)(priorityDisplayedResolution * vertexScaling));
            }

        }


        public void getPosition(final double[] v) {
            v[0] = getTranslateX();
            v[1] = getTranslateY();
        }

        final public TermNode move(final double x, final double y) {
            setTranslateX(this.tx = x);
            setTranslateY(this.ty = y);
            return this;
        }

        final public boolean move(final double[] v, final double threshold) {
            final double x = getTranslateX();
            final double y = getTranslateY();
            final double nx = v[0];
            final double ny = v[1];
            if (!((Math.abs(x - nx) < threshold) && (Math.abs(y - ny) < threshold))) {
                move(nx, ny);
                return true;
            }
            return false;
        }

        public double width() {
            return scaled; //getScaleX();
        }

        public double height() {
            return scaled; //getScaleY();
        }

        public double x() {
            return tx; //getTranslateX();
        }

        public double y() {
            return ty; //getTranslateY();
        }
    }

    public VisModel visModel = new VisModel() {

        public Color getVertexColor(double priority, float conf) {
            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

            if (!Double.isFinite(conf)) {
                conf = 0;
            }

            return Color.hsb(250.0 + 75.0 * (conf),
                    0.10f + 0.85f * priority,
                    0.10f + 0.5f * priority);


        }


        public double getVertexScaleByPri(Concept c) {
            return (c != null ? c.getPriority() : 0);
        }

        public double getVertexScaleByConf(Concept c) {
            double conf = c.getBeliefs().getConfidenceMax(0, 1);
            if (Double.isFinite(conf)) return conf;
            return 0;
        }

        @Override
        public double getVertexScale(Concept c) {
            return getVertexScaleByConf(c) * 0.75f + 0.25f;
            //return getVertexScaleByPri(c);
        }

        public Color getEdgeColor(double termMean, double taskMean) {
            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

            return Color.hsb(25.0 + 180.0 * (1.0 + (termMean - taskMean)),
                    0.95f,
                    Math.min(0.25f + 0.75f * (termMean + taskMean) / 2f, 1f));


//        return new Color(
//                0.5f + 0.5f * termMean,
//                0,
//                0.5f + 0.5f * taskMean,
//                0.5f + 0.5f * (termMean + taskMean)/2f
//        );
        }

    };

    float edgeThickness = 0.003f;

    public class TermEdge extends Group implements ChangeListener {

        public final TermNode bSrc;
        public final TermNode aSrc;
        final TermEdgeHalf a, b;

        private final Translate translate;
        private final Rotate rotate;
        private final Scale scale;
        private AtomicBoolean changed = new AtomicBoolean(true);
        public double len;

        public TermEdge(TermNode aSrc, TermNode bSrc) {

            this.aSrc = aSrc;
            this.bSrc = bSrc;

            a = new TermEdgeHalf(aSrc, bSrc, this);
            a.setVisible(false);
            b = new TermEdgeHalf(bSrc, aSrc, this);
            b.setVisible(false);

            if (aSrc.term.compareTo(bSrc.term)>0) {
                throw new RuntimeException("invalid term order for TermEdge: " + aSrc + " " + bSrc);
            }

            getChildren().setAll( a, b );

            //aSrc.layoutXProperty().addListener(this);
            //aSrc.layoutXProperty().addListener(this);
            aSrc.localToSceneTransformProperty().addListener(this);
            bSrc.localToSceneTransformProperty().addListener(this);


            getTransforms().setAll(
                    translate = Transform.translate(0, 0),
                    rotate = Transform.rotate(0, 0, 0),
                    scale = Transform.scale(0, 0)
            );

        }

        public void delete() {
            aSrc.localToSceneTransformProperty().removeListener(this);
            bSrc.localToSceneTransformProperty().removeListener(this);
        }

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {

            changed.set(true);

        }

        //        private void setA(TermNode aSrc) {
//            this.aSrc = aSrc;
//            a.setVisible(aSrc!=null);
//        }
//
//        private void setB(TermNode bSrc) {
//            this.bSrc = bSrc;
//            b.setVisible(bSrc!=null);
//        }

        public void update() {

            changed.set(false);


            if (!aSrc.isVisible() || !bSrc.isVisible()) {
                setVisible(false);
                return;
            }

            double x1 = aSrc.x();// + fw / 2d;
            double y1 = aSrc.y();// + fh / 2d;
            double x2 = bSrc.x();// + tw / 2d;
            double y2 = bSrc.y();// + th / 2d;
            double dx = (x1 - x2);
            double dy = (y1 - y2);
            this.len = Math.sqrt(dx * dx + dy * dy);
            //len-=fw/2;

            //double rot = Math.atan2(dy, dx);
            double rot = FastMath.atan2(dy, dx);
            double cx = 0.5f * (x1 + x2);
            double cy = 0.5f * (y1 + y2);


            translate.setX(cx);
            translate.setY(cy);
            rotate.setAngle(FastMath.toDegrees(rot));
            scale.setX(len);
            scale.setY(len);



            a.update();
            b.update();

        }

        public final TermNode otherNode(final TermNode x) {
            if (aSrc == x) return bSrc;
            return aSrc;
        }

    }

    public class TermEdgeHalf extends Polygon {

        private final TermEdge edge;
        long lastUpdate = -1;

        //final double minPriVisiblityThresh = 0.1;
        final double minThickVisibility = 0.05;

        private final TermNode from;
        private final TermNode to;

        float taskPri = 0, termPri = 0;
        private int tasks;

        SimpleDoubleProperty thickness = new SimpleDoubleProperty();

        public void set(TaskLink t, long when) {
            if (lastUpdate!=when) {
                reset(when);
            }

            taskPri += t.getPriority();
            tasks++;
        }

        protected void reset(long when) {
            edge.changed.set(true);
            taskPri = termPri = 0;
            tasks = 0;
            lastUpdate = when;
        }

        public void set(TermLink t, long when) {
            if (lastUpdate!=when) {
                reset(when);
            }

            termPri += t.getPriority();
        }

//        final protected void dirty(boolean newValue) {
//            dirty.set(newValue);
//            if (newValue)
//                edgeDirty.set(true);
//        }
//


        public TermEdgeHalf(TermNode from, TermNode to, TermEdge termEdge) {
            super();
            this.from = from;
            this.to = to;
            this.edge = termEdge;

            //setCache(true);
            //setCacheHint(CacheHint.SCALE_AND_ROTATE);

            //setManaged(false);
            setStrokeWidth(0);
            setStroke(null);

            //getPoints().setAll(0.5d, 0d, -0.5d, -0.5d, -0.5d, +0.5d); //isoceles triangle within -0.5,-0.5...0.5,0.5 (len/wid = 1)

            double q = 0.25f;
            if (!order(from.term, to.term)) {
                getPoints().setAll(0.5d, 0d, -0.5d, q, -0.5d, -q); //right triangle
            }
            else {
                //180deg rotate
                getPoints().setAll(-0.5d, 0d, 0.5d, -q, 0.5d, q); //right triangle
            }

            thickness.addListener((t) -> {
                double T = thickness.doubleValue();

                if (T <= 0) {
                    setVisible(false);
                    return;
                }

                double fw = from.width();
                //double fh = from.height();
                double tw = to.width();
                //double th = to.height();
                double thickness = T * edgeThickness * Math.min(fw,tw);

                if (thickness < minThickVisibility) {
                    setVisible(false);
                    return;
                }


                setVisible(true);
                setScaleY(thickness);
                setFill(visModel.getEdgeColor(termPri, taskPri / tasks));
                setOpacity(T);

            });

        }


        public void update() {
            if (termPri > 1) termPri = 1;
            float taskPriMean = tasks > 0 ? taskPri/tasks : 0;
            if (taskPriMean > 1) taskPriMean = 1f;


            thickness.set( 0.5f * (taskPriMean + termPri) );
        }

//        public final void updateIfVisible() {
//
//
////            int numTasks = taskLinks.size();
////            final double taskSum, taskMean;
////            if (numTasks > 0) {
////                this.taskPrioSum = taskSum = taskLinks.stream()
////                        .mapToDouble(t -> t.getPriority()).sum();//.orElse(0);
////                taskMean = taskSum / numTasks;
////            } else {
////                taskSum = taskMean = 0;
////            }
//
//            //temporary
//            //float taskMean = termPri + taskPri;
//
//            //final double termPrio = termLink != null ? termLink.getPriority() : 0;
//            //this.thickness = (taskMean + termPrio);
//
//            //dirty(false);
//        }



    }

    final Map<Term, TermNode> terms = new LinkedHashMap();
    final Map<Term, TermNode> termToAdd = new LinkedHashMap();
    final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
    final Table<Term, Term, TermEdge> edgeToAdd = HashBasedTable.create();

    int maxTerms = 64;

    public TermNode getTermNode(final Term t) {
        TermNode tn = terms.get(t);
        if (tn == null) {
            tn = termToAdd.get(t);
            if (tn == null) {
                tn = new TermNode(t);
                termToAdd.put(t, tn);
            }
        }
        return tn;
    }

    public TermEdgeHalf getConceptEdgeHalf(final TermNode s, final TermNode t) {
        TermEdge parent = getConceptEdge(s, t);
        if (order(s.term,t.term)) {
            return parent.a;
        }
        else {
            return parent.b;
        }
    }

    static boolean order(final Term x, final Term y) {
        final int i = x.compareTo(y);
        if (i == 0) throw new RuntimeException("order=0 but must be non-equal");
        return i < 0;
    }

    public TermEdge getConceptEdge(TermNode s, TermNode t) {

        if (!order(s.term, t.term)) {
            TermNode x = s;
            s = t;
            t = x;
        }

        TermEdge e = edges.get(s.term, t.term);

        if (e == null) {
            e = edgeToAdd.get(s.term, t.term);
            if (e == null) {
                e = new TermEdge(s, t);
                edgeToAdd.put(s.term, t.term, e);
            }
        }
        return e;
    }

    final Set<Term> toRemove = Global.newHashSet(1);

    public synchronized void updateGraph() {
        int n = 0;

        toRemove.addAll(terms.keySet());

        final long now = nar.time();

        for (Concept c : nar.memory.getControl()) {

            final Term source = c.getTerm();
            TermNode sn = getTermNode(source);

            c.getTaskLinks().forEach(t -> {
                Term target = t.getTarget();
                if (!source.equals(target.getTerm())) {
                    TermNode tn = getTermNode(target);
                    TermEdgeHalf e = getConceptEdgeHalf(sn, tn);
                    e.set(t, now);
                }
            });

            c.getTermLinks().forEach(t -> {
                Term target = t.getTarget();
                TermNode tn = getTermNode(target);
                TermEdgeHalf e = getConceptEdgeHalf(sn, tn);
                e.set(t, now);
            });

            toRemove.remove(source);

            if (n++ > maxTerms)
                break;
        }

        if (!toRemove.isEmpty()) {
            final Term[] tr = toRemove.toArray(new Term[toRemove.size()]);
            toRemove.clear();
            runLater(() -> {
                for (Term r : tr) {
                    TermNode c = terms.remove(r);
                    if (c != null)
                        removeNodes(c);

                    Map<Term, TermEdge> er = edges.rowMap().remove(r);
                    if (er != null)
                        removeEdges((Collection) er.values());

                    Map<Term, TermEdge> ec = edges.columnMap().remove(r);
                    if (ec != null)
                        removeEdges((Collection) ec.values());
                }
            });
        }

        if (!termToAdd.isEmpty()) {
            TermNode[] x = termToAdd.values().toArray(new TermNode[termToAdd.size()]);
            termToAdd.clear();
            runLater(() -> {
                for (TermNode tn : x)
                    terms.put(tn.term, tn);
                addNodes(x);
            });
        }

        if (!edgeToAdd.isEmpty()) {
            TermEdge[] x = edgeToAdd.values().toArray(new TermEdge[edgeToAdd.size()]);
            edgeToAdd.clear();
            runLater(() -> {
                for (TermEdge te : x)
                    edges.put(te.aSrc.term, te.bSrc.term, te);
                addEdges(x);
            });
        }


    }

    @FunctionalInterface public interface PreallocatedResultFunction<X,Y> {
        public void apply(X x, Y setResultHereAndReturnIt);
    }
    @FunctionalInterface public interface PairConsumer<A,B> {
        public void accept(A a, B b);
    }

    public static class CircleLayout<N, E> implements IterativeLayout<N, E> {


        public void run(Collection<N> verts,
                        //PreallocatedResultFunction<N,double[]> getPosition,
                        ToDoubleFunction<N> radiusFraction,
                        ToDoubleFunction<N> angle,
                        PairConsumer<N,double[]> setPosition) {


            double d[] = new double[2];

            verts.forEach(v -> {
                final double r = radiusFraction.applyAsDouble(v);
                final double a = angle.applyAsDouble(v);
                d[0] = Math.cos(a) * r;
                d[1] = Math.sin(a) * r;
                setPosition.accept(v, d);
            });

        }

        @Override
        public ArrayRealVector getPosition(N vertex) {
            return null;
        }

        @Override
        public void run(int iterations) {

        }

        @Override
        public void resetLearning() {

        }

        @Override
        public double getRadius(N vertex) {
            return 0;
        }

    };

    IterativeLayout<TermNode, TermEdge> layout = null;

    HyperassociativeMap<TermNode, TermEdge> h = null;

    protected void layoutNodes() {
        if (layout == null) {
            layout = new CircleLayout<TermNode,TermEdge>();
        }

        double[] i = new double[1];
        double numFraction = Math.PI * 2.0 * 1.0 / terms.size();
        double radiusMin = Math.sqrt(terms.size() + 1) * 120;
        double radiusMax = 2f * radiusMin;

        ((CircleLayout<TermNode,TermEdge>)layout).
            run(terms.values(),
            (v) -> {
                double r = 1f - (v.c != null ? v.c.getPriority() : 0);
                double min = radiusMin;
                double max = radiusMax;
                return r * (max-min) + min;
            },
            (v) -> {
                //return Math.PI*2*(v.term.hashCode() % 8192)/8192.0;
                i[0] += numFraction;
                return i[0];
            },
            (v, d) -> {
                v.move(d[0], d[1]);
            });


    }

    protected void layoutNodesHyper() {


        if (h == null) {


            h = new HyperassociativeMap<TermNode, TermEdge>(2) {
                float termRadius = 1;

                @Override
                public void getPosition(final TermNode node, final double[] v) {
                    node.getPosition(v);
                }

                /*
                @Override
                public double getEdgeWeight(TermEdge termEdge) {
                    ///doesnt do anything in this anymore
                }
                */

                /*@Override
                public double getRadius(TermNode termNode) {
                    return super.getRadius(termNode);
                }*/

                @Override
                public boolean normalize() {
                    return true;
                }


                @Override
                public double getRadius(TermNode termNode) {
                    //return termNode.width() / 2 / scaleFactor / 2;
                    return 0.01;
                }

                @Override
                public double getSpeedFactor(TermNode termNode) {
                    return 10 / termNode.width(); //heavier is slower, forcing smaller ones to move faster around it
                }

                @Override
                public void apply(TermNode node, double[] dataRef) {
                    node.move(dataRef, 0.01);
                }

                @Override
                protected Collection<TermNode> getVertices() {
                    double scaleFactor = 250 + 150 * Math.sqrt(1 + terms.size());
                    setScale(scaleFactor);

                    //termRadius = (float) (1.0f / Math.sqrt(terms.size() + 1));

                    setEquilibriumDistance(0.01f); //termRadius * 1.5f);

                    return terms.values();
                }

                @Override
                protected void edges(final TermNode nodeToQuery, Consumer<TermNode> updateFunc, boolean ins, boolean outs) {
                    edges.values().forEach(new TermEdgeConsumer(updateFunc, nodeToQuery));
                }

            };


            h.setRepulsiveWeakness(10.0);
            h.setAttractionStrength(7.0);
            h.setMaxRepulsionDistance(100.5);

        }

        h.align();

        h.apply();

        //h.resetLearning();


    }

    protected void updateNodes() {

        for (TermNode n : terms.values()) {
            n.update();
        }

    }

    protected void updateEdges() {
        //if (edgeDirty.get()) {
        //edgeDirty.set(false);

        for (TermEdge e : edges.values()) {
            if (e.changed.get())
                e.update();
        }

        //}
    }


    public NARGraph1(NAR n) {

        super();



        new NARStream(this.nar = n)
                //.stdout()
                //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")
                .forEachNthFrame(this::updateGraph, 1);
                /*.forEachCycle(() -> {
                    double[] dd = new double[4];
                    nar.memory.getControl().conceptPriorityHistogram(dd);
                    System.out.println( Arrays.toString(dd) );

                    System.out.println(
                            nar.memory.getActivePrioritySum(true, false, false) +
                            " " +
                            nar.memory.getActivePrioritySum(false, true, false) +
                            " " +
                            nar.memory.getActivePrioritySum(false, false, true)  );

                })*/

        new Animate(60, a -> {
            updateNodes();
            updateEdges();
            if (terms.size() > 0) {
                layoutNodes();
            }
        }).start();


    }


    private class TermEdgeConsumer implements Consumer<TermEdge> {
        private final Consumer<TermNode> updateFunc;
        private final TermNode nodeToQuery;

        public TermEdgeConsumer(Consumer<TermNode> updateFunc, TermNode nodeToQuery) {
            this.updateFunc = updateFunc;
            this.nodeToQuery = nodeToQuery;
        }

        @Override
        public void accept(TermEdge te) {
            if (te.isVisible())
                updateFunc.accept(te.otherNode(nodeToQuery));
        }
    }
}
