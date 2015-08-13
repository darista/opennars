package nars.nal.nal9;


import nars.NARSeed;
import nars.nal.ScriptNALTest;
import nars.nar.Default;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.in.LibraryInput.getParams;


public class OtherTests extends ScriptNALTest {

    public OtherTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{/*"operator",*/ "other"},
                new Default()
        );
    }

    @Override
    public int getMaxCycles() { return 600; }


}

