package nars.nar.experimental;

import nars.Global;
import nars.bag.Bag;
import nars.budget.ItemAccumulator;
import nars.budget.ItemComparator;
import nars.concept.Concept;
import nars.cycle.DefaultCycle;
import nars.nal.Deriver;
import nars.nal.LogicPolicy;
import nars.nal.nal8.ImmediateOperator;
import nars.nar.Default;
import nars.premise.BloomFilterNovelPremiseGenerator;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.term.Term;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static nars.nar.NewDefault.nalex;

/**
 * Created by me on 7/21/15.
 */
public class Equalized extends Default {



    public Equalized(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(maxConcepts, conceptsFirePerCycle, termLinksPerCycle);
    }

    @Override
    public LogicPolicy getLogicPolicy() {
        return nalex(Deriver.defaults);
    }


    public static class EqualizedCycle extends DefaultCycle {

        /** stores sorted tasks temporarily */
        private final List<Task> temporary = Global.newArrayList();

        public EqualizedCycle(ItemAccumulator taskAccumulator, Bag<Term, Concept> concepts, AtomicInteger conceptsFiredPerCycle) {
            super(taskAccumulator, concepts, null, null, null, conceptsFiredPerCycle);
        }

        /**
         * An atomic working cycle of the system:
         *  0) optionally process inputs
         *  1) optionally process new task(s)
         *  2) optionally process novel task(s)
         *  2) optionally fire a concept
         **/
        @Override
        public void cycle() {

            int conceptsToFire = conceptsFiredPerCycle.get();

            concepts.forgetNext(
                    memory.param.conceptForgetDurations,
                    Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                    memory);

            //inputs
            if (memory.isInputting()) {

                //input all available percepts
                Task t;
                while ((t = percepts.get())!=null) {
                    if (t.isCommand())
                        memory.add(t);
                    else
                        newTasks.add(t);
                }
            }

            queueNewTasks();



            //new tasks
            float maxBusyness = conceptsFiredPerCycle.get(); //interpret concepts fired per cycle as business limit
            int newTasksToFire = newTasks.size();
            Iterator<Task> ii = newTasks.iterateHighestFirst(temporary);

            float priFactor = 1f / maxBusyness; //divide the procesesd priority by the expected busyness of this cycle to approximate 1.0 total

            float b = 0;
            for (int n = newTasksToFire;  ii.hasNext() && n > 0; n--) {
                Task next = ii.next();
                if (next == null) break;
                float nextPri = next.getPriority();

                TaskProcess tp = TaskProcess.get(memory, next, priFactor);
                if (tp!=null) {
                    tp.run();
                    b += next.getPriority();
                }

                ii.remove();

                if (b > maxBusyness)
                    break;
            }
            temporary.clear();



            //1 concept if (memory.newTasks.isEmpty())*/
            float conceptForgetDurations = memory.param.conceptForgetDurations.floatValue();
            ConceptProcess.forEachPremise(memory,
                    () -> nextConceptToProcess(conceptForgetDurations),
                    conceptsToFire, f -> f.run()
            );


            int added = commitNewTasks();

            //System.out.print("newTasks=" + newTasksToFire + " + " + added + "  ");

            //System.out.print("concepts=" + conceptsToFire + "  ");

            memory.runNextTasks();

            final int maxNewTasks = conceptsToFire * memory.duration();
            if (newTasks.size() > maxNewTasks) {
                int removed = newTasks.limit(maxNewTasks, new Consumer<Task>() {
                    @Override public void accept(Task task) {
                        memory.removed(task, "Ignored");
                    }
                }, temporary);

                //System.out.print("discarded=" + removed + "  ");
            }

            //System.out.println();


        }

        //final Frequency termTypes = new Frequency();

        private float getPriority(final Task task, int conceptsToFire) {
            //somehow this should be a function of:
            // # of concepts
            // concept forget rate
            // concept bag priority sum and distribution
            //float targetBusyness = 5;

            float f = (conceptsToFire);
            if (f > 1f) f = 1f;


//            final Compound term = task.getTerm();
//            long includedTerms = (term.structuralHash() & 0xffffffff);
//            long v = 1;
//            for (int i = 0; i < 32; i++) {
//                v = v << 1;
//                if ((includedTerms & v) > 0) {
//                    termTypes.addValue(Op.values()[i]);
//                }
//            }



//            //EXPERIMENTAL
//            double p = termTypes.getPct(term.operator());
//            f *= Math.max(0.1, 1.0 - p);


//            switch (term.operator()) {
//                case IMAGE_EXT:
//                case IMAGE_INT:
//                    f *= 0.5f;
//                    break;
//            }
//
//            if (Math.random() < 0.01) {
//                System.out.println(termTypes);
//            }

            return f;
        }


    }

    @Override
    public BloomFilterNovelPremiseGenerator newPremiseGenerator() {
        int novelCycles = duration.get();
        return new BloomFilterNovelPremiseGenerator(termLinkMaxMatched, novelCycles /* cycle to clear after */,
                novelCycles * conceptTaskTermProcessPerCycle.get(),
                0.01f /* false positive probability */ );
    }

    @Override
    public CycleProcess newCycleProcess() {
        return new EqualizedCycle(
                new ItemAccumulator(new ItemComparator.Plus()),
                newConceptBag(),
                conceptsFiredPerCycle
        );
    }

}
