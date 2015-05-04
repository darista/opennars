package nars.rl.example;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import com.google.common.collect.Iterables;
import jurls.core.utils.MatrixImage;
import nars.nal.concept.Concept;
import nars.nal.nal8.Operation;
import nars.nal.term.Term;
import nars.rl.QEntry;
import nars.rl.QLAgent;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by me on 5/2/15.
 */
public class QVis extends MatrixImage implements Runnable {

    final java.util.List<Term> xstates = new ArrayList();
    final java.util.List<Operation> xactions = new ArrayList();
    private final QLAgent<Term> agent;
    private final NWindow nmi;

    Data2D mid = new Data2D() {

        @Override
        public double getValue(final int y, final int x) {
            return agent.q(xstates.get(x), xactions.get(y));
        }

    };


    public QVis(QLAgent agent) {
        super(400, 400);
        this.agent = agent;

        nmi = new NWindow("Q", this).show(400, 400);
    }

    @Override
    public void run() {
        repaint();


        //if (xstates.size() != states.size()) {
        xstates.clear();
        Iterables.addAll(xstates, agent.rows);
        //}
        //if (xactions.size() != actions.size()) {
        xactions.clear();
        Iterables.addAll(xactions, agent.cols);
        //}

        repaint();


        draw(mid, xstates.size(), xactions.size(), -1, 1);

        nmi.setTitle(xstates.size() + " states, " + xactions.size() + " actions");

//                    mi.draw(new Data2D() {
//                        @Override
//                        public double getValue(int x, int y) {
//                            return q(y, x);
//                        }
//                    }, nstates, nactions, -1, 1);
    }

    public int val2col(final double n, final double min, final double max, final float bright) {
        final double mean = (max + min) / 2.0;
        final double n5 = min + 2.0 * (max - min) / 3.0;
        double r;
        if (n < mean) {
            r = (255.0 * (min - n) / (mean - min)) + 255;
        } else {
            r = 0;
        }
        if (r < 0) {
            r = 0;
        }
        if (r > 255) {
            r = 255;
        }
        double g;
        if (n < mean) {
            g = (255.0 * (n - min) / (mean - min));
        } else if (n < n5) {
            g = 255;
        } else {
            g = (255.0 * (n5 - n) / (max - n5)) + 255.0;
        }
        if (g < 0) {
            g = 0;
        }
        if (g > 255) {
            g = 255;
        }
        double b;
        if (n < mean) {
            b = 0;
        } else if (n < n5) {
            b = (255.0 * (n - mean) / (n5 - mean));
        } else {
            b = 255.0;
        }
        if (b < 0) {
            b = 0;
        }
        if (b > 255) {
            b = 255;
        }

        r = r * bright;
        g = g * bright;
        b = b * bright;

        int ir = (int)r;
        int ig = (int)g;
        int ib = (int)b;

        return 255 << 24 | ib << 16 | ig << 8 | ir;
    }


    @Override
    protected void pixel(final BufferedImage image, final int j, final int i, final double value) {

        float pri = 0;
        float elig = 0;

        QEntry v = agent.table.get(xstates.get(j), xactions.get(i));
        if (v != null) {
            Concept c = v.getConcept();
            if (c != null) {
                pri = c.getPriority();
            }
            elig = (float) v.getE();
        }


        image.setRGB(j, i,
                Video.colorHSB(
                        0.05f + 0.95f * ((float)(-value)/2f + 0.5f),
                        Math.min(1.0f, 0.8f + elig*0.2f),
                        0.75f + 0.25f * pri
                        )
        );
                //val2col(value, -1, 1, 0.5f + 0.5f * pri));

    }

//    @Override
//    public void draw(Data2D d, int cw, int ch, double minValue, double maxValue) {
//        super.draw(d, cw, ch, minValue, maxValue);
////
////        for (int i = 0; i < ch; i++) {
////            for (int j = 0; j < cw; j++) {
////                final double value = d.getValue(i, j);
////
////                float pri = 0;
////
////                QEntry v = agent.table.get(xstates.get(j), xactions.get(i));
////                if (v != null) {
////                    Concept c = v.getConcept();
////                    if (c != null) {
////                        pri = c.getPriority();
////                    }
////                }
////
////                int ipri = (int) (127 * pri) + 127;
////                int p = image.getRGB(j, i);
////
////                //dim
////                p &= (ipri << 24) | 0xffffff; // | (ipri << 16) | (ipri << 8) | (ipri);
////
////                image.setRGB(j, i, p);
////            }
////        }
//    }

}