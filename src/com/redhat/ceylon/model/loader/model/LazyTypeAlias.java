package com.redhat.ceylon.model.loader.model;

import java.util.List;
import java.util.Map;

import com.redhat.ceylon.model.loader.JvmBackendUtil;
import com.redhat.ceylon.model.loader.ModelCompleter;
import com.redhat.ceylon.model.loader.mirror.ClassMirror;
import com.redhat.ceylon.model.typechecker.model.Annotation;
import com.redhat.ceylon.model.typechecker.model.Declaration;
import com.redhat.ceylon.model.typechecker.model.DeclarationKind;
import com.redhat.ceylon.model.typechecker.model.DeclarationWithProximity;
import com.redhat.ceylon.model.typechecker.model.Import;
import com.redhat.ceylon.model.typechecker.model.Reference;
import com.redhat.ceylon.model.typechecker.model.Type;
import com.redhat.ceylon.model.typechecker.model.Scope;
import com.redhat.ceylon.model.typechecker.model.TypeAlias;
import com.redhat.ceylon.model.typechecker.model.TypeDeclaration;
import com.redhat.ceylon.model.typechecker.model.TypeParameter;
import com.redhat.ceylon.model.typechecker.model.Unit;

/**
 * Represents a lazy type alias
 *
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class LazyTypeAlias extends TypeAlias implements LazyContainer {

    public ClassMirror classMirror;
    private ModelCompleter completer;
    
    private boolean isLoaded = false;
    private boolean isLoaded2 = false;
    private boolean isTypeParamsLoaded = false;
    private boolean isTypeParamsLoaded2 = false;
    private boolean local;

    @Override
    protected Class<?> getModelClass() {
        return getClass().getSuperclass(); 
    }
    
    public LazyTypeAlias(ClassMirror classMirror, ModelCompleter completer) {
        this.classMirror = classMirror;
        this.completer = completer;
        setName(JvmBackendUtil.getMirrorName(classMirror));
    }

    private void load() {
        if(!isLoaded2){
            synchronized(completer.getLock()){
                loadTypeParams();
                if(!isLoaded){
                    isLoaded = true;
                    completer.complete(this);
                    isLoaded2 = true;
                }
            }
        }
    }

    private void loadTypeParams() {
        if(!isTypeParamsLoaded2){
            synchronized(completer.getLock()){
                if(!isTypeParamsLoaded){
                    isTypeParamsLoaded = true;
                    completer.completeTypeParameters(this);
                    isTypeParamsLoaded2 = true;
                }
            }
        }
    }


    @Override
    public String toString() {
        if (!isLoaded) {
            return "UNLOADED:" + super.toString();
        }
        return super.toString();
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void addMember(Declaration decl) {
        // do this without lazy-loading
        super.getMembers().add(decl);
    }

    @Override
    public Type appliedType(Type outerType, List<Type> typeArguments) {
        loadTypeParams();
        return super.appliedType(outerType, typeArguments);
    }

    @Override
    public DeclarationKind getDeclarationKind() {
        load();
        return super.getDeclarationKind();
    }

    @Override
    public boolean isAlias() {
        // does not require lazy loading since it depends on class
        return super.isAlias();
    }

    @Override
    protected TypeDeclaration clone() {
        load();
        return super.clone();
    }

    @Override
    public boolean isParameterized() {
        load();
        return super.isParameterized();
    }

    @Override
    public List<TypeParameter> getTypeParameters() {
        loadTypeParams();
        return super.getTypeParameters();
    }

    @Override
    public Type getExtendedType() {
        load();
        return super.getExtendedType();
    }

    @Override
    public List<Type> getSatisfiedTypes() {
        load();
        return super.getSatisfiedTypes();
    }

    @Override
    public List<Type> getCaseTypes() {
        load();
        return super.getCaseTypes();
    }

    @Override
    public Reference appliedReference(Type pt, List<Type> typeArguments) {
        loadTypeParams();
        return super.appliedReference(pt, typeArguments);
    }

    @Override
    public Type getType() {
        loadTypeParams();
        return super.getType();
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<Declaration> getInheritedMembers(String name) {
        load();
        return super.getInheritedMembers(name);
    }

    @Override
    public boolean isMember(Declaration dec) {
        load();
        return super.isMember(dec);
    }

    @Override
    public boolean inherits(TypeDeclaration dec) {
        load();
        return super.inherits(dec);
    }

    @Override
    public Declaration getRefinedMember(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getRefinedMember(name, signature, ellipsis);
    }

    @Override
    public Declaration getMember(String name, Unit unit, List<Type> signature, boolean ellipsis) {
        load();
        return super.getMember(name, unit, signature, ellipsis);
    }

    @Override
    public Declaration getMember(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getMember(name, signature, ellipsis);
    }

    @Override
    public Declaration getMemberOrParameter(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getMemberOrParameter(name, signature, ellipsis);
    }

    @Override
    public boolean isInherited(Declaration d) {
        load();
        return super.isInherited(d);
    }

    @Override
    public TypeDeclaration getInheritingDeclaration(Declaration d) {
        load();
        return super.getInheritingDeclaration(d);
    }

    @Override
    public boolean isInheritedFromSupertype(Declaration member) {
        load();
        return super.isInheritedFromSupertype(member);
    }

    @Override
    public Type getSelfType() {
        load();
        return super.getSelfType();
    }

    @Override
    public Map<String, DeclarationWithProximity> getImportableDeclarations(Unit unit, String startingWith, List<Import> imports, int proximity) {
        load();
        return super.getImportableDeclarations(unit, startingWith, imports, proximity);
    }

    @Override
    public Map<String, DeclarationWithProximity> getMatchingDeclarations(Unit unit, String startingWith, int proximity) {
        load();
        return super.getMatchingDeclarations(unit, startingWith, proximity);
    }

    @Override
    public Map<String, DeclarationWithProximity> getMatchingMemberDeclarations(Unit unit, Scope scope, String startingWith, int proximity) {
        load();
        return super.getMatchingMemberDeclarations(unit, scope, startingWith, proximity);
    }

    @Override
    public Scope getVisibleScope() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.getVisibleScope();
    }

    @Override
    public String getName() {
        // no need to load model
        return super.getName();
    }

    @Override
    public boolean isShared() {
        // no need to load model
        return super.isShared();
    }

    @Override
    public List<Annotation> getAnnotations() {
        load();
        return super.getAnnotations();
    }

    @Override
    public String getQualifiedNameString() {
        load();
        return super.getQualifiedNameString();
    }

    @Override
    public boolean isActual() {
        load();
        return super.isActual();
    }

    @Override
    public boolean isFormal() {
        load();
        return super.isFormal();
    }

    @Override
    public boolean isDefault() {
        load();
        return super.isDefault();
    }

    @Override
    public Declaration getRefinedDeclaration() {
        load();
        return super.getRefinedDeclaration();
    }

    @Override
    public boolean isVisible(Scope scope) {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isVisible(scope);
    }

    @Override
    public boolean isDefinedInScope(Scope scope) {
        load();
        return super.isDefinedInScope(scope);
    }

    @Override
    public boolean isCaptured() {
        load();
        return super.isCaptured();
    }

    @Override
    public boolean isToplevel() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isToplevel();
    }

    @Override
    public boolean isClassMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isClassMember();
    }

    @Override
    public boolean isInterfaceMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isInterfaceMember();
    }

    @Override
    public boolean isClassOrInterfaceMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isClassOrInterfaceMember();
    }

    @Override
    public boolean isMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isMember();
    }

    @Override
    public boolean isStaticallyImportable() {
        load();
        return super.isStaticallyImportable();
    }

    @Override
    public boolean isProtectedVisibility() {
        load();
        return super.isProtectedVisibility();
    }

    @Override
    public boolean isPackageVisibility() {
        load();
        return super.isPackageVisibility();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean refines(Declaration other) {
        load();
        return super.refines(other);
    }

    @Override
    public boolean isAnonymous() {
        load();
        return super.isAnonymous();
    }

    @Override
    public String getNameAsString() {
        load();
        return super.getNameAsString();
    }

    @Override
    public Unit getUnit() {
        // this doesn't require to load the model
        return super.getUnit();
    }

    @Override
    public Scope getContainer() {
        // this doesn't require to load the model
        return super.getContainer();
    }

    @Override
    public List<Declaration> getMembers() {
        load();
        return super.getMembers();
    }

    @Override
    public Declaration getDirectMember(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getDirectMember(name, signature, ellipsis);
    }

    @Override
    public Type getDeclaringType(Declaration d) {
        load();
        return super.getDeclaringType(d);
    }

    @Override
    public Declaration getMemberOrParameter(Unit unit, String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getMemberOrParameter(unit, name, signature, ellipsis);
    }

    @Override
    public void setLocal(boolean local) {
        this.local = local;
    }
    
    @Override
    public boolean isLocal(){
        return this.local ;
    }

    @Override
    public Declaration getLocalDeclaration(String name) {
        return null;
    }

    @Override
    public void addLocalDeclaration(Declaration declaration) {
        throw new RuntimeException("type aliases do not contain any local declarations");
    }

    @Override
    public boolean isDeprecated() {
        load();
        return super.isDeprecated();
    }
}
