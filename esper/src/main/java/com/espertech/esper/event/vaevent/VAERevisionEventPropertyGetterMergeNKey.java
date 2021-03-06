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
package com.espertech.esper.event.vaevent;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMethodId;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.event.EventPropertyGetterSPI;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.event.vaevent.VAERevisionEventPropertyGetterDeclaredGetVersioned.revisionImplementationNotProvided;

public class VAERevisionEventPropertyGetterMergeNKey implements EventPropertyGetterSPI {
    private final int keyPropertyNumber;

    public VAERevisionEventPropertyGetterMergeNKey(int keyPropertyNumber) {
        this.keyPropertyNumber = keyPropertyNumber;
    }

    public Object get(EventBean eventBean) throws PropertyAccessException {
        RevisionEventBeanMerge riv = (RevisionEventBeanMerge) eventBean;
        MultiKeyUntyped mk = (MultiKeyUntyped) riv.getKey();
        return mk.getKeys()[keyPropertyNumber];
    }

    private CodegenMethodId getCodegen(CodegenContext context) {
        return context.addMethod(Object.class, this.getClass()).add(EventBean.class, "eventBean").begin()
                .declareVar(RevisionEventBeanMerge.class, "riv", cast(RevisionEventBeanMerge.class, ref("eventBean")))
                .declareVar(MultiKeyUntyped.class, "mk", cast(MultiKeyUntyped.class, exprDotMethod(ref("riv"), "getKey")))
                .methodReturn(arrayAtIndex(exprDotMethod(ref("mk"), "getKeys"), constant(keyPropertyNumber)));
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return true;
    }

    public Object getFragment(EventBean eventBean) {
        return null;
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return localMethod(getCodegen(context), beanExpression);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return constantNull();
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        throw revisionImplementationNotProvided();
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        throw revisionImplementationNotProvided();
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        throw revisionImplementationNotProvided();
    }
}
