package nars.guifx.graph2.layout;

import automenta.vivisect.dimensionalize.IterativeLayout;
import nars.guifx.graph2.NARGraph;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;

/**
 * for layouts which process items according to their
 * linear sequence position
 */
public abstract class Linear<N extends TermNode> implements IterativeLayout<N,TermEdge> {

    @Override
    public void init(N n) {
        //n/a
    }

    /** sets the position of a vertex at specified position order */
    public abstract void setPosition(TermNode vertex, int sequence, int max);

    @Override
    public void run(NARGraph graph, int iterations) {
        final TermNode[] verts = graph.displayed;

        int i = 0;


        for (TermNode v : verts) {
            if (v == null)  continue; //break?

            setPosition(v, i++, verts.length);
        }
    }


}
