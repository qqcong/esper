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
package com.espertech.esper.avro.getter;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMethodId;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.event.EventPropertyGetterSPI;
import org.apache.avro.generic.GenericData;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class AvroEventBeanGetterNestedDynamicSimple implements EventPropertyGetterSPI {

    private final int posTop;
    private final String propertyName;

    public AvroEventBeanGetterNestedDynamicSimple(int posTop, String propertyName) {
        this.posTop = posTop;
        this.propertyName = propertyName;
    }

    public Object get(EventBean eventBean) throws PropertyAccessException {
        return get((GenericData.Record) eventBean.getUnderlying());
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return isExistsProperty((GenericData.Record) eventBean.getUnderlying());
    }

    private Object get(GenericData.Record record) throws PropertyAccessException {
        GenericData.Record inner = (GenericData.Record) record.get(posTop);
        if (inner == null) {
            return null;
        }
        return inner.get(propertyName);
    }

    private CodegenMethodId getCodegen(CodegenContext context) {
        return context.addMethod(Object.class, this.getClass()).add(GenericData.Record.class, "record").begin()
                .declareVar(GenericData.Record.class, "inner", cast(GenericData.Record.class, exprDotMethod(ref("record"), "get", constant(posTop))))
                .ifRefNullReturnNull("inner")
                .methodReturn(exprDotMethod(ref("inner"), "get", constant(propertyName)));
    }

    private boolean isExistsProperty(GenericData.Record record) {
        GenericData.Record inner = (GenericData.Record) record.get(posTop);
        if (inner == null) {
            return false;
        }
        return inner.getSchema().getField(propertyName) != null;
    }

    private CodegenMethodId isExistsPropertyCodegen(CodegenContext context) {
        return context.addMethod(boolean.class, this.getClass()).add(GenericData.Record.class, "record").begin()
                .declareVar(GenericData.Record.class, "inner", cast(GenericData.Record.class, exprDotMethod(ref("record"), "get", constant(posTop))))
                .ifRefNullReturnFalse("inner")
                .methodReturn(notEqualsNull(exprDotMethodChain(ref("inner")).add("getSchema").add("getField", constant(propertyName))));
    }

    public Object getFragment(EventBean eventBean) throws PropertyAccessException {
        return null;
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingGetCodegen(castUnderlying(GenericData.Record.class, beanExpression), context);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingExistsCodegen(castUnderlying(GenericData.Record.class, beanExpression), context);
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return constantNull();
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(getCodegen(context), underlyingExpression);
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(isExistsPropertyCodegen(context), underlyingExpression);
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return constantNull();
    }
}
