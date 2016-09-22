/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.web.faces;

import org.ecloudmanager.deployment.vm.provisioning.ChefAttribute;
import org.ecloudmanager.jeecore.web.faces.Controller;

import javax.inject.Inject;
import java.util.List;

@Controller
public class ChefAttributeController extends EntityEditorController<ChefAttribute> {
    private static final String ENV_DEFAULT = "Default";
    private static final String ENV_OVERRIDE = "Override";
    private static final String ENV_NO = "No";

    @Inject
    RecipeController recipeController;

    protected ChefAttributeController() {
        super(ChefAttribute.class);
    }

    public String getEditAttributeEnv() {
        if (getSelected().isEnvironmentAttribute()) {
            if (getSelected().isEnvironmentDefaultAttribute()) {
                return ENV_DEFAULT;
            } else {
                return ENV_OVERRIDE;
            }
        } else {
            return ENV_NO;
        }
    }

    public void setEditAttributeEnv(String editAttributeEnv) {
        if (ENV_NO.equals(editAttributeEnv)) {
            getSelected().setEnvironmentAttribute(false);
        } else if (ENV_DEFAULT.equals(editAttributeEnv)) {
            getSelected().setEnvironmentAttribute(true);
            getSelected().setEnvironmentDefaultAttribute(true);
        } else {
            getSelected().setEnvironmentAttribute(true);
            getSelected().setEnvironmentDefaultAttribute(false);
        }
    }

    @Override
    public void delete(ChefAttribute entity) {
    }

    @Override
    protected void doSave(ChefAttribute old, ChefAttribute entity) {
        if (isEdit()) {
            List<ChefAttribute> attributes = recipeController.getValue().getAttributes();
            int position = attributes.indexOf(old);
            attributes.add(position, entity);
            attributes.remove(old);
        } else {
            doAdd(entity);
        }
    }

    @Override
    protected void doAdd(ChefAttribute entity) {
        recipeController.getValue().getAttributes().add(entity);
    }
}
