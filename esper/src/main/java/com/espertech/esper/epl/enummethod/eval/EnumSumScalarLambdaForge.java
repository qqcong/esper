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
import com.espertech.esper.event.arr.ObjectArrayEventType;

public class EnumSumScalarLambdaForge extends EnumForgeBase {

    protected final ExprDotEvalSumMethodFactory sumMethodFactory;
    protected final ObjectArrayEventType resultEventType;

    public EnumSumScalarLambdaForge(ExprForge innerExpression, int streamCountIncoming, ExprDotEvalSumMethodFactory sumMethodFactory, ObjectArrayEventType resultEventType) {
        super(innerExpression, streamCountIncoming);
        this.sumMethodFactory = sumMethodFactory;
        this.resultEventType = resultEventType;
    }

    public EnumEval getEnumEvaluator() {
        return new EnumSumScalarLambdaForgeEval(this, innerExpression.getExprEvaluator());
    }

    public CodegenExpression codegen(CodegenParamSetEnumMethodNonPremade premade, CodegenContext context) {
        return EnumSumScalarLambdaForgeEval.codegen(this, premade, context);
    }
}
