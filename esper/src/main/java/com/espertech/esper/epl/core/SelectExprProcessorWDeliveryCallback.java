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
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder;
import com.espertech.esper.codegen.model.method.CodegenParamSetSelectPremade;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.exprDotMethod;

/**
 * Interface for processors of select-clause items, implementors are computing results based on matching events.
 */
public class SelectExprProcessorWDeliveryCallback implements SelectExprProcessor, SelectExprProcessorForge {
    private final EventType eventType;
    private final BindProcessorForge bindProcessorForge;
    private final SelectExprProcessorDeliveryCallback selectExprProcessorCallback;
    private BindProcessor bindProcessor;

    public SelectExprProcessorWDeliveryCallback(EventType eventType, BindProcessorForge bindProcessorForge, SelectExprProcessorDeliveryCallback selectExprProcessorCallback) {
        this.eventType = eventType;
        this.bindProcessorForge = bindProcessorForge;
        this.selectExprProcessorCallback = selectExprProcessorCallback;
    }

    public EventType getResultEventType() {
        return eventType;
    }

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext) {
        Object[] columns = bindProcessor.process(eventsPerStream, isNewData, exprEvaluatorContext);
        return selectExprProcessorCallback.selected(columns);
    }

    public SelectExprProcessor getSelectExprProcessor(EngineImportService engineImportService, boolean isFireAndForget, String statementName) {
        if (bindProcessor == null) {
            bindProcessor = bindProcessorForge.getBindProcessor(engineImportService, isFireAndForget, statementName);
        }
        return this;
    }

    public CodegenExpression processCodegen(CodegenMember memberResultEventType, CodegenMember memberEventAdapterService, CodegenParamSetSelectPremade params, CodegenContext context) {
        CodegenMember memberCallback = context.makeAddMember(SelectExprProcessorDeliveryCallback.class, selectExprProcessorCallback);
        return exprDotMethod(CodegenExpressionBuilder.member(memberCallback.getMemberId()), "selected", bindProcessorForge.processCodegen(params, context));
    }
}
