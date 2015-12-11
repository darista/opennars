package nars.nal;

import com.google.common.collect.Lists;
import nars.Global;
import nars.NAR;
import nars.nar.Default;
import nars.nar.SingleStepNAR;
import nars.nar.Terminal;
import nars.util.meter.TestNAR;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.util.List;
import java.util.function.Supplier;

import static nars.util.data.LabeledSupplier.supply;

/**
 * Created by me on 2/10/15.
 */
@Ignore
public abstract class AbstractNALTester {


    //final ThreadLocal<NAR> nars;
    private final Supplier<NAR> nar;
    //private final NAR the;
    private TestNAR created;

    protected AbstractNALTester(NAR nar) {
        Global.DEBUG = true;
        this.nar = () -> nar;
    }

    protected AbstractNALTester(Supplier<NAR> nar) {
        Global.DEBUG = true;
        //this.the = nar.get();
        this.nar = nar;
    }


    public final TestNAR test() {
        return created;
    }


    public NAR nar() {
        //return the
        return nar.get();
    }

    public static Iterable<Supplier<NAR>> terminal() {
        return Lists.newArrayList(
            new Supplier[] {supply("Terminal", Terminal::new)}
        );
    }

    @Before
    public void start() {
        created = new TestNAR(nar());
    }
    @After
    public void end() {
        created.run2();
    }

    @Deprecated public static Iterable<Supplier<NAR>> nars(int level, boolean requireMultistep) {
        return requireMultistep ? nars(level, false, true) : nars(level, true, true);
    }

    public static Iterable<Supplier<NAR>> nars(int level, boolean single, boolean multi) {

        //Level adjustments
        {
            //HACK bump to level 3 is somehow necessary
            if (level == 2) level = 3;
        }

        List<Supplier<NAR>> l = Global.newArrayList(2);

        int finalLevel = level;

        if (multi) {
            l.add(supply("Default2[NAL<=" + level + ']',
                    () -> {
                        Default d = new Default(512, 2, 2, 2);
                        d.nal(finalLevel);
                        return d;
                    }
            ));
        }

        if (single) {
            l.add( supply("SingleStep[NAL<=" + level + ']',
                    () -> new SingleStepNAR().nal(finalLevel) ) );
        }

        return l;
    }

}
