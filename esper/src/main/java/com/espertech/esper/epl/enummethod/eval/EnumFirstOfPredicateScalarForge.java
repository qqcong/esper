/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.epl.enummethod.eval;

import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetEnumMethodNonPremade;
import com.espertech.esper.epl.expression.core.ExprForge;
import com.espertech.esper.epl.rettype.EPType;
import com.espertech.esper.event.arr.ObjectArrayEventType;

public class EnumFirstOfPredicateScalarForge extends EnumForgeBaseScalar {

    protected final EPType resultType;

    public EnumFirstOfPredicateScalarForge(ExprForge innerExpression, int streamCountIncoming, ObjectArrayEventType type, EPType resultType) {
        super(innerExpression, streamCountIncoming, type);
        this.resultType = resultType;
    }

    public EnumEval getEnumEvaluator() {
        return new EnumFirstOfPredicateScalarForgeEval(this, innerExpression.getExprEvaluator());
    }

    public CodegenExpression codegen(CodegenParamSetEnumMethodNonPremade premade, CodegenContext context) {
        return EnumFirstOfPredicateScalarForgeEval.codegen(this, premade, context);
    }
}
