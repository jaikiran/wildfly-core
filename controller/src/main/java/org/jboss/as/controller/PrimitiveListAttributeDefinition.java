/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.controller;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Date: 13.10.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Richard Achmatowicz (c) 2012 RedHat Inc.
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 */
public class PrimitiveListAttributeDefinition extends ListAttributeDefinition {
    private final ModelType valueType;

    PrimitiveListAttributeDefinition(final ListAttributeDefinition.Builder builder, ModelType valueType) {
        super(builder);
        this.valueType = valueType;
    }


    public ModelType getValueType() {
        return valueType;
    }

    @Override
    public ModelNode addResourceAttributeDescription(ResourceBundle bundle, String prefix, ModelNode resourceDescription) {
        final ModelNode result = super.addResourceAttributeDescription(bundle, prefix, resourceDescription);
        addValueTypeDescription(result);
        return result;
    }

    @Override
    protected void addValueTypeDescription(final ModelNode node, final ResourceBundle bundle) {
        addValueTypeDescription(node);
    }


    protected void addValueTypeDescription(final ModelNode node) {
        if (isAllowExpression()) {
            node.get(ModelDescriptionConstants.EXPRESSIONS_ALLOWED).set(true);
        }
        node.get(ModelDescriptionConstants.VALUE_TYPE).set(valueType);
    }

    @Override
    protected ModelNode convertParameterElementExpressions(ModelNode parameterElement) {
        if (isAllowExpression() && COMPLEX_TYPES.contains(valueType)) {
            // This implementation isn't suitable. Must be overridden
            throw new IllegalStateException();
        }
        return super.convertParameterElementExpressions(parameterElement);
    }

    @Override
    protected void addAttributeValueTypeDescription(final ModelNode node, final ResourceDescriptionResolver resolver, final Locale locale, final ResourceBundle bundle) {
        addValueTypeDescription(node);
    }

    @Override
    protected void addOperationParameterValueTypeDescription(final ModelNode node, final String operationName, final ResourceDescriptionResolver resolver, final Locale locale, final ResourceBundle bundle) {
        addValueTypeDescription(node);
    }

    @Override
    public void addCapabilityRequirements(OperationContext context, Resource resource, ModelNode attributeValue) {
        handleCapabilityRequirements(context, resource, attributeValue, false);
    }

    @Override
    public void removeCapabilityRequirements(OperationContext context, Resource resource, ModelNode attributeValue) {
        handleCapabilityRequirements(context, resource, attributeValue, true);
    }

    @Override
    ModelNode parseResolvedValue(ModelNode original, ModelNode resolved) {
        return parseSingleElementToList(this, original, resolved);
    }

    static ModelNode parseSingleElementToList(AttributeDefinition ad, ModelNode original, ModelNode resolved) {
        ModelNode result = resolved;
        if (original.isDefined()
                && !resolved.equals(original)
                && resolved.getType() == ModelType.LIST
                && resolved.asInt() == 1) {
            // WFCORE-3448. We have a list with 1 element that is not the same as the defined original.
            // So that implies we had an expression as the element, which is what we would have gotten
            // if the expression string was passed by an xml parser to parseAndSetParameter. See if the
            // resolved form of that expression in turn parses to a list and if it does, used the parsed list.
            ModelNode element = resolved.get(0);
            if (element.getType() == ModelType.STRING) {
                ModelNode holder = new ModelNode();
                try {
                    ad.getParser().parseAndSetParameter(ad, element.asString(), holder, null);
                    ModelNode parsed = holder.get(ad.getName());
                    if (parsed.getType() == ModelType.LIST && parsed.asInt() > 1) {
                        result = parsed;
                    }
                } catch (XMLStreamException | RuntimeException e) {
                    // ignore and just return the original value
                }
            }
        }
        return result;
    }

    private void handleCapabilityRequirements(OperationContext context, Resource resource, ModelNode attributeValue, boolean remove) {
        if (referenceRecorder != null && attributeValue.isDefined()) {
            List<ModelNode> valueList = attributeValue.asList();
            String[] attributeValues = new String[valueList.size()];
            int position = 0;
            for (ModelNode current : valueList) {
                if (!current.isDefined() || current.getType().equals(ModelType.EXPRESSION)) {
                    return;
                }
                attributeValues[position++] = current.asString();
            }
            if (remove) {
                referenceRecorder.removeCapabilityRequirements(context, resource, getName(), attributeValues);
            } else {
                referenceRecorder.addCapabilityRequirements(context, resource, getName(), attributeValues);
            }
        }
    }

    public static class Builder extends ListAttributeDefinition.Builder<Builder, PrimitiveListAttributeDefinition> {

        private final ModelType valueType;

        public Builder(final String name, final ModelType valueType) {
            super(name);
            this.valueType = valueType;
            setElementValidator(new ModelTypeValidator(valueType));
        }

        public Builder(final PrimitiveListAttributeDefinition basis) {
            super(basis);
            this.valueType = basis.getValueType();
        }

        public static Builder of(final String name, final ModelType valueType) {
            return new Builder(name, valueType);
        }

        public ModelType getValueType() {
            return valueType;
        }

        public PrimitiveListAttributeDefinition build() {
            return new PrimitiveListAttributeDefinition(this, getValueType());
        }
    }
}
