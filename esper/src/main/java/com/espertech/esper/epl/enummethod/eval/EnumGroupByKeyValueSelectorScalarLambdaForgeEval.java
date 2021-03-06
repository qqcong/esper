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

import java.util.*;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class EnumGroupByKeyValueSelectorScalarLambdaForgeEval implements EnumEval {

    private final EnumGroupByKeyValueSelectorScalarLambdaForge forge;
    private final ExprEvaluator innerExpression;
    private final ExprEvaluator secondExpression;

    public EnumGroupByKeyValueSelectorScalarLambdaForgeEval(EnumGroupByKeyValueSelectorScalarLambdaForge forge, ExprEvaluator innerExpression, ExprEvaluator secondExpression) {
        this.forge = forge;
        this.innerExpression = innerExpression;
        this.secondExpression = secondExpression;
    }

    public Object evaluateEnumMethod(EventBean[] eventsLambda, Collection enumcoll, boolean isNewData, ExprEvaluatorContext context) {
        if (enumcoll.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Object, Collection> result = new LinkedHashMap<Object, Collection>();

        ObjectArrayEventBean resultEvent = new ObjectArrayEventBean(new Object[1], forge.resultEventType);
        Object[] props = resultEvent.getProperties();
        eventsLambda[forge.streamNumLambda] = resultEvent;
        Collection<Object> values = (Collection<Object>) enumcoll;

        for (Object next : values) {

            props[0] = next;
            Object key = innerExpression.evaluate(eventsLambda, isNewData, context);
            Object entry = secondExpression.evaluate(eventsLambda, isNewData, context);

            Collection value = result.get(key);
            if (value == null) {
                value = new ArrayList();
                result.put(key, value);
            }
            value.add(entry);
        }

        return result;
    }

    public static CodegenExpression codegen(EnumGroupByKeyValueSelectorScalarLambdaForge forge, CodegenParamSetEnumMethodNonPremade args, CodegenContext context) {
        CodegenMember resultTypeMember = context.makeAddMember(ObjectArrayEventType.class, forge.resultEventType);
        CodegenParamSetEnumMethodPremade premade = CodegenParamSetEnumMethodPremade.INSTANCE;
        CodegenBlock block = context.addMethod(Map.class, EnumGroupByKeyValueSelectorScalarLambdaForgeEval.class).add(premade).begin()
                .ifCondition(exprDotMethod(premade.enumcoll(), "isEmpty"))
                .blockReturn(staticMethod(Collections.class, "emptyMap"))
                .declareVar(Map.class, "result", newInstance(LinkedHashMap.class))
                .declareVar(ObjectArrayEventBean.class, "resultEvent", newInstance(ObjectArrayEventBean.class, newArray(Object.class, constant(1)), member(resultTypeMember.getMemberId())))
                .assignArrayElement(premade.eps(), constant(forge.streamNumLambda), ref("resultEvent"))
                .declareVar(Object[].class, "props", exprDotMethod(ref("resultEvent"), "getProperties"));

        block.forEach(Object.class, "next", premade.enumcoll())
                .assignArrayElement("props", constant(0), ref("next"))
                .declareVar(Object.class, "key", forge.innerExpression.evaluateCodegen(CodegenParamSetExprPremade.INSTANCE, context))
                .declareVar(Object.class, "entry", forge.secondExpression.evaluateCodegen(CodegenParamSetExprPremade.INSTANCE, context))
                .declareVar(Collection.class, "value", cast(Collection.class, exprDotMethod(ref("result"), "get", ref("key"))))
                .ifRefNull("value")
                .assignRef("value", newInstance(ArrayList.class))
                .expression(exprDotMethod(ref("result"), "put", ref("key"), ref("value")))
                .blockEnd()
                .expression(exprDotMethod(ref("value"), "add", ref("entry")));
        CodegenMethodId method = block.methodReturn(ref("result"));
        return localMethodBuild(method).passAll(args).call();
    }
}
