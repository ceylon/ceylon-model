package com.redhat.ceylon.model.typechecker.model;


/**
 * An attribute getter.
 *
 * @author Gavin King
 */
@Deprecated
public class Getter extends Value implements Scope {
    @Override
    public boolean isTransient() {
        return true;
    }
}
