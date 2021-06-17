package org.metacity.commands;

/**
 * Check a particular argument in the command
 * Determine if the node should be executed
 */
@FunctionalInterface
public interface NodeValidator {

    boolean validate(String s);

}
