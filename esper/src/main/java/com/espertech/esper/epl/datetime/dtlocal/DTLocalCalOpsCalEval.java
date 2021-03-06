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
package com.espertech.esper.epl.datetime.dtlocal;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMethodId;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.datetime.calop.CalendarOp;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;

import java.util.Calendar;
import java.util.List;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.epl.datetime.dtlocal.DTLocalUtil.evaluateCalOpsCalendarCodegen;

public class DTLocalCalOpsCalEval extends DTLocalEvaluatorCalOpsCalBase implements DTLocalEvaluator {
    public DTLocalCalOpsCalEval(List<CalendarOp> calendarOps) {
        super(calendarOps);
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        Calendar calValue = (Calendar) target;
        Calendar cal = (Calendar) calValue.clone();

        DTLocalUtil.evaluateCalOpsCalendar(calendarOps, cal, eventsPerStream, isNewData, exprEvaluatorContext);

        return cal;
    }

    public static CodegenExpression codegen(DTLocalCalOpsCalForge forge, CodegenExpression inner, CodegenParamSetExprPremade params, CodegenContext context) {
        CodegenBlock block = context.addMethod(Calendar.class, DTLocalCalOpsCalEval.class).add(Calendar.class, "target").add(params).begin()
                .declareVar(Calendar.class, "cal", cast(Calendar.class, exprDotMethod(ref("target"), "clone")));
        evaluateCalOpsCalendarCodegen(block, forge.calendarForges, ref("cal"), params, context);
        CodegenMethodId method = block.methodReturn(ref("cal"));
        return localMethodBuild(method).pass(inner).passAll(params).call();
    }
}
