package com.redhat.ceylon.model.loader.model;

import java.util.Collections;
import java.util.List;

import com.redhat.ceylon.model.typechecker.model.Functional;
import com.redhat.ceylon.model.typechecker.model.Interface;
import com.redhat.ceylon.model.typechecker.model.Scope;
import com.redhat.ceylon.model.typechecker.model.TypeParameter;
import com.redhat.ceylon.model.typechecker.model.TypedDeclaration;
import com.redhat.ceylon.model.typechecker.model.Unit;

/**
 * Wrapper class which pretends a function or value is an interface, so that they can
 * be used to qualify local types in runtime reified checks.
 *
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class FunctionOrValueInterface extends Interface {

    private final TypedDeclaration declaration;

    public FunctionOrValueInterface(TypedDeclaration declaration){
        this.declaration = declaration;
    }
    
    @Override
    public String getQualifier() {
        return declaration.getQualifier();
    }
    
    @Override
    public String getName() {
        return declaration.getName();
    }
    
    @Override
    public Scope getContainer() {
        return declaration.getContainer();
    }
    
    @Override
    public List<TypeParameter> getTypeParameters() {
        return declaration instanceof Functional 
                ? ((Functional) declaration).getTypeParameters() 
                : Collections.<TypeParameter>emptyList();
    }

    @Override
    public Unit getUnit() {
        return declaration.getUnit();
    }
    
    public TypedDeclaration getUnderlyingDeclaration() {
        return declaration;
    }
}
