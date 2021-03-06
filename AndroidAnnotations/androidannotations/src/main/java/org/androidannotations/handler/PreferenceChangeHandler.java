/**
 * Copyright (C) 2010-2015 eBusiness Information, Excilys Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.androidannotations.handler;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.helper.APTCodeModelHelper;
import org.androidannotations.helper.CanonicalNameConstants;
import org.androidannotations.holder.HasPreferences;
import org.androidannotations.model.AnnotationElements;
import org.androidannotations.process.IsValid;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class PreferenceChangeHandler extends AbstractPreferenceListenerHandler {

	private final APTCodeModelHelper codeModelHelper = new APTCodeModelHelper();

	public PreferenceChangeHandler(ProcessingEnvironment processingEnvironment) {
		super(PreferenceChange.class, processingEnvironment);
	}

	@Override
	public void validate(Element element, AnnotationElements validatedElements, IsValid valid) {
		super.validate(element, validatedElements, valid);
		validatorHelper.enclosingElementExtendsPreferenceActivityOrPreferenceFragment(element, valid);

		ExecutableElement executableElement = (ExecutableElement) element;

		validatorHelper.returnTypeIsVoidOrBoolean(executableElement, valid);

		validatorHelper.param.anyOrder() //
				.type(CanonicalNameConstants.PREFERENCE).optional() //
				.anyOfTypes(CanonicalNameConstants.STRING, CanonicalNameConstants.BOOLEAN, boolean.class.getName(), CanonicalNameConstants.OBJECT, CanonicalNameConstants.SET).optional() //
				.validate(executableElement, valid);
	}

	@Override
	protected void makeCall(JBlock listenerMethodBody, JInvocation call, TypeMirror returnType) {
		boolean returnMethodResult = returnType.getKind() != TypeKind.VOID;
		if (returnMethodResult) {
			listenerMethodBody._return(call);
		} else {
			listenerMethodBody.add(call);
			listenerMethodBody._return(JExpr.TRUE);
		}
	}

	@Override
	protected void processParameters(HasPreferences holder, JMethod listenerMethod, JInvocation call, List<? extends VariableElement> userParameters) {
		JVar preferenceParam = listenerMethod.param(classes().PREFERENCE, "preference");
		JVar newValueParam = listenerMethod.param(classes().OBJECT, "newValue");

		for (VariableElement variableElement : userParameters) {
			if (variableElement.asType().toString().equals(CanonicalNameConstants.PREFERENCE)) {
				call.arg(preferenceParam);
			} else if (variableElement.asType().toString().equals(CanonicalNameConstants.OBJECT)) {
				call.arg(newValueParam);
			} else {
				JClass userParamClass = codeModelHelper.typeMirrorToJClass(variableElement.asType(), holder);
				call.arg(JExpr.cast(userParamClass, newValueParam));
			}
		}
	}

	@Override
	protected JMethod createListenerMethod(JDefinedClass listenerAnonymousClass) {
		return listenerAnonymousClass.method(JMod.PUBLIC, codeModel().BOOLEAN, "onPreferenceChange");
	}

	@Override
	protected String getSetterName() {
		return "setOnPreferenceChangeListener";
	}

	@Override
	protected JClass getListenerClass() {
		return classes().PREFERENCE_CHANGE_LISTENER;
	}

}
