package xyz.wagyourtail.jsmacrosjython;

import java.io.File;
import java.util.Map;

import org.python.util.PythonInterpreter;

import net.fabricmc.api.ClientModInitializer;
import xyz.wagyourtail.jsmacros.config.RawMacro;
import xyz.wagyourtail.jsmacros.runscript.RunScript;
import xyz.wagyourtail.jsmacros.runscript.functions.Functions;

public class JsMacrosJython implements ClientModInitializer {
    public static boolean hasJEP = false;
    
    @Override
    public void onInitializeClient() {
        
        for (Functions fun : RunScript.standardLib) {
            if (fun.libName.equals("fs") || fun.libName.equals("time")) {
                fun.excludeLanguages.add("jython.py");
            }
        }
        
        // register language
        RunScript.addLanguage(new RunScript.Language() {

            @Override
            public void exec(RawMacro macro, File file, String event, Map<String, Object> args) throws Exception {
                try (PythonInterpreter interp = new PythonInterpreter()) {
                    interp.set("event", event);
                    interp.set("args", args);
                    interp.set("file", file);

                    for (Functions f : RunScript.standardLib) {
                        if (!f.excludeLanguages.contains("jython.py")) {
                            interp.set(f.libName, f);
                        }
                    }

                    interp.exec("import os\nos.chdir('"
                        + file.getParentFile().getCanonicalPath().replaceAll("\\\\", "/") + "')");
                    interp.execfile(file.getCanonicalPath());
                } catch (Exception e) {
                    throw e;
                }
            }


            @Override
            public String extension() {
                return hasJEP ? "jython.py" : ".py";
            }

        });

        RunScript.sortLanguages();
        
        // pre-init
        Thread t = new Thread(() -> {
            try (PythonInterpreter interp = new PythonInterpreter()) {
                interp.exec("print(\"Jython Loaded.\")");
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        
        t.start();
    }

}