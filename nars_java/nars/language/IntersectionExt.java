/*
 * IntersectionExt.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.language;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import nars.core.Parameters;
import nars.io.Symbols.NativeOperator;
import static nars.language.SetTensional.verifySortedAndUnique;

/**
 * A compound term whose extension is the intersection of the extensions of its term
 */
public class IntersectionExt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private IntersectionExt(Term[] arg) {
        super(arg);
        
        if (Parameters.DEBUG) {
            verifySortedAndUnique(arg, false);
        }
        
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a IntersectionExt
     */
    @Override
    public IntersectionExt clone() {
        return new IntersectionExt(term);
    }
    
    @Override
    public CompoundTerm clone(Term[] replaced) {
        if (replaced.length == 1)
            return (CompoundTerm) replaced[0];
        else if (replaced.length > 1)
            return (CompoundTerm) make(replaced);
        else
            throw new RuntimeException("Invalid # of terms for Intersection: " + Arrays.toString(replaced));
    }
    
    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param term1 The first compoment
     * @param term2 The first compoment
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term term1, Term term2) {
        Set<Term> set;
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            set = new HashSet<>(((CompoundTerm) term1).getTermList());
            set.addAll(((CompoundTerm) term2).getTermList());        // set union
            return SetInt.make(set);
        }
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            set = new HashSet<>(((CompoundTerm) term1).getTermList());
            set.retainAll(((CompoundTerm) term2).getTermList());     // set intersection
            return SetExt.make(set);
        }
        if (term1 instanceof IntersectionExt) {
            set = new HashSet<>(((CompoundTerm) term1).getTermList());
            if (term2 instanceof IntersectionExt) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                set.addAll(((CompoundTerm) term2).getTermList());
            }               
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                set.add(term2);
            }               
        } else if (term2 instanceof IntersectionExt) {
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            set = new HashSet<>(((CompoundTerm) term2).getTermList());
            set.add(term1);
        } else {
            set = new HashSet<>();
            set.add(term1);
            set.add(term2);
        }
        return make(set.toArray(new Term[set.size()]));
    }

    /**
     * Try to make a new IntersectionExt.  Called when the input may contain duplicates
     * @return the Term generated from the arguments
     * @param argList The list of term
     * @param memory Reference to the memory
     */
    public static Term makeUnduplicated(Term... t) {
        if (t.length == 1) return t[0]; // special case: single component        
        Set<Term> s = new HashSet();
        for (Term x : t) s.add(x);
        return make(s.toArray(new Term[s.size()]));
    }
    
    public static Term make(Term... t) {
        if (t.length == 1) return t[0]; // special case: single component        
        
        Term[] s = Term.toSortedSetArray(t);        
        return new IntersectionExt(s);
    }



    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.INTERSECTION_EXT;
    }

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
