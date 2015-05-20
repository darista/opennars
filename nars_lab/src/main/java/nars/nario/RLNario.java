package nars.nario;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import boofcv.alg.color.ColorHsv;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.ConvertRaster;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.MultiSpectral;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.gui.NARSwing;
import nars.gui.output.ImagePanel;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.rl.Perception;
import nars.rl.QLAgent;
import nars.rl.RawPerception;
import nars.rl.example.QVis;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by me on 4/26/15.
 */
public class RLNario extends NARio  {

    private final QVis mi;
    private final QLAgent agent;

    public RLNario(NAR nar, Perception... p) {
        super(nar);

        float fps = 30f;
        gameRate = 1.0f / fps;

        this.agent = new QLAgent(nar, "act", "<nario --> good>", this, p);

        agent.brain.setEpsilon(0.15);
        agent.brain.setAlpha(0.1);

        mi = new QVis(agent);


        Video.themeInvert();
        NARSwing s = new NARSwing(nar);
        s.setSpeed(0.5f);

    }

    @Override
    @Deprecated synchronized public double[] observe() {


        o.clear();

        o.addAll(dx/6.0, dy/6.0); //higher resolution velocity
        o.addAll(dx/12.0, dy/12.0); //coarse velocity

        for (boolean b : mario.keys)
            o.add(b ? 1 : -1);


        //o.addAll(radar);

        //Image screen = ((LevelScene) scene).layer.image;



        return o.toArray();
    }

    @Override
    protected void input(String sensed) {
        //ignore this input
    }


    @Override
    public void frame() {
        super.frame();
        mi.frame();
    }

    public static void main(String[] args) {


        NAR nar = new NAR(new Default(2000, 20, 4).setInternalExperience(null) );

        nar.param.duration.set(memoryCyclesPerFrame * 3);
        nar.setCyclesPerFrame(memoryCyclesPerFrame);

        nar.param.outputVolume.set(0);
        nar.param.decisionThreshold.set(0.65);
        nar.param.shortTermMemoryHistory.set(3);

        RLNario rl = new RLNario(nar,
                new RawPerception("r", 0.1f)
                //new RawPerception.BipolarDirectPerception("r", 0.7f),
                //new HaiSOMPerception("s", 3, 0.1f),
                //new AEPerception("a", 0.1f, 3, 2).setLearningRate(0.005).setSigmoid(false)
                //new AEPerception("b", 0.8f, 4).setLearningRate(0.08).setSigmoid(false)
        );

        rl.agent.add( new ImagePerception("i1", rl.getScreenImage()) {
            ImagePanel panel = new ImagePanel(400,400);
            NWindow w = new NWindow("i1", panel).show(500,400);

            public MultiSpectral<ImageFloat32> hsv;
            public MultiSpectral<ImageFloat32> rgb;
            public BufferedImage valueBuffer, hueBuffer;
            public BufferedImage output;

            @Override
            protected synchronized float[] process(BufferedImage i) {

                rgb = ConvertBufferedImage.convertFromMulti(i, rgb, false, ImageFloat32.class);

                if (hsv == null)
                    hsv = new MultiSpectral<ImageFloat32>(ImageFloat32.class,rgb.width,rgb.height,3);

                ColorHsv.rgbToHsv_F32(rgb, hsv);


                //valueBuffer = ConvertBufferedImage.convertTo(hsv, valueBuffer, false);

                ImageFloat32 value = hsv.getBand(2);
                valueBuffer = ConvertBufferedImage.convertTo(value, valueBuffer);


                ImageFloat32 hue = hsv.getBand(0);
                hueBuffer = ConvertBufferedImage.convertTo(hue, hueBuffer);

                final int targetWidth = valueBuffer.getWidth();
                final int targetHeight = valueBuffer.getHeight();
                if ((output == null || (output.getWidth() != targetWidth) || (output.getHeight() != targetHeight))) {
                    output = new BufferedImage(targetWidth, targetHeight, valueBuffer.getType());
                }
                Graphics2D g = (Graphics2D) output.getGraphics();
                g.drawImage(valueBuffer, 0, 0, null);
                //g.drawImage(hueBuffer, valueBuffer.getWidth(), 0, hueBuffer.getWidth()/2, hueBuffer.getHeight()/2, null);

                panel.setImage(valueBuffer);


                System.out.println(value.getWidth() + " " + value.getHeight());
                return value.getData();
            }
        });

        /*rl.agent.add( new ImagePerception("i1", rl.getLevelImage()) {

        });*/

    }

    abstract public static class ImagePerception implements Perception {

        private final Supplier<BufferedImage> source;
        private final String id;

        public ImagePerception( String id, Supplier<BufferedImage> source ) {
            this.source = source;
            this.id = id;
        }

        @Override
        public void init(RLEnvironment env, QLAgent agent) {


        }

        @Override
        public Iterable<Task> perceive(NAR nar, double[] input, double t) {
            BufferedImage i = source.get();
            float[] im = process(i);

            System.out.println(im.length);

            if (im==null) return null;

            List<Task> p = new ArrayList();
            return p;
        }

        protected abstract float[] process(BufferedImage i);

        @Override
        public boolean isState(Term t) {
            return false;
        }
    }
}
