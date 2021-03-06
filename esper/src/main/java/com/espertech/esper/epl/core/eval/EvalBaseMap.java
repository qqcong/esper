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
import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.core.CodegenMethodId;
import com.espertech.esper.codegen.model.blocks.CodegenLegoMayVoid;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.codegen.model.method.CodegenParamSetSelectPremade;
import com.espertech.esper.epl.core.EngineImportService;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.core.SelectExprProcessorForge;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.core.ExprNodeUtility;
import com.espertech.esper.util.CollectionUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public abstract class EvalBaseMap extends EvalBase implements SelectExprProcessor, SelectExprProcessorForge {

    protected ExprEvaluator[] evaluators;

    protected EvalBaseMap(SelectExprForgeContext selectExprForgeContext, EventType resultEventType) {
        super(selectExprForgeContext, resultEventType);
    }

    protected abstract void initSelectExprProcessorSpecific(EngineImportService engineImportService, boolean isFireAndForget, String statementName);

    protected abstract EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext);

    protected abstract CodegenExpression processSpecificCodegen(CodegenMember memberResultEventType, CodegenMember memberEventAdapterService, CodegenExpression props, CodegenParamSetSelectPremade params, CodegenContext context);

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext) {
        String[] columnNames = context.getColumnNames();

        // Evaluate all expressions and build a map of name-value pairs
        Map<String, Object> props;
        if (evaluators.length == 0) {
            props = Collections.emptyMap();
        } else {
            props = new HashMap<>(CollectionUtil.capacityHashMap(evaluators.length));
            for (int i = 0; i < evaluators.length; i++) {
                Object evalResult = evaluators[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                props.put(columnNames[i], evalResult);
            }
        }

        return processSpecific(props, eventsPerStream, isNewData, isSynthesize, exprEvaluatorContext);
    }

    public CodegenExpression processCodegen(CodegenMember memberResultEventType, CodegenMember memberEventAdapterService, CodegenParamSetSelectPremade params, CodegenContext context) {
        CodegenBlock block = context.addMethod(EventBean.class, EvalBaseMap.class).add(params).begin();
        if (this.context.getExprForges().length == 0) {
            block.declareVar(Map.class, "props", staticMethod(Collections.class, "emptyMap"));
        } else {
            block.declareVar(Map.class, "props", newInstance(HashMap.class, constant(CollectionUtil.capacityHashMap(this.context.getColumnNames().length))));
        }
        for (int i = 0; i < this.context.getColumnNames().length; i++) {
            CodegenExpression expression = CodegenLegoMayVoid.expressionMayVoid(this.context.getExprForges()[i], CodegenParamSetExprPremade.INSTANCE, context);
            block.expression(exprDotMethod(ref("props"), "put", constant(this.context.getColumnNames()[i]), expression));
        }
        CodegenMethodId method = block.methodReturn(processSpecificCodegen(memberResultEventType, memberEventAdapterService, ref("props"), params, context));
        return localMethodBuild(method).passAll(params).call();
    }

    public SelectExprProcessor getSelectExprProcessor(EngineImportService engineImportService, boolean isFireAndForget, String statementName) {
        if (evaluators == null) {
            evaluators = ExprNodeUtility.getEvaluatorsMayCompile(context.getExprForges(), engineImportService, this.getClass(), isFireAndForget, statementName);
        }
        initSelectExprProcessorSpecific(engineImportService, isFireAndForget, statementName);
        return this;
    }
}