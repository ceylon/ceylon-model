/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.redhat.ceylon.model.loader.impl.reflect;

import java.lang.reflect.Member;
import java.util.List;

import com.redhat.ceylon.common.JVMModuleUtil;
import com.redhat.ceylon.common.log.Logger;
import com.redhat.ceylon.model.loader.AbstractModelLoader;
import com.redhat.ceylon.model.loader.Timer;
import com.redhat.ceylon.model.loader.TypeParser;
import com.redhat.ceylon.model.loader.impl.reflect.mirror.ReflectionClass;
import com.redhat.ceylon.model.loader.impl.reflect.mirror.ReflectionMethod;
import com.redhat.ceylon.model.loader.mirror.ClassMirror;
import com.redhat.ceylon.model.loader.mirror.MethodMirror;
import com.redhat.ceylon.model.typechecker.model.Module;
import com.redhat.ceylon.model.typechecker.model.Modules;
import com.redhat.ceylon.model.typechecker.model.Unit;
import com.redhat.ceylon.model.typechecker.util.ModuleManager;

/**
 * A model loader which uses Java reflection.
 *
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public abstract class ReflectionModelLoader extends AbstractModelLoader {
	protected Logger log;
	
    public ReflectionModelLoader(ModuleManager moduleManager, Modules modules, Logger log){
        this.moduleManager = moduleManager;
        this.modules = modules;
        this.typeFactory = new Unit();
        this.typeParser = new TypeParser(this);
        this.timer = new Timer(false);
        this.log = log;
    }

    protected abstract List<String> getPackageList(Module module, String packageName);
    protected abstract boolean packageExists(Module module, String packageName);
    protected abstract Class<?> loadClass(Module module, String name);

    @Override
    public void loadStandardModules() {
        super.loadStandardModules();
        // load two packages for the language module
        Module languageModule = modules.getLanguageModule();
        findOrCreatePackage(languageModule, AbstractModelLoader.CEYLON_LANGUAGE);
        findOrCreatePackage(languageModule, AbstractModelLoader.CEYLON_LANGUAGE_MODEL);
        findOrCreatePackage(languageModule, AbstractModelLoader.CEYLON_LANGUAGE_MODEL_DECLARATION);
        findOrCreatePackage(languageModule, AbstractModelLoader.CEYLON_LANGUAGE_SERIALIZATION);
    }
    
    @Override
    public boolean loadPackage(Module module, String packageName, boolean loadDeclarations) {
        // abort if we already loaded it, but only record that we loaded it if we want
        // to load the declarations, because merely calling complete() on the package
        // is OK
        packageName = JVMModuleUtil.quoteJavaKeywords(packageName);
        if(loadDeclarations && !loadedPackages.add(cacheKeyByModule(module, packageName))){
            return true;
        }
        if(!packageExists(module, packageName))
            return false;
        if(loadDeclarations){
            for(String file : getPackageList(module, packageName)){
                // ignore non-class stuff
                if(!file.toLowerCase().endsWith(".class"))
                    continue;
                // turn it into a class name
                // FIXME: this is terrible
                String className = file.substring(0, file.length()-6).replace('/', '.');
                // get the last part
                int lastDot = className.lastIndexOf('.');
                String lastPart = lastDot == -1 ? className : className.substring(lastDot+1);
                int dollar = lastPart.indexOf('$');
                // if we have a dollar after the first char (where it would be quoting), skip it
                // because those are local/member/anonymous/impl ones
                if(dollar > 0)
                    continue;
                // skip module/package declarations too (do not strip before checking)
                if(isModuleOrPackageDescriptorName(lastPart))
                    continue;

                // the logic for lower-cased names should be abstracted somewhere sane
                if(!isLoadedFromSource(className) 
                        && (!className.endsWith("_") || !isLoadedFromSource(className.substring(0, className.length()-1)))
                        && !isTypeHidden(module, className))
                    convertToDeclaration(module, className, DeclarationType.TYPE);
            }
            if(module.getNameAsString().equals(JAVA_BASE_MODULE_NAME)
                    && packageName.equals("java.lang"))
                loadJavaBaseArrays();
        }
        return true;
    }

    protected boolean isLoadedFromSource(String className) {
        return false;
    }

    @Override
    public ClassMirror lookupNewClassMirror(Module module, String name) {
        Class<?> klass = null;
        // first try with the same name, for Java interop with classes with lowercase name
        klass = loadClass(module, JVMModuleUtil.quoteJavaKeywords(name));
        if (klass == null && lastPartHasLowerInitial(name) && !name.endsWith("_")) {
            klass = loadClass(module, JVMModuleUtil.quoteJavaKeywords(name+"_"));
        }
        return klass != null ? new ReflectionClass(klass) : null;
    }

    @Override
    protected String assembleJavaClass(String javaClass, String packageName) {
        // strip the java class name of its package part
        if(!packageName.isEmpty())
            javaClass = javaClass.substring(packageName.length()+1); // pkg + dot
        // now replace every dot in the name part with $
        javaClass = javaClass.replace('.', '$');
        // assemble back
        if(packageName.isEmpty())
            return javaClass;
        return packageName + "." + javaClass;
    }
    
    @Override
    protected boolean isOverridingMethod(MethodMirror methodSymbol) {
        final Member method = ((ReflectionMethod)methodSymbol).method;
        if (method.getDeclaringClass().getName().contentEquals("ceylon.language.Identifiable")) {
            if (method.getName().contentEquals("equals") || method.getName().contentEquals("hashCode")) {
                return true;
            }
        }
        if (method.getDeclaringClass().getName().contentEquals("ceylon.language.Object")) {
            if (method.getName().contentEquals("equals") || method.getName().contentEquals("hashCode") || method.getName().contentEquals("toString")) {
                return false;
            }
        }
        return ((ReflectionMethod)methodSymbol).isOverridingMethod();
    }
    
    @Override
    protected boolean isOverloadingMethod(MethodMirror methodSymbol) {
        return ((ReflectionMethod)methodSymbol).isOverloadingMethod();
    }

    @Override
    protected void logError(String message) {
        log.error(message);
    }
    
    @Override
    protected void logWarning(String message) {
        log.warning(message);
    }
    
    @Override
    protected void logVerbose(String message) {
        log.debug(message);
    }

}
