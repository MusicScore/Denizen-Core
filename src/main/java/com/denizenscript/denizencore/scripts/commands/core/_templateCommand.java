package com.denizenscript.denizencore.scripts.commands.core;

import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;

/**
 * Your command!
 * This class is a template for a Command in Denizen.
 * <p/>
 * If loading externally, implement dExternal and its load() method.
 *
 * @author Jeremy Schroeder, mcmonkey
 */
public class _templateCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("required_integer")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("required_integer", arg.asElement());
            }

            // else if (...)

            else {
                arg.reportUnhandled();
            }
        }

        // Check for required information
        if (!scriptEntry.hasObject("required_object")) {
            throw new InvalidArgumentsException("Must have required object!");
        }

    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Fetch required objects
        Element required_integer = scriptEntry.getElement("required_integer");
        // dObject object = scriptEntry.getdObject("required_object");

        // Debug the execution
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), required_integer.debug());
        }

        // Do the execution

        // INSERT
        // YOUR
        // CODE
        // HERE :)
    }
}