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
package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder;
import com.espertech.esper.codegen.model.method.CodegenParamSetSelectPremade;
import com.espertech.esper.epl.core.EngineImportService;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.core.SelectExprProcessorForge;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;

import java.util.Map;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.exprDotMethod;

public class EvalInsertWildcardJoin extends EvalBaseMap implements SelectExprProcessor {

    private final SelectExprProcessorForge joinWildcardProcessorForge;
    private SelectExprProcessor joinWildcardProcessor;

    public EvalInsertWildcardJoin(SelectExprForgeContext selectExprForgeContext, EventType resultEventType, SelectExprProcessorForge joinWildcardProcessorForge) {
        super(selectExprForgeContext, resultEventType);
        this.joinWildcardProcessorForge = joinWildcardProcessorForge;
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext) {
        EventBean theEvent = joinWildcardProcessor.process(eventsPerStream, isNewData, isSynthesize, exprEvaluatorContext);
        // Using a wrapper bean since we cannot use the same event type else same-type filters match.
        // Wrapping it even when not adding properties is very inexpensive.
        return super.getEventAdapterService().adapterForTypedWrapper(theEvent, props, super.getResultEventType());
    }

    protected void initSelectExprProcessorSpecific(EngineImportService engineImportService, boolean isFireAndForget, String statementName) {
        joinWildcardProcessor = joinWildcardProcessorForge.getSelectExprProcessor(engineImportService, isFireAndForget, statementName);
    }

    protected CodegenExpression processSpecificCodegen(CodegenMember memberResultEventType, CodegenMember memberEventAdapterService, CodegenExpression props, CodegenParamSetSelectPremade params, CodegenContext context) {
        CodegenExpression inner = joinWildcardProcessorForge.processCodegen(memberResultEventType, memberEventAdapterService, params, context);
        return exprDotMethod(CodegenExpressionBuilder.member(memberEventAdapterService.getMemberId()), "adapterForTypedWrapper", inner, props, CodegenExpressionBuilder.member(memberResultEventType.getMemberId()));
    }
}
