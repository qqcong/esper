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

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.core.CodegenMethodId;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetEnumMethodNonPremade;
import com.espertech.esper.codegen.model.method.CodegenParamSetEnumMethodPremade;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.event.arr.ObjectArrayEventBean;
import com.espertech.esper.event.arr.ObjectArrayEventType;

import java.util.Collection;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class EnumSumScalarLambdaForgeEval implements EnumEval {

    private final EnumSumScalarLambdaForge forge;
    private final ExprEvaluator innerExpression;

    public EnumSumScalarLambdaForgeEval(EnumSumScalarLambdaForge forge, ExprEvaluator innerExpression) {
        this.forge = forge;
        this.innerExpression = innerExpression;
    }

    public Object evaluateEnumMethod(EventBean[] eventsLambda, Collection enumcoll, boolean isNewData, ExprEvaluatorContext context) {

        ExprDotEvalSumMethod method = forge.sumMethodFactory.getSumAggregator();

        ObjectArrayEventBean resultEvent = new ObjectArrayEventBean(new Object[1], forge.resultEventType);
        eventsLambda[forge.streamNumLambda] = resultEvent;
        Object[] props = resultEvent.getProperties();

        Collection<Object> values = (Collection<Object>) enumcoll;
        for (Object next : values) {
            props[0] = next;

            Object value = innerExpression.evaluate(eventsLambda, isNewData, context);
            method.enter(value);
        }

        return method.getValue();
    }

    public static CodegenExpression codegen(EnumSumScalarLambdaForge forge, CodegenParamSetEnumMethodNonPremade args, CodegenContext context) {
        CodegenParamSetEnumMethodPremade premade = CodegenParamSetEnumMethodPremade.INSTANCE;
        CodegenBlock block = context.addMethod(forge.sumMethodFactory.getValueType(), EnumSumEventsForgeEval.class).add(premade).begin();
        Class innerType = forge.innerExpression.getEvaluationType();
        CodegenMember resultTypeMember = context.makeAddMember(ObjectArrayEventType.class, forge.resultEventType);

        forge.sumMethodFactory.codegenDeclare(block);
        block.declareVar(ObjectArrayEventBean.class, "resultEvent", newInstance(ObjectArrayEventBean.class, newArray(Object.class, constant(1)), member(resultTypeMember.getMemberId())))
                .assignArrayElement(premade.eps(), constant(forge.streamNumLambda), ref("resultEvent"))
                .declareVar(Object[].class, "props", exprDotMethod(ref("resultEvent"), "getProperties"));

        CodegenBlock forEach = block.forEach(Object.class, "next", premade.enumcoll())
                .assignArrayElement("props", constant(0), ref("next"))
                .declareVar(innerType, "value", forge.innerExpression.evaluateCodegen(CodegenParamSetExprPremade.INSTANCE, context));
        if (!innerType.isPrimitive()) {
            forEach.ifRefNull("value").blockContinue();
        }
        forge.sumMethodFactory.codegenEnterNumberTypedNonNull(forEach, ref("value"));

        CodegenMethodId method = forge.sumMethodFactory.codegenReturn(block);
        return localMethodBuild(method).passAll(args).call();
    }
}
