package com.denizenscript.denizencore.objects;

import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.containers.core.CustomScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.dB;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;

import java.util.Map;
import java.util.regex.Matcher;

public class CustomObject implements dObject, dObject.ObjectAttributable, Adjustable {

    // <--[language]
    // @name Custom Objects
    // @group Object System
    // @description
    // Custom objects are custom object types. They use a script basis to create an object
    // similar to the base object types (dLists, dPlayers, etc).
    //
    // Usage of these should generally be avoided, as they can be considered 'over-engineering'...
    // That is, using a very complicated solution to solve a problem that can be solved much more simply.
    //
    // -->

    // <--[language]
    // @name custom@
    // @group Object Fetcher System
    // @description
    // custom@ refers to the 'object identifier' of a Custom Object. The 'custom@' is notation for Denizen's Object
    // Fetcher. The constructor for an Custom Object is the name of the custom script, with any relevant properties specified.
    // -->

    @Fetchable("custom")
    public static CustomObject valueOf(String string, TagContext context) {
        Matcher m;

        ///////
        // Handle objects with properties through the object fetcher
        m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(CustomObject.class, string, context);
        }

        if (string.startsWith("custom@")) {
            string = string.substring("custom@".length());
        }

        String typeData = string;
        ScriptContainer sc = ScriptRegistry.getScriptContainer(typeData);
        if (sc == null) {
            if (context == null || context.debug) {
                dB.echoError("Null script container for " + typeData);
            }
            return null;
        }
        if (!(sc instanceof CustomScriptContainer)) {
            if (context == null || context.debug) {
                dB.echoError("Wrong-typed script container for " + typeData);
            }
            return null;
        }
        return new CustomObject((CustomScriptContainer) sc, ((CustomScriptContainer) sc).getVars());
    }

    public static CustomObject getFor(dObject obj, TagContext context) {
        return obj instanceof CustomObject ? (CustomObject) obj : valueOf(obj.toString(), context);
    }

    public static boolean matches(String string) {
        return string.startsWith("custom@");
    }

    public CustomScriptContainer container;
    public Map<String, dObject> vars;

    public CustomObject(CustomScriptContainer type, Map<String, dObject> values) {
        container = type;
        vars = values;
    }

    private String prefix = "Custom";

    @Override
    public String getObjectType() {
        return "Custom";
    }

    @Override
    public String identify() {
        StringBuilder outp = new StringBuilder();
        for (Map.Entry<String, dObject> var : vars.entrySet()) {
            outp.append(var.getKey() + "=" + var.getValue().toString().replace(';', (char) 0x2011) + ";");
        }
        return "custom@" + container.getName() + "[" + (outp.length() > 0 ? outp.substring(0, outp.length() - 1) : "") + "]";
    }

    @Override
    public String identifySimple() {
        return "custom@" + container.getName();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getAttribute(Attribute attribute) {
        return CoreUtilities.stringifyNullPass(getObjectAttribute(attribute));
    }

    @Override
    public <T extends dObject> T asObjectType(Class<T> type, TagContext context) {
        return null;
    }

    @Override
    public dObject getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        if (attribute.isComplete()) {
            return this;
        }

        dObject res = vars.get(attribute.getAttribute(1));
        if (res == null) {
            String taggo = attribute.getAttributeWithoutContext(1);
            if (container.hasPath("tags." + taggo)) {
                dList outcomes = container.runTagScript(taggo, attribute.getContextObject(1), this,
                        attribute.getScriptEntry() != null ? attribute.getScriptEntry().entryData :
                                DenizenCore.getImplementation().getEmptyScriptEntryData());
                if (outcomes == null) {
                    return null;
                }
                return CoreUtilities.autoAttribTyped(outcomes.getObject(0), attribute.fulfill(1));
            }
            return new Element(identify()).getObjectAttribute(attribute);
        }
        return CoreUtilities.autoAttribTyped(res, attribute.fulfill(1));
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {
        String name = CoreUtilities.toLowerCase(mechanism.getName());
        if (!mechanism.hasValue()) {
            vars.remove(name);
            return;
        }
        dObject value = mechanism.getValue();
        if (container.hasPath("mechanisms." + name)) {
            dList outcomes = container.runMechScript(name, this, value);
            if (outcomes == null) {
                return;
            }
            CustomObject co = CustomObject.getFor(outcomes.getObject(0), null);
            container = co.container;
            vars = co.vars;
        }
        else {
            vars.put(name, value);
        }
        mechanism.fulfill();
    }
}